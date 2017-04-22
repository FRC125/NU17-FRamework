package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Tuneable;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;

public class Climbtake implements Subsystem {

  private static final double CLIMBTAKE_SPEED_LEFT = 1.0;
  private static final double CLIMBTAKE_SPEED_RIGHT = -1.0;
  private final LoopSpeedController climbtakeControllerLeft;
  private final LoopSpeedController climbtakeControllerRight;
  private final Flowable<Boolean> forward;
  private final Flowable<Boolean> reverse;
  private Tuneable climbMotorLeft = new Tuneable("climbMotorLeft", CLIMBTAKE_SPEED_LEFT);
  private Tuneable climbMotorRight = new Tuneable("climbrMotorRight", CLIMBTAKE_SPEED_RIGHT);

  /**
   * Climber and Intake subsystem, used for boarding the airship and intaking fuel.
   *
   * @param climbtakeControllerLeft  Talon on left side of the climbtake.
   * @param climbtakeControllerRight Talon on the right side of the climbtake.
   * @param forward                  Button used for setting direction to forward.
   * @param reverse                  Button used for setting direction to backward..
   */
  public Climbtake(LoopSpeedController climbtakeControllerLeft,
                   LoopSpeedController climbtakeControllerRight,
                   Flowable<Boolean> forward,
                   Flowable<Boolean> reverse) {
    this.climbtakeControllerLeft = climbtakeControllerLeft;
    this.climbtakeControllerRight = climbtakeControllerRight;
    this.forward = forward;
    this.reverse = reverse;
  }

  /**
   * When started, climbtake is engaged. When terminated, it is disengaged.
   *
   * @param direction true for ball-intake, false for climb-intake
   */
  public Command pulse(boolean direction) {
    return Command.just(x -> {
      double sign = direction ? -1.0 : 1.0;
      climbtakeControllerLeft.runAtPower(sign * CLIMBTAKE_SPEED_LEFT);
      climbtakeControllerRight.runAtPower(sign * CLIMBTAKE_SPEED_RIGHT);
      return Flowable.just(() -> {
        climbtakeControllerLeft.runAtPower(0);
        climbtakeControllerRight.runAtPower(0);
      });
    });
  }

  @Override
  public void registerSubscriptions() {
    forward.map(b -> b ? CLIMBTAKE_SPEED_LEFT : 0.0)
        .map(Events::power)
        .subscribe(climbtakeControllerLeft);
    forward.map(b -> b ? CLIMBTAKE_SPEED_RIGHT : 0.0)
        .map(Events::power)
        .subscribe(climbtakeControllerRight);

    reverse.map(b -> b ? -CLIMBTAKE_SPEED_LEFT : 0.0)
        .map(Events::power)
        .subscribe(climbtakeControllerLeft);
    reverse.map(b -> b ? -CLIMBTAKE_SPEED_RIGHT : 0.0)
        .map(Events::power)
        .subscribe(climbtakeControllerRight);
    FlowOperators.toFlow(climbMotorLeft::get).subscribe(System.out::println);
    FlowOperators.toFlow(climbMotorRight::get).subscribe(System.out::println);
  }
}

