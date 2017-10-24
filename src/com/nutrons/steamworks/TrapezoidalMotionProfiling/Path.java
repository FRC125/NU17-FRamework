package com.nutrons.steamworks.TrapezoidalMotionProfiling;

import com.nutrons.steamworks.TrapezoidalMotionProfiling.*;

public class Path {
  private Trajectory.Pair trajectories;
  private String name;

  public Path(String name, Trajectory.Pair trajectories) {
    this.name = name;
    this.trajectories = trajectories;
  }

  public Path() {
    //left blank intentionally
  }

  public Trajectory getLeftTrajectory() {
    return trajectories.left;
  }

  public Trajectory getRightTrajectory() {
    return trajectories.right;
  }

  public String getName() {
    return name;
  }



}

