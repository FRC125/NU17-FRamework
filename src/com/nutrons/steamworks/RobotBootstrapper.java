package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.Serial;
import com.nutrons.framework.inputs.WpiXboxGamepad;

public class RobotBootstrapper extends Robot {
  private Talon intakeController;
  private WpiXboxGamepad controller;
  private Talon shooterMotor1;
  private Talon shooterMotor2;
  private Talon topHopperMotor;
  private Talon spinHopperMotor;
  public static Talon hoodMaster;
  private Serial serial;
  private Vision vision;

  @Override
  protected void constructStreams() {

    this.serial = new Serial(24, 12);
    this.vision = new Vision(serial.getDataStream());

    this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A,
        CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
    //hoodMaster.configNominalOutputVoltage(+0f, -0f);
    //hoodMaster.configPeakOutputVoltage(+12f, -12f);
    //TODO: add these methods somehow and reset position of hoodMaster

    this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
    this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
    this.controller = new WpiXboxGamepad(0);
    this.enabledStream();
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);
    sm.registerSubsystem(new Turret(vision.getAngle(), hoodMaster));
    sm.registerSubsystem(new Shooter(shooterMotor1));
    sm.registerSubsystem(new Feeder(intakeController));
    sm.registerSubsystem(new Hopper(spinHopperMotor));
    return sm;
  }
}
