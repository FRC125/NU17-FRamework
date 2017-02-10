package com.nutrons.steamworks;

import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.WpiXboxGamepad;

public class RobotBootstrapper extends Robot {
    private Talon intakeController;
    private WpiXboxGamepad controller;
    private Talon shooterMotor1;
    private Talon shooterMotor2;
    private Talon topHopperMotor;
    private Talon spinHopperMotor;
    private Talon climberController;
    private Talon climberMotor2;

    @Override
    protected void constructStreams() {
        this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
        this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
        this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
        this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
        this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
        this.climberController = new Talon(RobotMap.CLIMBER_MOTOR_1);
        this.climberMotor2 = new Talon(RobotMap.CLIMBER_MOTOR_2, this.climberController);
        this.controller = new WpiXboxGamepad(0);
        this.enabledStream();
    }

    @Override
    protected StreamManager provideStreamManager() {
        StreamManager sm = new StreamManager(this);
        sm.registerSubsystem(new Shooter(shooterMotor1));
        sm.registerSubsystem(new Feeder(intakeController));
        sm.registerSubsystem(new Hopper(spinHopperMotor));
        sm.registerSubsystem(new Climber(climberController));
        return sm;
    }
}
