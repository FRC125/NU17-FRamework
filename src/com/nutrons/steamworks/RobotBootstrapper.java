package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.Serial;
import com.nutrons.framework.inputs.WpiXboxGamepad;

public class RobotBootstrapper extends Robot {

  public final static int PACKET_LENGTH = 17;
  private Talon intakeController;
  private Talon shooterMotor1;
  private Talon shooterMotor2;
  private Talon topHopperMotor;
  private Talon spinHopperMotor;
  private Talon hoodMaster;
  private Serial serial;
  private Vision vision;

  private Talon leftLeader;
  private Talon leftFollower;
  private Talon rightLeader;
  private Talon rightFollower;

  private WpiXboxGamepad driverPad;
  private WpiXboxGamepad operatorPad;

  @Override
  protected void constructStreams() {

    //this.serial = new Serial(PACKET_LENGTH *2, PACKET_LENGTH);
    //this.vision = Vision.getInstance(serial.getDataStream());

     /*
    this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A,
            CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
            Events.setOutputVoltage(-12f, +12f).actOn(this.hoodMaster);
            Events.resetPosition(0.0).actOn(this.hoodMaster);


    this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
    this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
    */
    // Drivetrain Motors
    this.leftLeader = new Talon(RobotMap.FRONT_LEFT);
    this.leftLeader.setControlMode(ControlMode.MANUAL);

    this.leftFollower = new Talon(RobotMap.BACK_LEFT, this.leftLeader);


    this.rightLeader = new Talon(RobotMap.FRONT_RIGHT);
    this.rightLeader.setControlMode(ControlMode.MANUAL);

    this.rightFollower = new Talon(RobotMap.BACK_RIGHT, this.rightLeader);

    // Gamepads
    this.driverPad = new WpiXboxGamepad(RobotMap.DRIVER_PAD);
    //this.operatorPad = new WpiXboxGamepad(RobotMap.OP_PAD);
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);
    /*
    sm.registerSubsystem(new Turret(vision.getAngle(), hoodMaster));
    sm.registerSubsystem(new Shooter(shooterMotor1));
    sm.registerSubsystem(new Feeder(intakeController));
    sm.registerSubsystem(new Hopper(spinHopperMotor));
    sm.registerSubsystem(new ButtonTest(driverPad.button(1)));
    Events.mode(ControlMode.MANUAL).actOn(leftLeader);
    Events.mode(ControlMode.MANUAL).actOn(rightLeader);
    */
    sm.registerSubsystem(new Drivetrain(driverPad.joy2X().map(x -> -x), driverPad.joy1Y().map(x -> x), this.driverPad.button(1),
        leftLeader, rightLeader));
    return sm;
  }
}
