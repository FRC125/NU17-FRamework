package com.nutrons.libKudos254.vision;

/**
 * A container class for Targets detected by the vision system, containing the
 * location in three-dimensional space.
 */
public class TargetInfo {
  protected double xPos = 1.0;
  protected double yPos;
  protected double zPos;

  public TargetInfo(double yPos, double zPos) {
    this.yPos = yPos;
    this.zPos = zPos;
  }

  public double getX() {
    return xPos;
  }

  public double getY() {
    return yPos;
  }

  public double getZ() {
    return zPos;
  }
}