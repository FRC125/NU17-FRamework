package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.CommonController;
import com.nutrons.framework.inputs.HeadingGyro;
import com.nutrons.framework.inputs.Serial;
import com.nutrons.framework.util.FlowOperators;
import com.nutrons.libKudos254.vision.VisionServer;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import java.util.concurrent.TimeUnit;

public class RobotBootstrapper extends Robot {

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

  private CommonController driverPad;
  private CommonController operatorPad;
  private HeadingGyro gyro;

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
  protected void constructStreams() {

    this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A, CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    Events.setOutputVoltage(-6f, +6f).actOn(this.hoodMaster); //Move slow enough to set off limit switches
    //Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.hoodMaster.setOutputFlipped(false);
    this.hoodMaster.setReversedSensor(false);

    this.topFeederMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinFeederMotor = new Talon(RobotMap.SPIN_FEEDER_MOTOR, this.topFeederMotor);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1, (Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor2);
    Events.setOutputVoltage(-12f, +12f).actOn((Talon) this.shooterMotor1);

    this.climberMotor1 = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
    this.climberMotor2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2);

    this.climberMotor2.noSticky();
    this.climberMotor2.setControlMode(ControlMode.MANUAL);
    this.climberMotor1.noSticky();
    this.climberMotor1.setControlMode(ControlMode.MANUAL);
    this.climberMotor1.enableControl();
    this.climberMotor2.enableControl();

    // Drivetrain Motors
    this.leftLeader = new Talon(RobotMap.FRONT_LEFT);
    this.leftLeader.setControlMode(ControlMode.MANUAL);
    this.leftFollower = new Talon(RobotMap.BACK_LEFT, this.leftLeader);

    this.rightLeader = new Talon(RobotMap.FRONT_RIGHT);
    this.rightLeader.setControlMode(ControlMode.MANUAL);
    this.rightFollower = new Talon(RobotMap.BACK_RIGHT, this.rightLeader);

    // Gamepads
    this.driverPad = CommonController.xbox360(RobotMap.DRIVER_PAD);
    this.operatorPad = CommonController.xbox360(RobotMap.OP_PAD);
    this.gyro = new HeadingGyro();

    VisionServer mVisionServer = VisionServer.getInstance();
    mVisionServer.addVisionUpdateReceiver(VisionProcessor.getInstance());

  }

  @Override
  protected StreamManager provideStreamManager() {
    this.climberMotor1.setControlMode(ControlMode.MANUAL);
    this.climberMotor2.setControlMode(ControlMode.MANUAL);
    this.climberMotor1.enableControl();
    this.climberMotor2.enableControl();
    StreamManager sm = new StreamManager(this);
    sm.registerSubsystem(this.driverPad);
    sm.registerSubsystem(this.operatorPad);

    sm.registerSubsystem(new Shooter(shooterMotor2, this.operatorPad.rightBumper()));
    sm.registerSubsystem(new Feeder(spinFeederMotor, topFeederMotor, this.operatorPad.buttonB()));
    sm.registerSubsystem(new Climbtake(climberMotor1, climberMotor2, this.driverPad.rightBumper(), this.driverPad.leftBumper()));
    sm.registerSubsystem(new Turret(VisionProcessor.getInstance().getHorizAngleFlow().map(FlowOperators::printId), hoodMaster, this.operatorPad.leftStickX(), this.operatorPad.leftBumper())); //TODO: remove

    leftLeader.setControlMode(ControlMode.MANUAL);
    rightLeader.setControlMode(ControlMode.MANUAL);
    sm.registerSubsystem(new Drivetrain(driverPad.buttonB(),
        gyro.getGyroReadings(), Flowable.just(0.0)
        .concatWith(driverPad.buttonB().filter(x -> x).map(x -> this.gyro.getAngle())),
        driverPad.rightStickX(), driverPad.leftStickY().map(x -> -x),
        leftLeader, rightLeader));
    return sm;
  }
}
