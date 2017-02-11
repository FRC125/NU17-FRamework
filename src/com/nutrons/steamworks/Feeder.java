package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.controllers.Talon;

public class Feeder implements Subsystem {

  private final Talon intake;
  private static final double MOTOR_POWER = 1.0;

  public Feeder(Talon intake) {
    this.intake = intake;
  }

  @Override
  public void registerSubscriptions() {
    new RunAtPowerEvent(MOTOR_POWER).actOn(intake);
  }
}
