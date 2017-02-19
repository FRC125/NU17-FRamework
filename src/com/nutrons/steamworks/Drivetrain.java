package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static com.nutrons.framework.util.FlowOperators.*;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {
  private static final double FEET_PER_WHEEL_ROTATION = 0.851;
  private static final double WHEEL_ROTATION_PER_ENCODER_ROTATION = 52 / 42;
  private static final double FEET_PER_ENCODER_ROTATION =
      FEET_PER_WHEEL_ROTATION * WHEEL_ROTATION_PER_ENCODER_ROTATION;
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final LoopSpeedController leftDrive;
  private final LoopSpeedController rightDrive;
  private final Flowable<Double> error;
  private final Flowable<Double> output;
  private final double deadband = 0.3;
  private final Flowable<Boolean> teleHoldHeading;
  private final double ANGLE_P = 0.045;
  private final double ANGLE_I = 0.0;
  private final double ANGLE_D = 0.0065;
  private final int ANGLE_BUFFER_LENGTH = 10;
  private final double DRIVE_P = 0.0;
  private final double DRIVE_I = 0.0;
  private final double DRIVE_D = 0.0;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param teleHoldHeading    whether or not the drivetrain should maintain the target heading during teleop
   * @param currentHeading the current heading of the drivetrain
   * @param targetHeading  the target heading for the drivetrain to aquire
   * @param leftDrive      all controllers on the left of the drivetrain
   * @param rightDrive     all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Boolean> teleHoldHeading,
                    Flowable<Double> currentHeading, Flowable<Double> targetHeading,
                    Flowable<Double> throttle, Flowable<Double> yaw,
                    LoopSpeedController leftDrive, LoopSpeedController rightDrive) {

    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0));
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0));
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.error = combineLatest(targetHeading, currentHeading, (x, y) -> x - y);
    this.output = error
        .compose(pidLoop(ANGLE_P, ANGLE_BUFFER_LENGTH, ANGLE_I, ANGLE_D));
    this.teleHoldHeading = teleHoldHeading;
  }

  public Command driveTimeAction(long time) {
    Flowable<Double> move = toFlow(() -> 0.4);
    return Command.fromSubscription(() -> combineDisposable(move.map(x -> Events.power(x)).subscribe(leftDrive),
        move.map(x -> Events.power(-x)).subscribe(rightDrive))).killAfter(time, TimeUnit.MILLISECONDS).then(Command.fromAction(() -> {
      leftDrive.runAtPower(0);
      rightDrive.runAtPower(0);
    }));
  }

  /**
   * @param distance the distance to drive forward in feet
   */
  public Command driveDistanceAction(double distance) {
    return Command.just(() -> {
      leftDrive.setPID(DRIVE_P, DRIVE_I, DRIVE_D, 0.0);
      rightDrive.setPID(DRIVE_P, DRIVE_I, DRIVE_D, 0.0);
      ControllerEvent reset = Events.resetPosition(0);
      leftDrive.accept(reset);
      rightDrive.accept(reset);
      double setpoint = distance / FEET_PER_ENCODER_ROTATION;
      leftDrive.setSetpoint(setpoint);
      rightDrive.setSetpoint(setpoint);
      return Flowable.just(() -> {
        leftDrive.accept(reset);
        rightDrive.accept(reset);
        leftDrive.setSetpoint(0);
        rightDrive.setSetpoint(0);
        leftDrive.runAtPower(0);
        rightDrive.runAtPower(0);
      });
    });
  }

  public Command driveHoldHeading(Flowable<Double> left, Flowable<Double> right, Flowable<Boolean> holdHeading) {
    return Command.fromSubscription(() ->
        combineDisposable(
            combineLatest(left, output, holdHeading, (x, o, h) -> x - (h ? o : 0.0))
                .subscribeOn(Schedulers.io())
                .onBackpressureDrop()
                .compose(limitWithin(-1.0, 1.0))
                .map(Events::power)
                .subscribe(leftDrive),
            combineLatest(right, output, holdHeading, (x, o, h) -> x - (h ? o : 0.0))
                .subscribeOn(Schedulers.io())
                .onBackpressureDrop()
                .compose(limitWithin(-1.0, 1.0))
                .map(Events::power)
                .subscribe(rightDrive)));
  }

  public Command driveTeleop() {
    return driveHoldHeading(combineLatest(throttle, yaw, (x, y) -> x + y),
        combineLatest(throttle, yaw, (x, y) -> x - y), this.teleHoldHeading);
  }

  @Override
  public void registerSubscriptions() {
    driveHoldHeading(combineLatest(throttle, yaw, (x, y) -> x + y),
        combineLatest(throttle, yaw, (x, y) -> x - y),
        this.teleHoldHeading).startExecution();
  }
}
