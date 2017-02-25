package com.nutrons.steamworks;

public class RobotMap {

  // Intake or Shooter
  public static final int SHOOTER_MOTOR_1 = 2;
  public static final int SHOOTER_MOTOR_2 = 3;
  //TODO: Change climber ports to match robot.
  public static final int CLIMBTAKE_MOTOR_1 = 3000;
  public static final int CLIMBTAKE_MOTOR_2 = 1300;
  // TODO: Change hopper ports to match robot
  public static final int TOP_HOPPER_MOTOR = 600;
  public static final int SPIN_FEEDER_MOTOR = 400;
  public static final int HOOD_MOTOR_A = 5;

  // Ports of Drivetrain
  public static final int FRONT_LEFT = 100;
  public static final int BACK_LEFT = 2000;
  public static final int FRONT_RIGHT = 1400;
  public static final int BACK_RIGHT = 1500;

  // Ports of Servos TODO: Fix Port to match robot servos
  public static final int GEAR_SERVO_LEFT = 700;
  public static final int GEAR_SERVO_RIGHT = 800;

  // Controllers
  public static final int OP_PAD = 0;
  public static final int DRIVER_PAD = 1;
}
