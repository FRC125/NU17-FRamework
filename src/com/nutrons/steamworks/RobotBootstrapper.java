package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.WpiXboxGamepad;

public class RobotBootstrapper extends Robot {
  private LoopSpeedController intakeController;
  private Talon shooterMotor1;
  private Talon shooterMotor2;
  private Talon topHopperMotor;
  private Talon spinHopperMotor;

  private Talon leftLeader;
  private Talon leftFollower;
  private Talon rightLeader;
  private Talon rightFollower;

  private WpiXboxGamepad driverPad;
  private WpiXboxGamepad operatorPad;

  @Override
  protected void constructStreams() {
    this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
    this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
    this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
    this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
    this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
    // Drivetrain Motors
    this.leftLeader = new Talon(RobotMap.FRONT_LEFT);
    this.leftFollower = new Talon(RobotMap.BACK_LEFT, this.leftLeader);
    this.rightLeader = new Talon(RobotMap.FRONT_RIGHT);
    this.rightFollower = new Talon(RobotMap.BACK_RIGHT, this.rightLeader);
    // Gamepads
    this.driverPad = new WpiXboxGamepad(RobotMap.DRIVER_PAD);
    // this.operatorPad = new WpiXboxGamepad(RobotMap.OP_PAD);
  }

  @Override
  protected StreamManager provideStreamManager() {
    StreamManager sm = new StreamManager(this);
    //  sm.registerSubsystem(new Shooter(shooterMotor1));
    //  sm.registerSubsystem(new Feeder(intakeController));
    //  sm.registerSubsystem(new Hopper(spinHopperMotor));
    Events.mode(ControlMode.MANUAL).actOn(leftLeader);
    Events.mode(ControlMode.MANUAL).actOn(rightLeader);
    sm.registerSubsystem(new Drivetrain(driverPad.joy2X(), driverPad.joy1Y().map(x -> -x),
        leftLeader, rightLeader));
    return sm;
  }
}
