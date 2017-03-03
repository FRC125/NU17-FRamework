package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.RevServo;

import com.nutrons.framework.controllers.ServoInstr;
import io.reactivex.Flowable;

public class Gearplacer implements Subsystem {

  private final Flowable<Boolean> openButton;
  private final RevServo gearPlacerLeft;
  private final RevServo gearPlacerRight;

  /**
   * The Gearplacer subsystem used for placing gears for BIG POINTS!
   *
   * @param gearPlacerLeft The servo on the left side of the subsystem.
   * @param gearPlacerRight The servo on the right side of the subsystem.
   * @param openButton The button used to open the servo(s).
   */
  public Gearplacer(RevServo gearPlacerLeft,
      RevServo gearPlacerRight,
      Flowable<Boolean> openButton) {
    this.gearPlacerLeft = gearPlacerLeft;
    this.gearPlacerRight = gearPlacerRight;
    this.openButton = openButton;

  }

  @Override
  public void registerSubscriptions() {
    openButton.map(x -> x ? ServoInstr.set(0.0) : ServoInstr.set(0.5))
        .subscribe(gearPlacerLeft);
    openButton.map(x -> x ? ServoInstr.set(1.0) : ServoInstr.set(0.5))
        .subscribe(gearPlacerRight);

  }
}
