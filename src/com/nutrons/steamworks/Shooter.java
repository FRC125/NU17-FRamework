package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;


public class Shooter implements Subsystem {

  private final LoopSpeedController shooterController;

  public Shooter(LoopSpeedController shooterController) {
    this.shooterController = shooterController;
  }

  @Override
  public void registerSubscriptions() {
    shooterController.setPID(0.05, 0.0, 0.33, 0.035);
    shooterController.setSetpoint(2950.0);
  }
}
