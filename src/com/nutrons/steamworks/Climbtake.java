package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

public class Climbtake implements Subsystem {
  private static final double CLIMBTAKE_SPEED_LEFT = 1.0;
  private static final double CLIMBTAKE_SPEED_RIGHT =
      -1.0;
  private final LoopSpeedController climbtakeControllerLeft;
  private final LoopSpeedController climbtakeControllerRight;
  private final Flowable<Boolean> forward;
  private final Flowable<Boolean> reverse;

  public Climbtake(LoopSpeedController climbtakeControllerLeft, LoopSpeedController climbtakeControllerRight,
                   Flowable<Boolean> forward, Flowable<Boolean> reverse) {
    this.climbtakeControllerLeft = climbtakeControllerLeft;
    this.climbtakeControllerRight = climbtakeControllerRight;
    this.forward = forward;
    this.reverse = reverse;
  }

  @Override
  public void registerSubscriptions() {
    forward.map(b -> b ? CLIMBTAKE_SPEED_LEFT : 0.0).map(Events::power).subscribe(climbtakeControllerLeft);
    forward.map(b -> b ? CLIMBTAKE_SPEED_RIGHT : 0.0).map(Events::power).subscribe(climbtakeControllerRight);

    reverse.map(b -> b ? -CLIMBTAKE_SPEED_LEFT : 0.0).map(Events::power).subscribe(climbtakeControllerLeft);
    reverse.map(b -> b ? -CLIMBTAKE_SPEED_RIGHT : 0.0).map(Events::power).subscribe(climbtakeControllerRight);
  }
}

