package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.controllers.Talon;

public class Hopper implements Subsystem {

  // TODO: tune as needed
  private static final double SPIN_POWER = 0.9;
  private final Talon hopperMotor;
  
  public Hopper(Talon hopperMotor) {
    this.hopperMotor = hopperMotor;
  }

  @Override
  public void registerSubscriptions() {
    new RunAtPowerEvent(1.0).actOn(hopperMotor);
  }
}
