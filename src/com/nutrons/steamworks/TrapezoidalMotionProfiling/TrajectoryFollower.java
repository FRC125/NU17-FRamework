package com.nutrons.steamworks.TrapezoidalMotionProfiling;

import com.nutrons.steamworks.TrapezoidalMotionProfiling.*;


public class TrajectoryFollower {
  private double kv;
  private double ka;
  private double kp;
  private double kd;
  private double currentHeading;
  private double distanceSoFar;
  private int currentSegment;
  private double lastError;
  private Trajectory trajectory;
  private int size;

  public TrajectoryFollower(double kp, double kd, double kv, double ka, Trajectory toFollow) {
    this.kp = kp;
    this.kd = kd;
    this.kv = kv;
    this.ka = ka;
    this.trajectory = toFollow;
    size = trajectory.segments.length;
    currentHeading = 0;
    distanceSoFar = 0;
    lastError = 0;
  }

  public void reset() {
    lastError = 0.0;
    currentSegment = 0;
  }

  public double calculateOutput() {
    if (currentSegment < size) {
      Trajectory.Segment segment = trajectory.segments[currentSegment];
      double error = segment.pos - distanceSoFar;

      double output = kp * error + kd * ((error - lastError) / segment.dt - segment.vel) // Feed Back
          + (kv * segment.vel + ka * segment.acc); // Feed Foward
      lastError = error;
      currentHeading = segment.heading;
      currentSegment++;
      return output;
    }
    else { return 0; }
  }

  public double getHeading() {
    return currentHeading;
  }

}

