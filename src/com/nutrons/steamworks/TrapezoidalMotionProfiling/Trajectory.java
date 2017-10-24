package com.nutrons.steamworks.TrapezoidalMotionProfiling;

/**
 * A deserialized followable trapezoidal motion profile.
 */
public class Trajectory {

  public static class Pair {
    public Pair(Trajectory left, Trajectory right) {
      this.left = left;
      this.right = right;
    }

    public Trajectory left;
    public Trajectory right;
  }

  public static class Segment {

    public double pos, vel, acc, jerk, heading, dt, x, y;

    public Segment() {
    }

    public Segment(double pos, double vel, double acc, double jerk,
        double heading, double dt, double x, double y) {
      this.pos = pos;
      this.vel = vel;
      this.acc = acc;
      this.jerk = jerk;
      this.heading = heading;
      this.dt = dt;
      this.x = x;
      this.y = y;
    }

    public String toString() {
      return "pos: " + pos + "; vel: " + vel + "; acc: " + acc + "; jerk: "
          + jerk + "; heading: " + heading;
    }

  }

  public Segment[] segments;

  public Trajectory(int length) {
    segments = new Segment[length];
    for (int i = 0; i < length; ++i) {
      segments[i] = new Segment();
    }
  }

  public int getNumSegments() {
    return segments.length;
  }

  public void setSegment(int index, Segment segment) {
    if (index < getNumSegments()) {
      segments[index] = segment;
    }
    else {
      System.out.println("Invalid Index!");
    }
  }
}

