package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static com.nutrons.framework.util.FlowOperators.*;
import static io.reactivex.Flowable.combineLatest;
import static java.lang.Math.abs;

public class Drivetrain implements Subsystem {
  private static final double FEET_PER_WHEEL_ROTATION = 0.851;
  private static final double WHEEL_ROTATION_PER_ENCODER_ROTATION = 52 / 42;
  private static final double FEET_PER_ENCODER_ROTATION =
      FEET_PER_WHEEL_ROTATION * WHEEL_ROTATION_PER_ENCODER_ROTATION;
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final LoopSpeedController leftDrive;
  private final LoopSpeedController rightDrive;
  private final double deadband = 0.3;
  private final Flowable<Boolean> teleHoldHeading;
  private final double ANGLE_P = 0.045;
  private final double ANGLE_I = 0.0;
  private final double ANGLE_D = 0.0065;
  private final int ANGLE_BUFFER_LENGTH = 10;
  private final ConnectableFlowable<Double> currentHeading;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param teleHoldHeading whether or not the drivetrain should maintain the target heading during teleop
   * @param currentHeading  the current heading of the drivetrain
   * @param leftDrive       all controllers on the left of the drivetrain
   * @param rightDrive      all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Boolean> teleHoldHeading, Flowable<Double> currentHeading,
                    Flowable<Double> throttle, Flowable<Double> yaw,
                    LoopSpeedController leftDrive, LoopSpeedController rightDrive) {
    this.currentHeading = currentHeading.publish();
    this.currentHeading.connect();
    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0)).onBackpressureDrop();
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0)).onBackpressureDrop();
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.teleHoldHeading = teleHoldHeading;
  }

  public Command turn(double angle, double tolerance) {
    return driveHoldHeading(Flowable.just(0.0), Flowable.just(0.0), Flowable.just(true),
        currentHeading.take(1).map(x -> x + angle))
        .terminable(currentHeading.filter(x -> abs(x) < tolerance));
  }

  public Command driveTimeAction(long time) {
    Flowable<Double> move = toFlow(() -> 0.4);
    return Command.fromSubscription(() ->
        combineDisposable(
            move.map(x -> Events.power(x)).subscribe(leftDrive),
            move.map(x -> Events.power(-x)).subscribe(rightDrive)
        )
    ).killAfter(time, TimeUnit.MILLISECONDS).then(Command.fromAction(() -> {
      leftDrive.runAtPower(0);
      rightDrive.runAtPower(0);
    }));
  }

  /**
   * Drive the robot a distance, using the gyro to hold the current heading.
   *
   * @param distance  the distance to drive forward in feet
   * @param tolerance the command will stop once the distance is within the tolerance distance range
   * @param speed     the controller's output speed
   */
  public Command driveDistanceAction(double distance, double tolerance, double speed) {
    ControllerEvent reset = Events.resetPosition(0);
    double setpoint = distance / FEET_PER_ENCODER_ROTATION;
    Command resetRight = Command.just(() -> {
      rightDrive.accept(reset);
      return Flowable.just(() -> {
        rightDrive.accept(reset);
        rightDrive.setSetpoint(0);
        rightDrive.runAtPower(0);
      });
    });
    Flowable<Double> drive = toFlow(() -> speed);
    return Command.parallel(resetRight,
        driveHoldHeading(drive, drive, Flowable.just(true), currentHeading.take(1)))
        .killAfter(4000, TimeUnit.MILLISECONDS);
  }

  public Command driveHoldHeading(Flowable<Double> left, Flowable<Double> right, Flowable<Boolean> holdHeading, Flowable<Double> targetHeading) {
    return Command.fromSubscription(() -> {
      Flowable<Double> output = combineLatest(targetHeading, currentHeading, (x, y) -> x - y).onBackpressureDrop()
          .compose(pidLoop(ANGLE_P, ANGLE_BUFFER_LENGTH, ANGLE_I, ANGLE_D));
      return combineDisposable(
          combineLatest(left, output, holdHeading, (x, o, h) -> x - (h ? o : 0.0))
              .subscribeOn(Schedulers.io())
              .onBackpressureDrop()
              .compose(limitWithin(-1.0, 1.0))
              .map(Events::power)
              .subscribe(leftDrive),
          combineLatest(right, output, holdHeading, (x, o, h) -> x + (h ? o : 0.0))
              .subscribeOn(Schedulers.io())
              .onBackpressureDrop()
              .compose(limitWithin(-1.0, 1.0))
              .map(x -> Events.power(-x))
              .subscribe(rightDrive));
    })
        .addFinalTerminator(() -> {
          leftDrive.runAtPower(0);
          leftDrive.runAtPower(0);
        });
  }

  public Command driveHoldHeading(Flowable<Double> left, Flowable<Double> right, Flowable<Boolean> holdHeading) {
    return driveHoldHeading(left, right, holdHeading, Flowable.just(0.0).mergeWith(
        holdHeading.filter(x -> x).withLatestFrom(currentHeading, (x, y) -> y)));
  }

  public Command driveTeleop() {
    return driveHoldHeading(
        combineLatest(throttle, yaw, (x, y) -> x + y).onBackpressureDrop(),
        combineLatest(throttle, yaw, (x, y) -> x - y).onBackpressureDrop(), Flowable.just(false).concatWith(this.teleHoldHeading));
  }

  @Override
  public void registerSubscriptions() {

  }
}
