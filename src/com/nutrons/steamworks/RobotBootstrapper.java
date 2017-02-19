package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.CommonController;
import com.nutrons.framework.inputs.HeadingGyro;
import com.nutrons.framework.inputs.Serial;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import java.util.concurrent.TimeUnit;

import static com.nutrons.framework.util.FlowOperators.toFlow;

public class RobotBootstrapper extends Robot {

  public final static int PACKET_LENGTH = 17;
  private Drivetrain drivetrain;
  private LoopSpeedController shooterMotor2;
  private Talon topFeederMotor;
  private Talon spinFeederMotor;
  private LoopSpeedController climberController;
  private LoopSpeedController climberMotor2;
  private Talon hoodMaster;
  private Serial serial;
  private Vision vision;
  private Talon leftLeader;
  private Talon leftFollower;
  private Talon rightLeader;
  private Talon rightFollower;
  private CommonController driverPad;
  private CommonController operatorPad;
  private HeadingGyro gyro;
  private Talon intakeController;
  private Talon shooterMotor1;
  private Talon intakeController2;

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
    return x -> x ? Flowable.just(true).delay(delay, unit) : Flowable.just(false);
  }

  @Override
  public Command registerAuto() {
    return this.drivetrain.driveDistanceAction(1);
  }

  @Override
  public Command registerTele() {
    return this.drivetrain.driveTeleop();
  }
  @Override
  protected void constructStreams() {
    this.serial = new Serial(PACKET_LENGTH * 2, PACKET_LENGTH);
    this.vision = Vision.getInstance(serial.getDataStream());

    this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A, CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    Events.setOutputVoltage(-12f, +12f).actOn(this.hoodMaster);
    Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.hoodMaster.setOutputFlipped(false);
    this.hoodMaster.setReversedSensor(false);

    this.topFeederMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinFeederMotor = new Talon(RobotMap.SPIN_FEEDER_MOTOR, this.topFeederMotor);
    this.intakeController = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
    this.intakeController2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2, (Talon) this.intakeController);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1, (Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor1);

    this.climberController = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
    this.climberMotor2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2);

    // Drivetrain Motors
    this.leftLeader = new Talon(RobotMap.BACK_LEFT);
    this.leftLeader.setControlMode(ControlMode.MANUAL);
    this.leftLeader.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    this.leftFollower = new Talon(RobotMap.FRONT_LEFT, this.leftLeader);

    this.rightLeader = new Talon(RobotMap.BACK_RIGHT);
    this.rightLeader.setControlMode(ControlMode.MANUAL);
    this.rightLeader.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    this.rightFollower = new Talon(RobotMap.FRONT_RIGHT, this.rightLeader);

    // Gamepads
    this.driverPad = CommonController.xbox360(RobotMap.DRIVER_PAD);
    this.operatorPad = CommonController.xbox360(RobotMap.OP_PAD);
    this.gyro = new HeadingGyro();
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);
    sm.registerSubsystem(this.driverPad);
    sm.registerSubsystem(this.operatorPad);

    sm.registerSubsystem(new Shooter(shooterMotor2, this.operatorPad.rightBumper()));
    sm.registerSubsystem(new Feeder(spinFeederMotor, topFeederMotor, this.operatorPad.buttonB()));
    sm.registerSubsystem(new Climbtake(climberController, climberMotor2, this.driverPad.buttonY(), this.driverPad.buttonA()));
    sm.registerSubsystem(new Turret(vision.getAngle(), vision.getState(), hoodMaster, this.operatorPad.leftStickY())); //TODO: remove

    leftLeader.setControlMode(ControlMode.MANUAL);
    rightLeader.setControlMode(ControlMode.MANUAL);
    this.drivetrain = new Drivetrain(driverPad.buttonA(),
        gyro.getGyroReadings(), Flowable.just(0.0)
        .concatWith(driverPad.buttonA().filter(x -> x).map(x -> this.gyro.getAngle())),
        driverPad.rightStickX(), driverPad.leftStickY().map(x -> -x),
        leftLeader, rightLeader);
    toFlow(() -> leftLeader.position()).subscribe(new WpiSmartDashboard().getTextFieldDouble("lpos"));
    toFlow(() -> rightLeader.position()).subscribe(new WpiSmartDashboard().getTextFieldDouble("rpos"));
    sm.registerSubsystem(this.drivetrain);
    return sm;
  }
}
