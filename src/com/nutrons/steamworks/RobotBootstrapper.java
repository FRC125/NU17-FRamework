package com.nutrons.steamworks;

import com.ctre.CANTalon;
import com.nutrons.framework.Robot;
import com.nutrons.framework.StreamManager;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.Serial;
import com.nutrons.framework.inputs.WpiXboxGamepad;

import static com.nutrons.framework.controllers.Events.setOutputVoltage;

public class RobotBootstrapper extends Robot {

    public final static int PACKET_LENGTH = 17;
    private LoopSpeedController intakeController;
    private LoopSpeedController intakeController2;
    private Talon shooterMotor1;
    private Talon shooterMotor2;
    private Talon topFeederMotor;
    private Talon spinFeederMotor;
    private Talon climberController;
    private Talon climberMotor2;
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
        this.serial = new Serial(PACKET_LENGTH *2, PACKET_LENGTH);
        this.vision = Vision.getInstance(serial.getDataStream());

        this.hoodMaster = new Talon(RobotMap.HOOD_MOTOR_A, CANTalon.FeedbackDevice.CtreMagEncoder_Absolute);
        Events.setOutputVoltage(-12f, +12f).actOn(this.hoodMaster);
        Events.resetPosition(0.0).actOn(this.hoodMaster);

        this.topFeederMotor = new Talon(RobotMap.TOP_HOPPER_MOTOR);
        this.spinFeederMotor = new Talon(RobotMap.SPIN_FEEDER_MOTOR, this.topFeederMotor);
        this.intakeController = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
        this.intakeController2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2, (Talon) this.intakeController);
        this.shooterMotor1 = new Talon(RobotMap.SHOOTER_MOTOR_1);
        this.shooterMotor2 = new Talon(RobotMap.SHOOTER_MOTOR_2, this.shooterMotor1);
        this.climberController = new Talon(RobotMap.CLIMBTAKE_MOTOR_1);
        this.climberMotor2 = new Talon(RobotMap.CLIMBTAKE_MOTOR_2, this.climberController);
        // Drivetrain Motors
        this.leftLeader = new Talon(RobotMap.FRONT_LEFT);
        this.leftFollower = new Talon(RobotMap.BACK_LEFT, this.leftLeader);
        this.rightLeader = new Talon(RobotMap.BACK_RIGHT);
        this.rightFollower = new Talon(RobotMap.FRONT_RIGHT, this.rightLeader);
        // Gamepads
        this.driverPad = new WpiXboxGamepad(RobotMap.DRIVER_PAD);
        this.operatorPad = new WpiXboxGamepad(RobotMap.OP_PAD);
    }

    @Override
    protected StreamManager provideStreamManager() {
        StreamManager sm = new StreamManager(this);
        sm.registerSubsystem(new Turret(vision.getAngle(), hoodMaster));
        sm.registerSubsystem(new Shooter(shooterMotor1));
        sm.registerSubsystem(new Feeder(spinFeederMotor));
        sm.registerSubsystem(new Climbtake(climberController, intakeController));
        sm.registerSubsystem(new Drivetrain(driverPad.joy2X().map(x -> -x), driverPad.joy1Y(),
                leftLeader, rightLeader));
        return sm;
    }

}
