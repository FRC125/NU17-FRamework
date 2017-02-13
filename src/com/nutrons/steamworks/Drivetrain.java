package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

import static com.nutrons.framework.util.FlowOperators.deadband;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final Consumer<ControllerEvent> leftDrive;
  private final Consumer<ControllerEvent> rightDrive;
  private final Flowable<Boolean> flipHeading;
  private double flip;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param leftDrive  all controllers on the left of the drivetrain
   * @param rightDrive all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Double> throttle,
                    Flowable<Double> yaw,
                    Consumer<ControllerEvent> leftDrive,
                    Consumer<ControllerEvent> rightDrive,
                    Flowable<Boolean> flipHeading) {
    this.throttle = deadband(throttle);
    this.yaw = deadband(yaw);
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.flipHeading = flipHeading;
    this.flip = 1;
  }

  @Override
  public void registerSubscriptions() {
    flip = FlowOperators.getLastValue(flipHeading.map(b -> b ? 1.0 : -1.0));
    FlowOperators.deadband(combineLatest(throttle, yaw, (x, y) -> -(x * flip + y) / 2)).map(Events::power).subscribe(leftDrive);
    FlowOperators.deadband(combineLatest(throttle, yaw, (x, y) -> -(x * flip - y) / 2)).map(Events::power).subscribe(rightDrive);
  }
}
