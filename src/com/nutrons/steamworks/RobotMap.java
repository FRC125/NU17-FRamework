package com.nutrons.steamworks;

public class RobotMap {
  // Intake or Shooter
  public static final int INTAKE_MOTOR = 0; // 4
  public static final int SHOOTER_MOTOR_1 = 0; // 2
  public static final int SHOOTER_MOTOR_2 = 0; // 3
  // TODO: Change hopper ports to match robot
  public static final int TOP_HOPPER_MOTOR = 0;
  public static final int SPIN_HOPPER_MOTOR = 0;
  public static final int HOOD_MOTOR_A = 0; // 3

  // Ports of wheels TODO: Fix ports to match robot motors
  public static final int FRONT_LEFT = 4; // EH = 1 EV = 4
  public static final int BACK_LEFT = 5; // EH = 20 EV = 5
  public static final int FRONT_RIGHT = 1; // EH = 14 EV = 1
  public static final int BACK_RIGHT = 2;  // EH = 15 EV = 2

  // Controllers
  public static final int DRIVER_PAD = 1;
  public static final int OP_PAD = 0;
}
