package com.libKudos254.vision;

/**
 * A container class for Targets detected by the vision system, containing the
 * location in three-dimensional space.
 */
public class TargetInfo {
  protected double horizantalAxis = 1.0;
  protected double verticalAxis;
  protected double depthAxis;

  public TargetInfo(double verticalAxis, double depthAxis) {
    this.verticalAxis = verticalAxis;
    this.depthAxis = depthAxis;
  }

  public double getX() {
    return horizantalAxis;
  }

  public double getY() {
    return verticalAxis;
  }

  public double getZ() {
    return depthAxis;
  }
}