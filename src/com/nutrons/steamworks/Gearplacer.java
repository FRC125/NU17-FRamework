package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.RevServo;
import com.nutrons.framework.controllers.ServoCommand;
import com.nutrons.framework.controllers.ServoInstr;
import io.reactivex.Flowable;

public class Gearplacer implements Subsystem {

  private static final boolean LEFT_INVERT = true;
  private static final boolean RIGHT_INVERT = false;
  private final Flowable<Boolean> openButton;
  private final RevServo gearPlacerLeft;
  private final RevServo gearPlacerRight;

  /**
   * The Gearplacer subsystem used for placing gears for BIG POINTS!
   *
   * @param gearPlacerLeft  The servo on the left side of the subsystem.
   * @param gearPlacerRight The servo on the right side of the subsystem.
   * @param openButton      The button used to open the servo(s).
   */
  public Gearplacer(RevServo gearPlacerLeft,
                    RevServo gearPlacerRight,
                    Flowable<Boolean> openButton) {
    this.gearPlacerLeft = gearPlacerLeft;
    this.gearPlacerRight = gearPlacerRight;
    this.openButton = openButton;
  }

  /**
   * Returns a command to move the servos.
   */
  public Command pulse() {
    return Command.just(x -> {
      gearPlacerLeft.accept(maxPosition(!LEFT_INVERT));
      gearPlacerRight.accept(maxPosition(!RIGHT_INVERT));
      return Flowable.just(() -> {
        gearPlacerLeft.accept(maxPosition(LEFT_INVERT));
        gearPlacerRight.accept(maxPosition(RIGHT_INVERT));
      });
    });
  }

  @Override
  public void registerSubscriptions() {
    openButton.map(x -> maxPosition(LEFT_INVERT ^ x))
        .subscribe(gearPlacerLeft);
    openButton.map(x -> maxPosition(RIGHT_INVERT ^ x))
        .subscribe(gearPlacerRight);
  }

  /**
   * Return a command to move to the max position either right or left.
   *
   * @param fullRight if true, max position to the right, if false, max position to the left
   */
  private ServoCommand maxPosition(boolean fullRight) {
    return fullRight ? ServoInstr.set(1.0) : ServoInstr.set(0.0);
  }
}
