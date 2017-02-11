package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;

public class Feeder implements Subsystem {

  // TODO: tune as needed
  private static final double SPIN_POWER = 0.9;
  private final LoopSpeedController feederController;
  
  public Feeder(LoopSpeedController feederController) {
    this.feederController = feederController;
  }

  @Override
  public void registerSubscriptions() {
    feederController.runAtPower(SPIN_POWER);
  }
}
