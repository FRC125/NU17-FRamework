package com.nutrons.steamworks;

import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.WpiXboxGamepad;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;

public class RobotBootstrapper extends Robot {
    private Talon intakeController;
    private Talon shooterMotor1;
    private Talon shooterMotor2;
    private Talon topHopperMotor;
    private Talon spinHopperMotor;
    private Talon leftLeader;
    private Talon leftFollower;
    private Talon rightLeader;
    private Talon rightFollower;
    private ADXRS450_Gyro headingGyro;
    private WpiXboxGamepad driverPad;
    private WpiXboxGamepad operatorPad;




    @Override
    protected void constructStreams() {
        this.topHopperMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
        this.spinHopperMotor = new Talon(RobotMap.SPIN_HOPPER_MOTOR, this.topHopperMotor);
        this.intakeController = new Talon(RobotMap.INTAKE_MOTOR);
        this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
        this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
        // Motors
        this.leftLeader = new Talon(RobotMap.FRONT_LEFT);
        this.leftFollower = new Talon(RobotMap.BACK_LEFT, this.leftLeader);
        this.rightLeader = new Talon(RobotMap.BACK_RIGHT);
        this.rightFollower = new Talon(RobotMap.FRONT_RIGHT, this.rightLeader);
        this.driverPad = new WpiXboxGamepad(RobotMap.DRIVER_PAD);
        this.operatorPad = new WpiXboxGamepad(RobotMap.OP_PAD);
        this.headingGyro = new  ADXRS450_Gyro();
        this.enabledStream();

    }

    @Override
    protected StreamManager provideStreamManager() {
        StreamManager sm = new StreamManager(this);
        sm.registerSubsystem(new Shooter(shooterMotor1));
        sm.registerSubsystem(new Feeder(intakeController));
        sm.registerSubsystem(new Hopper(spinHopperMotor));
        // Todo: Get right button number for right trigger
        sm.registerSubsystem(new Drivetrain(driverPad.joy2X().map(x -> -x), driverPad.joy1Y(), driverPad.button(0),
                leftLeader, rightLeader));
        return sm;
    }
}
