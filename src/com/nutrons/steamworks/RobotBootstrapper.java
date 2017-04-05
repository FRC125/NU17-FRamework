package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.ctre.CANTalon;
import com.libKudos254.vision.VisionServer;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
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
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import javafx.scene.Camera;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RobotBootstrapper extends Robot {

  private Gearplacer gearplacer;
  private Drivetrain drivetrain;
  private Climbtake climbtake;
  private FloorGearPlacer floorGearPlacer;
  private LoopSpeedController shooterMotor1;
  private LoopSpeedController shooterMotor2;
  private Talon wristMotor;
  private Talon intakeMotor;
  private Talon topFeederMotor;
  private Talon spinFeederMotor;
  private LoopSpeedController climberMotor1;
  private LoopSpeedController climberMotor2;
  private Talon hoodMaster;
  private Talon leftLeader;
  private Talon leftFollower;
  private Talon rightLeader;
  private Talon rightFollower;
  private SendableChooser<Command> autoSelector;
  private RevServo servoRight;
  private RevServo servoLeft;


  private CommonController driverPad;
  private CommonController operatorPad;
  private HeadingGyro gyro;
  private Turret turret;
  private Shooter shooter;
  public static Feeder feeder;

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
    return Command.defer(() -> {
      Command c = autoSelector.getSelected();
      System.out.println("auto command retrieved from SD");
      System.out.println(c);
      return c;
    });
  }

  @Override
  public Command registerTele() {
    return Command.parallel(this.drivetrain.driveTeleop().terminable(Flowable.never()),
        this.turret.teleControl().terminable(Flowable.never()));
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

    //this.intakeMotor = new Talon(RobotMap.INTAKE_MOTOR);
    //this.wristMotor = new Talon(RobotMap.WRIST_MOTOR);

    //Gear Placer Servos
    this.servoLeft = new RevServo(RobotMap.GEAR_SERVO_RIGHT);
    this.servoRight = new RevServo(RobotMap.GEAR_SERVO_LEFT);


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
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);

    sm.registerSubsystem(this.driverPad);
    sm.registerSubsystem(this.operatorPad);

    gearplacer = new Gearplacer(this.servoLeft, this.servoRight, this.driverPad
        .buttonX());
    sm.registerSubsystem(this.gearplacer);

    this.shooter = new Shooter(shooterMotor2, this.operatorPad.rightBumper(),
        toFlow(() -> VisionProcessor.getInstance().getDistance()),
        this.operatorPad.rightStickY().map(FlowOperators.deadbandMap(-0.2, 0.2, 0))
            .map(x -> -100.0 * x));
    sm.registerSubsystem(shooter);

    this.feeder = new Feeder(spinFeederMotor, topFeederMotor, this.operatorPad.buttonB(),
        this.operatorPad.buttonY());
    sm.registerSubsystem(feeder);
    this.turret = new Turret(VisionProcessor.getInstance().getHorizAngleFlow(),
        toFlow(() -> VisionProcessor.getInstance().getDistance()), hoodMaster,
        this.operatorPad.leftStickX(), this.operatorPad.leftBumper());
    sm.registerSubsystem(turret); //TODO: remove
    this.driverPad.rightBumper().subscribe(System.out::println);
    sm.registerSubsystem(new Climbtake(climberMotor1, climberMotor2,
        this.driverPad.rightBumper(), this.driverPad.leftBumper()));
    leftLeader.setControlMode(ControlMode.MANUAL);
    rightLeader.setControlMode(ControlMode.MANUAL);
    this.leftLeader.accept(Events.resetPosition(0.0));
    this.rightLeader.accept(Events.resetPosition(0.0));
    this.leftLeader.setVoltageRampRate(80); //48
    this.rightLeader.setVoltageRampRate(80); //48
    this.drivetrain = new Drivetrain(driverPad.buttonB(),
        //Flowable.never(),
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
        this.turret.automagicMode().delayFinish(1000, TimeUnit.MILLISECONDS),
        this.shooter.pulse().delayStart(1000, TimeUnit.MILLISECONDS)
            .delayFinish(13, TimeUnit.SECONDS),
        this.feeder.pulse().delayStart(3000, TimeUnit.MILLISECONDS)
            .delayFinish(11, TimeUnit.SECONDS));

    this.autoSelector = new SendableChooser<>();
    this.autoSelector.addDefault("intake", RobotBootstrapper.this
        .climbtake.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS));

    this.autoSelector.addObject("boiler; turn left", hopperDrive(5.75, -85, 5.25));
    this.autoSelector.addObject("boiler; turn right", hopperDrive(5.75, 85, 5.25));
    this.autoSelector.addObject("aim & shoot",
        Command.parallel(RobotBootstrapper.this.shooter.pulse().delayFinish(12, TimeUnit.SECONDS),
            RobotBootstrapper.this.turret.automagicMode().delayFinish(12, TimeUnit.SECONDS),
            RobotBootstrapper.this.feeder.pulse().delayStart(2, TimeUnit.SECONDS)
                .delayFinish(10, TimeUnit.SECONDS)));
    this.autoSelector.addObject("forward gear",
        RobotBootstrapper.this.drivetrain.driveDistance(-8, 0.25, 5)
            .endsWhen(Flowable.timer(5, TimeUnit.SECONDS), true)
            .then(RobotBootstrapper.this.gearplacer.pulse().delayFinish(1, TimeUnit.SECONDS))
            .then(RobotBootstrapper.this.climbtake.pulse(true)
                .delayFinish(500, TimeUnit.MILLISECONDS))
            .then(RobotBootstrapper.this.drivetrain.driveDistance(2, 0.25, 5)));
    SmartDashboard.putData("automesdjme5", this.autoSelector);
    return sm;
  }

  private Command hopperDrive(double distance1, double angle, double distance2) {
    return
        Command.parallel(
            climbtake.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS)
                .then(climbtake.pulse(false).delayFinish(500, TimeUnit.MILLISECONDS)),
            Command.serial(drivetrain.driveDistance(distance1, .25, 5)
                    .endsWhen(Flowable.timer(1300, TimeUnit.MILLISECONDS), true),
                drivetrain.turn(angle, 5),
                Command.parallel(
                    turret.automagicMode().delayFinish(15000, TimeUnit.MILLISECONDS),
                    shooter.auto().delayStart(1500, TimeUnit.MILLISECONDS)
                        .delayFinish(15, TimeUnit.SECONDS),
                    drivetrain.driveDistance(distance2, .25, 5)
                        .endsWhen(Flowable.timer(1300, TimeUnit.MILLISECONDS), true),
                    feeder.pulse().delayStart(4300, TimeUnit.MILLISECONDS)
                        .delayFinish(15000, TimeUnit.MILLISECONDS)
                )
            )
        );
  }
}
