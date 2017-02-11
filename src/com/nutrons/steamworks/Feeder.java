package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.controllers.Talon;

public class Feeder implements Subsystem {

  private final Talon intakeMotor;

  public Feeder(Talon intakeMotor) {
    this.intakeMotor = intakeMotor;
  }

  @Override
  public void registerSubscriptions() {
    new RunAtPowerEvent(1.0).actOn(intakeMotor);
  }
}