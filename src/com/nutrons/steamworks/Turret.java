package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;

public class Turret implements Subsystem {
  private final Flowable<Double> angle;
  private final LoopSpeedController hoodMaster;

  private volatile double motorRotation;
  private static final double HOOD_RADIUS_IN = 10.5;

  public Turret(Flowable<Double> angle, LoopSpeedController master) {
    this.angle = angle;
    this.hoodMaster = master;
    this.angle.map(x -> x / 360.0).subscribe(x -> this.motorRotation = x); // need to find out how many rotations of the motor to turn around turret 360 degrees
    //Calculates arc length turret needs to travel to reach a certain angle,
    //Finds ratio of angle to 360 and creates a proportion to ratio with arc length to full circumference
    this.hoodMaster.resetPosition();
  }

  @Override
  public void registerSubscriptions() {
    Flowable<ControllerEvent> source =
        toFlow(() -> Events.pid(hoodMaster.position() + this.motorRotation,
            0.03, 0.0, 0.0, 0.0));
    source.subscribe(hoodMaster);
  }
}
