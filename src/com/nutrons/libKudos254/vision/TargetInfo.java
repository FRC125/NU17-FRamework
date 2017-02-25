package com.nutrons.libKudos254.vision;

/**
 * A container class for Targets detected by the vision system, containing the
 * location in three-dimensional space.
 */
public class TargetInfo {
  protected double posX = 1.0;
  protected double posY;
  protected double posZ;

  public TargetInfo(double posY, double posZ) {
    this.posY = posY;
    this.posZ = posZ;
  }

  public double getPosX() {
    return posX;
  }

  public double getPosY() {
    return posY;
  }

  public double getPosZ() {
    return posZ;
  }
}