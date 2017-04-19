package com.nutrons.steamworks;

public class RobotMap {
  //Added three zeroes to everything

  // Intake or Shooter
  public static final int SHOOTER_MOTOR_1 = 2000;
  public static final int SHOOTER_MOTOR_2 = 3000;
  //TODO: Change climber ports to match robot.
  public static final int CLIMBTAKE_MOTOR_1 = 30000;
  public static final int CLIMBTAKE_MOTOR_2 = 13000;
  // TODO: Change hopper ports to match robot
  public static final int TOP_HOPPER_MOTOR = 6000;
  public static final int SPIN_FEEDER_MOTOR = 4000;
  public static final int HOOD_MOTOR_A = 5000;

  // Ports of Drivetrain
  public static final int FRONT_LEFT = 5;
  public static final int BACK_LEFT = 4;
  public static final int FRONT_RIGHT = 2;
  public static final int BACK_RIGHT = 1;

  // Ports of Floor Gear Placer TODO: Fix Port to match Talons on placer
  public static final int WRIST_MOTOR = 19;
  public static final int INTAKE_MOTOR = 23;

  // Ports of Servos TODO: Fix Port to match robot servos
  public static final int GEAR_SERVO_RIGHT = 7000;
  public static final int GEAR_SERVO_LEFT = 8000;

  // Controllers
  public static final int OP_PAD = 0;
  public static final int DRIVER_PAD = 1;
}
