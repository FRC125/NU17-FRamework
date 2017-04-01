package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

public class Feeder implements Subsystem {

  // TODO: tune as needed
  private static final double SPIN_POWER = .80; //.6
  private static final double ROLLER_POWER = -1.0; //.85
  private final LoopSpeedController feederController;
  private final LoopSpeedController rollerController;
  private final Flowable<Boolean> feederButton;
  private final Flowable<Boolean> agitatorButton;

  /**
   * The feeder hopper used for primarily feeding balls to the shooter.
   *
   * @param feederController The controller responsible for the control of the feeder.
   * @param rollerController The controller responsible for the control of the roller.
   * @param feederButton     The button mapped to running the hopper system.
   */
  public Feeder(LoopSpeedController feederController,
                LoopSpeedController rollerController,
                Flowable<Boolean> feederButton,
                Flowable<Boolean> agitatorButton) {
    this.feederController = feederController;
    this.rollerController = rollerController;
    this.feederButton = feederButton;
    this.agitatorButton = agitatorButton;
  }

  /**
   * When started, feeder is engaged. When terminated, it is disengaged.
   */
  public Command pulse() {
    return Command.just(x -> {
      feederController.runAtPower(SPIN_POWER);
      rollerController.runAtPower(ROLLER_POWER);
      return Flowable.just(() -> {
        feederController.runAtPower(0);
        rollerController.runAtPower(0);
      });
    });
  }

  public Command agitate(){
    return Command.just(x -> {
      feederController.runAtPower(SPIN_POWER);
      rollerController.runAtPower(-ROLLER_POWER);
      return Flowable.just(() -> {
              feederController.runAtPower(0);
              rollerController.runAtPower(0);
            });
          });
    }

  @Override
  public void registerSubscriptions() {
    feederButton.map(b -> b ? SPIN_POWER : 0.0).map(Events::power).subscribe(feederController);
    feederButton.map(b -> b ? ROLLER_POWER : 0.0).map(Events::power).subscribe(rollerController);

    this.agitatorButton.filter(x -> x).map(x -> agitate().terminable(agitatorButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));
  }
}