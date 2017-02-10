package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.deadband;
import static io.reactivex.Flowable.combineLatest;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class Drivetrain implements Subsystem {
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final Consumer<ControllerEvent> leftDrive;
  private final Consumer<ControllerEvent> rightDrive;
  private double coeff = 1.0;

  // Does cheezy drive.
  public Drivetrain(Flowable<Double> throttle, Flowable<Double> yaw,
                    Consumer<ControllerEvent> leftDrive, Consumer<ControllerEvent> rightDrive) {
    this.throttle = deadband(throttle);
    this.yaw = deadband(yaw);
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
  }

  @Override
  public void registerSubscriptions() {
    combineLatest(throttle, yaw, (x, y) -> x + y).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(leftDrive);
    combineLatest(throttle, yaw, (x, y) -> x - y).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(rightDrive);
  }
}
