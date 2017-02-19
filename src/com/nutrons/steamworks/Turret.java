package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import io.reactivex.Flowable;

public class Turret implements Subsystem {

  private static final double PVAL = 0.03;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.0;
  private static final double FVAL = 0.0;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;
  private final Flowable<Double> angle;
  private final Talon hoodMaster;

  /**
   * The Turret System that is used for aiming our shooter.
   * @param angle The flowable of doubles that is represent the angle the turret should be facing.
   * @param master The talon controlling the movement of the turret.
   */
  public Turret(Flowable<Double> angle, Talon master) {
    this.angle = angle;
    this.hoodMaster = master;
    Events.resetPosition(0.0).actOn(this.hoodMaster);
  }

  @Override
  public void registerSubscriptions() {
    Flowable<ControllerEvent> source = this.angle
        .map(x -> x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS / 360.0)
        .map(x -> Events.pid(hoodMaster.position() + x, PVAL, IVAL, DVAL, FVAL));

    source.subscribe(hoodMaster);
  }
}
