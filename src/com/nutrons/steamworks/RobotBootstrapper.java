package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.deadbandMap;
import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.ctre.CANTalon;
import com.libKudos254.vision.VisionServer;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.RevServo;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.CommonController;
import com.nutrons.framework.inputs.HeadingGyro;
import com.nutrons.framework.inputs.RadioBox;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RobotBootstrapper extends Robot {

  private Drivetrain drivetrain;
  private Climbtake climbtake;
  private LoopSpeedController shooterMotor1;
  private LoopSpeedController shooterMotor2;
  private Talon topFeederMotor;
  private Talon spinFeederMotor;
  private LoopSpeedController climberMotor1;
  private LoopSpeedController climberMotor2;
  private Talon hoodMaster;
  private Talon leftLeader;
  private Talon leftFollower;
  private Talon rightLeader;
  private Talon rightFollower;

  private RevServo servoLeft;
  private RevServo servoRight;

  private CommonController driverPad;
  private CommonController operatorPad;
  private HeadingGyro gyro;
  private Turret turret;
  private Shooter shooter;
  private RadioBox<Command> box;
  private Feeder feeder;
  private Gearplacer gearplacer;

  /**
   * Converts booleans into streams, and if the boolean is true,
   * delay the emission of the item by the specified amount.
   * Useful as an argument of switchMap on button streams.
   * The combination will delay all true value emissions by the specified delay,
   * but if false is emitted within that delay, the delayed true value will be discarded.
   * Effectively, subscribers will only receive true values if the button
   * is held down past the time specified by the delay.
   */
  static Function<Boolean, Flowable<Boolean>> delayTrue(long delay, TimeUnit unit) {
    return x -> x ? Flowable.just(false).delay(delay, unit) : Flowable.just(false);
  }

  @Override
  public Command registerAuto() {
    Flowable<Command> boxStream = box.selected().cache().map(FlowOperators::printId);
    boxStream.subscribe();
    return Command.defer(() -> {
      Command c = FlowOperators.getLastValue(boxStream);
      System.out.println(c);
      return c;
    });
  }

  @Override
  public Command registerTele() {
    return this.drivetrain.driveTeleop().terminable(Flowable.never());
  }

  @Override
  protected void constructStreams() {
    // Gamepads
    this.driverPad = CommonController.xbox360(RobotMap.DRIVER_PAD);
    this.operatorPad = CommonController.xbox360(RobotMap.OP_PAD);
    this.gyro = new HeadingGyro();

    this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A,
        CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    Events.setOutputVoltage(-6f, +6f).actOn(this.hoodMaster); //Move slow to set off limit switches
    //Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.hoodMaster.setOutputFlipped(false);
    this.hoodMaster.setReversedSensor(false);

    this.topFeederMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinFeederMotor = new Talon(RobotMap.SPIN_FEEDER_MOTOR, this.topFeederMotor);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2,
        CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1, (Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor1);

    this.climberMotor1 = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
    this.climberMotor2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2);

    this.climbtake = new Climbtake(climberMotor1, climberMotor2,
        this.driverPad.rightBumper(), this.driverPad.leftBumper());

    // Drivetrain Motors
    this.leftLeader = new Talon(RobotMap.BACK_LEFT);
    this.leftLeader.setControlMode(ControlMode.MANUAL);
    this.leftLeader.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    this.leftLeader.setReversedSensor(true);
    this.leftFollower = new Talon(RobotMap.FRONT_LEFT, this.leftLeader);

    this.rightLeader = new Talon(RobotMap.BACK_RIGHT);
    this.rightLeader.setControlMode(ControlMode.MANUAL);
    this.rightLeader.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    this.rightFollower = new Talon(RobotMap.FRONT_RIGHT, this.rightLeader);

    VisionServer visionServer = VisionServer.getInstance();
    visionServer.addVisionUpdateReceiver(VisionProcessor.getInstance());

    //Gear Placer Servos
    this.servoLeft = new RevServo(RobotMap.GEAR_SERVO_RIGHT);
    this.servoRight = new RevServo(RobotMap.GEAR_SERVO_LEFT);
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);

    sm.registerSubsystem(this.driverPad);
    sm.registerSubsystem(this.operatorPad);

    this.shooter = new Shooter(shooterMotor2, this.operatorPad.rightBumper(),
        toFlow(() -> VisionProcessor.getInstance().getDistance()).share(),
        this.operatorPad.rightStickY().map(FlowOperators.deadbandMap(-0.2, 0.2,0)).map(x -> -100.0 * x));
    sm.registerSubsystem(shooter);

    this.gearplacer = new Gearplacer(this.servoLeft,
        this.servoRight,
        this.driverPad.buttonX());
    sm.registerSubsystem(gearplacer);

    this.feeder = new Feeder(spinFeederMotor, topFeederMotor, this.operatorPad.buttonB());
    sm.registerSubsystem(feeder);
    this.turret = new Turret(VisionProcessor.getInstance().getHorizAngleFlow(),
        toFlow(() -> VisionProcessor.getInstance().getDistance()).share(), hoodMaster,
        this.operatorPad.leftStickX(), this.operatorPad.leftBumper());
    sm.registerSubsystem(turret); //TODO: remove
    this.driverPad.rightBumper().subscribe(System.out::println);
    sm.registerSubsystem(new Climbtake(climberMotor1, climberMotor2,
        this.driverPad.rightBumper(), this.driverPad.leftBumper()));
    leftLeader.setControlMode(ControlMode.MANUAL);
    rightLeader.setControlMode(ControlMode.MANUAL);
    this.leftLeader.accept(Events.resetPosition(0.0));
    this.rightLeader.accept(Events.resetPosition(0.0));
    this.drivetrain = new Drivetrain(driverPad.buttonB(),
        gyro.getGyroReadings().share(),
        driverPad.leftStickY().map(x -> -x),
        driverPad.rightStickX(),
        leftLeader, rightLeader);
    toFlow(() -> leftLeader.position())
        .subscribe(new WpiSmartDashboard().getTextFieldDouble("lpos"));
    toFlow(() -> rightLeader.position())
        .subscribe(new WpiSmartDashboard().getTextFieldDouble("rpos"));
    sm.registerSubsystem(this.drivetrain);
    Command kpa40 = Command.parallel(
        Command.fromAction(() -> {
          RobotBootstrapper.this.leftLeader.runAtPower(0);
          RobotBootstrapper.this.rightLeader.runAtPower(0);
        }),
        RobotBootstrapper.this.climbtake.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS),
        this.turret.automagicMode().delayFinish(1250, TimeUnit.MILLISECONDS),
        this.shooter.pulse().delayStart(1250, TimeUnit.MILLISECONDS).delayFinish(12, TimeUnit.SECONDS),
        this.feeder.pulse().delayStart(3250, TimeUnit.MILLISECONDS).delayFinish(10, TimeUnit.SECONDS));

    Map<String, Command> autos = new HashMap<String, Command>() {{
      put("intake", RobotBootstrapper.this
          .climbtake.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS));
      put("boiler; turn left", Command.serial(
          RobotBootstrapper.this.drivetrain.driveDistance(9.5, 1, 10).endsWhen(Flowable.timer(2, TimeUnit.SECONDS), true),
          RobotBootstrapper.this.drivetrain.turn(-85, 10),
          RobotBootstrapper.this.drivetrain.driveDistance(5.0, 1, 10).endsWhen(Flowable.timer(2, TimeUnit.SECONDS), true)).then(kpa40));
      put("boiler; turn right", Command.serial(
          RobotBootstrapper.this.drivetrain.driveDistance(9.5, 1, 10).endsWhen(Flowable.timer(2, TimeUnit.SECONDS), true),
          RobotBootstrapper.this.drivetrain.turn(85, 10),
          RobotBootstrapper.this.drivetrain.driveDistance(5.0, 1, 10).endsWhen(Flowable.timer(2, TimeUnit.SECONDS), true)).then(kpa40));
      put("aim & shoot", Command.parallel(RobotBootstrapper.this.shooter.pulse().delayFinish(12, TimeUnit.SECONDS),
          RobotBootstrapper.this.turret.automagicMode().delayFinish(12, TimeUnit.SECONDS),
          RobotBootstrapper.this.feeder.pulse().delayStart(2, TimeUnit.SECONDS).delayFinish(10, TimeUnit.SECONDS)));
      put("forward gear", RobotBootstrapper.this.drivetrain.driveDistance(-8, 0.25, 5).endsWhen(Flowable.timer(5, TimeUnit.SECONDS), true)
          .then(RobotBootstrapper.this.gearplacer.pulse().delayFinish(1, TimeUnit.SECONDS))
          .then(RobotBootstrapper.this.climbtake.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS))
          .then(RobotBootstrapper.this.drivetrain.driveDistance(2, 0.25, 5)));
    }};

    box = new RadioBox<>("auto4", autos, "intake");
    sm.registerSubsystem(box);

    return sm;
  }
}
