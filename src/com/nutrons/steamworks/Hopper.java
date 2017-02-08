package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class Hopper implements Subsystem {

  // TODO: tune as needed
  private static final double SPIN_POWER = 0.9;
  private final Flowable<ControllerEvent> runHopper;
  private final Consumer<ControllerEvent> hopperController;

  /**
   * @param hopperController passes in RunAtPowerEvent.
   */
  public Hopper(Consumer<ControllerEvent> hopperController) {
    this.runHopper = Flowable.just(Events.power(SPIN_POWER));
    this.hopperController = hopperController;
  }

  @Override
  public void registerSubscriptions() {
    runHopper.subscribe(hopperController);
  }
}
