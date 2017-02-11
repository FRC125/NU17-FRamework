package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.deadbandMap;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final LoopSpeedController leftDrive;
  private final LoopSpeedController rightDrive;
  private final double deadband = 0.2;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param leftDrive  all controllers on the left of the drivetrain
   * @param rightDrive all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Double> throttle,
                    Flowable<Double> yaw,
                    LoopSpeedController leftDrive,
                    LoopSpeedController rightDrive) {

    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0));
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0));
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
  }

  @Override
  public void registerSubscriptions() {
    combineLatest(throttle, yaw, (x, y) -> x + y)
        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
        .map(Events::power).subscribe(leftDrive);
    combineLatest(throttle, yaw, (x, y) -> x - y)
        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
        .map(Events::power)
        .subscribe(rightDrive);
  }
}
