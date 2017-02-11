package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Talon;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;

public class Turret implements Subsystem {
  private final Flowable<Double> angle;
  private final Talon hoodMaster;

  private volatile double motorRotation;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;

  public Turret(Flowable<Double> angle, Talon master) {
    this.angle = angle;
    this.hoodMaster = master;
    this.angle.map(x -> x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS / 360.0).subscribe(x -> this.motorRotation = x);
    Events.resetPosition(0.0).actOn(this.hoodMaster);
  }

  @Override
  public void registerSubscriptions() {
    Flowable<ControllerEvent> source =
        toFlow(() -> Events.pid(hoodMaster.position() + this.motorRotation,
            0.03, 0.0, 0.0, 0.0));
    source.subscribe(hoodMaster);
  }
}
