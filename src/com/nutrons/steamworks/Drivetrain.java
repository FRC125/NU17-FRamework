package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import com.nutrons.framework.inputs.HeadingGyro;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.nutrons.framework.util.FlowOperators.toFlow;
import static io.reactivex.Flowable.combineLatest;
import static com.nutrons.framework.util.FlowOperators.deadbandMap;

public class Drivetrain implements Subsystem {
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final Consumer<ControllerEvent> leftDrive;
  private final Consumer<ControllerEvent> rightDrive;
  //private final Flowable<Command> holdHeading;
  private final HeadingGyro headingGyro;
  private final Flowable<Double> setpoint;
  private final Flowable<Double> gyroAngles;
  private final Flowable<Double> error;
  private double gyroSetpoint = 0.0;
  private final FlowingPID PIDControl;
  //private final Command holdHeadingCmd;
  //private final Command driveNormalCmd;
  private final double deadband = 0.3;

  /**
   * A drivetrain which uses Arcade Drive. AKA Cheezy Drive
   *
   * @param leftDrive  all controllers on the left of the drivetrain
   * @param rightDrive all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Double> throttle, Flowable<Double> yaw, Flowable<Boolean> holdHeading,
                    Consumer<ControllerEvent> leftDrive, Consumer<ControllerEvent> rightDrive) {

    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0)).subscribeOn(Schedulers.io()).onBackpressureDrop();
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0)).subscribeOn(Schedulers.io()).onBackpressureDrop();
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.headingGyro = new HeadingGyro();
    this.gyroAngles = headingGyro.getGyroReadings()
            .map(x -> {
              System.out.println(x + " = Gyro Readings");
              return x;
            });
    this.setpoint = toFlow(() -> getSetpoint()).subscribeOn(Schedulers.io());
    this.error = combineLatest(setpoint, gyroAngles, (x, y) -> x - y).subscribeOn(Schedulers.io()).onBackpressureDrop();
    this.PIDControl = new FlowingPID(error, 0.025, 0.0, 0.01);
    //this.holdHeadingCmd = Command.create(() -> holdHeadingAction());
    //this.driveNormalCmd = Command.create(() -> driveNormalAction());
    //this.holdHeading = holdHeading.map(x -> x ? holdHeadingCmd : driveNormalCmd); // Right Trigger
  }

  private synchronized double getSetpoint() {
    return gyroSetpoint;
  }

  private void setSetpoint (double setpoint) {
    this.gyroSetpoint = setpoint;
  }

  private void holdHeadingAction() {
    headingGyro.reset();
    setSetpoint(0.0);
    combineLatest(throttle, yaw, PIDControl.getOutput(), (x, y, z) -> x + y + z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power).subscribe(leftDrive);

    combineLatest(throttle, yaw, PIDControl.getOutput(), (x, y, z) -> x - y - z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(rightDrive);
  }
    private void driveNormalAction() {
    combineLatest(throttle, yaw, (x, y) -> x + y)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> {
              System.out.println(x);
              return x;
            })
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power).subscribe(leftDrive);
    combineLatest(throttle, yaw, (x, y) -> x - y)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(rightDrive);
  }

  @Override
  public void registerSubscriptions() {
    headingGyro.reset();
    setSetpoint(0.0);
    combineLatest(throttle, yaw, PIDControl.getOutput(), (x, y, z) -> x + y + z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power).subscribe(leftDrive);

    combineLatest(throttle, yaw, PIDControl.getOutput(), (x, y, z) -> x - y - z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(rightDrive);

  }
}
