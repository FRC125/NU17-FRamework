package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.controllers.WpiTalonProxy;
import com.nutrons.framework.inputs.WpiXboxGamepad;

public class RobotBootstrapper extends Robot {
    private Talon intakeController;
    private WpiXboxGamepad controller;
    private Talon shooterMotor1;
    private Talon shooterMotor2;
    private Talon topHopperMotor;
    private Talon spinHopperMotor;
    public static Talon hoodMaster;
    public static CANTalon hmt;

    @Override
    protected void constructStreams() {
        this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
        this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
        this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
        this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
        this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
        this.controller = new WpiXboxGamepad(0);

        hmt = new CANTalon(RobotMap.HOOD_MOTOR_A);
        this.hoodMaster = new Talon(new WpiTalonProxy(hmt));
        hoodMaster.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
        hoodMaster.configNominalOutputVoltage(+0f, -0f);
        hoodMaster.configPeakOutputVoltage(+12f, -12f);
        hoodMaster.reverseOutput(false);
        hoodMaster.reverseSensor(false);
        hmt.clearStickyFaults();
        hmt.setAllowableClosedLoopErr(0);
        hmt.setProfile(0);

        this.enabledStream();
    }

    @Override
    protected StreamManager provideStreamManager() {
        StreamManager sm = new StreamManager(this);
        sm.registerSubsystem(new Shooter(shooterMotor1));
        sm.registerSubsystem(new Feeder(intakeController));
        sm.registerSubsystem(new Hopper(spinHopperMotor));
        return sm;
    }
}
