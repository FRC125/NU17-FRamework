package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;

public class Feeder implements Subsystem {

  private final LoopSpeedController intakeController;

  public Feeder(LoopSpeedController intakeController) {
    this.intakeController = intakeController;
  }

  @Override
  public void registerSubscriptions() {
    intakeController.runAtPower(1.0);
  }
}
