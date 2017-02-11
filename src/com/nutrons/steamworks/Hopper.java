package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.controllers.Talon;

public class Hopper implements Subsystem {

  // TODO: tune as needed
  private static final double SPIN_POWER = 0.9;
  private final Talon hopper;
  
  public Hopper(Talon hopper) {
    this.hopper = hopper;
  }

  @Override
  public void registerSubscriptions() {
    new RunAtPowerEvent(SPIN_POWER).actOn(hopper);
  }
}
