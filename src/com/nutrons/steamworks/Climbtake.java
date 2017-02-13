package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

public class Climbtake implements Subsystem {
  private static final double INTAKE_SPEED = -1.0;
  private static final double CLIMBER_SPEED = 1.0;
  private final LoopSpeedController intakeController;
  private final LoopSpeedController climberController;
  private final Flowable<Boolean> forward;
  private final Flowable<Boolean> reverse;

  public Climbtake(LoopSpeedController intakeController, LoopSpeedController climberController,
                   Flowable<Boolean> forward, Flowable<Boolean> reverse) {
    this.intakeController = intakeController;
    this.climberController = climberController;
    this.forward = forward;
    this.reverse = reverse;
  }

  @Override
  public void registerSubscriptions() {
    forward.map(b -> b ? INTAKE_SPEED : 0.0).map(Events::power).subscribe(intakeController);
    forward.map(b -> b ? CLIMBER_SPEED : 0.0).map(Events::power).subscribe(climberController);

    reverse.map(b -> b ? -INTAKE_SPEED : 0.0).map(Events::power).subscribe(intakeController);
    reverse.map(b -> b ? -CLIMBER_SPEED : 0.0).map(Events::power).subscribe(climberController);
  }
}

