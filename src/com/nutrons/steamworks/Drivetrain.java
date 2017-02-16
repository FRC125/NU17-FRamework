package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.nutrons.framework.util.FlowOperators.deadbandMap;
import static com.nutrons.framework.util.FlowOperators.pidLoop;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final Consumer<ControllerEvent> leftDrive;
  private final Consumer<ControllerEvent> rightDrive;

  private final Flowable<Double> error;
  private final Flowable<Double> output;
  private final double deadband = 0.3;
  private final Flowable<Boolean> holdHeading;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param holdHeading    whether or not the drivetrain should maintain the target heading
   * @param currentHeading the current heading of the drivetrain
   * @param targetHeading  the target heading for the drivetrain to aquire
   * @param leftDrive      all controllers on the left of the drivetrain
   * @param rightDrive     all controllers on the right of the drivetrain
   */

  public Drivetrain(Flowable<Boolean> holdHeading,
                    Flowable<Double> currentHeading, Flowable<Double> targetHeading,
                    Flowable<Double> throttle, Flowable<Double> yaw,
                    Consumer<ControllerEvent> leftDrive, Consumer<ControllerEvent> rightDrive) {

    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0));
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0));
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.error = combineLatest(targetHeading, currentHeading, (x, y) -> x - y);
    this.output = error
        .compose(pidLoop(0.045, 10, 0.0, 0.0065));
    this.holdHeading = holdHeading;
  }

  @Override
  public void registerSubscriptions() {
    combineLatest(throttle, yaw, output, holdHeading, (x, y, z, h) -> x + y + (h ? z : 0.0))
        .subscribeOn(Schedulers.io())
        .onBackpressureDrop()
        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
        .map(Events::power)
        .subscribe(leftDrive);

    combineLatest(throttle, yaw, output, holdHeading, (x, y, z, h) -> x - y + (h ? z : 0.0))
        .subscribeOn(Schedulers.io())
        .onBackpressureDrop()
        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
        .map(Events::power)
        .subscribe(rightDrive);
  }
}
