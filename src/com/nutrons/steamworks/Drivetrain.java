package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.combineDisposable;
import static com.nutrons.framework.util.FlowOperators.deadbandMap;
import static com.nutrons.framework.util.FlowOperators.limitWithin;
import static com.nutrons.framework.util.FlowOperators.pidLoop;
import static com.nutrons.framework.util.FlowOperators.toFlow;
import static io.reactivex.Flowable.combineLatest;
import static java.lang.Math.abs;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.commands.Terminator;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.TimeUnit;

public class Drivetrain implements Subsystem {
  private static final double FEET_PER_WHEEL_ROTATION = 0.851;
  private static final double WHEEL_ROTATION_PER_ENCODER_ROTATION = 42.0 / 54.0;
  private static final double FEET_PER_ENCODER_ROTATION =
      FEET_PER_WHEEL_ROTATION * WHEEL_ROTATION_PER_ENCODER_ROTATION;
  // PID for turning to an angle based on the gyro
  private static final double ANGLE_P = 0.09;
  private static final double ANGLE_I = 0.0;
  private static final double ANGLE_D = 0.035;
  private static final int ANGLE_BUFFER_LENGTH = 5;
  // PID for distance driving based on encoders
  private static final double DISTANCE_P = 0.08;
  private static final double DISTANCE_I = 0.0;
  private static final double DISTANCE_D = 0.0;
  private static final int DISTANCE_BUFFER_LENGTH = 5;
  // Time required to spend within the PID tolerance for the PID loop to terminate
  private static final TimeUnit PID_TERMINATE_UNIT = TimeUnit.MILLISECONDS;
  private static final long PID_TERMINATE_TIME = 500;
  private static final double DEADBAND = 0.3;
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final LoopSpeedController leftDrive;
  private final LoopSpeedController rightDrive;
  private final Flowable<Boolean> teleHoldHeading;
  private final ConnectableFlowable<Double> currentHeading;

  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param teleHoldHeading whether or not the drivetrain should maintain the target heading during
   *                        teleop
   * @param currentHeading  the current heading of the drivetrain
   * @param leftDrive       all controllers on the left of the drivetrain
   * @param rightDrive      all controllers on the right of the drivetrain
   */
  public Drivetrain(Flowable<Boolean> teleHoldHeading, Flowable<Double> currentHeading,
                    Flowable<Double> throttle, Flowable<Double> yaw,
                    LoopSpeedController leftDrive, LoopSpeedController rightDrive) {
    this.currentHeading = currentHeading.publish();
    this.currentHeading.connect();
    this.throttle = throttle.map(deadbandMap(-DEADBAND, DEADBAND, 0.0)).onBackpressureDrop();
    this.yaw = yaw.map(deadbandMap(-DEADBAND, DEADBAND, 0.0)).onBackpressureDrop();
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.teleHoldHeading = teleHoldHeading;
  }

  /**
   * A stream which will emit an item once the error stream is within
   * the tolerance for an acceptable amount of time, as determined by the constants.
   * Intended use is to terminate a PID loop command.
   */
  private Flowable<?> pidTerminator(Flowable<Double> error, double tolerance) {
    return pidTerminator(error, tolerance, PID_TERMINATE_TIME, PID_TERMINATE_UNIT);
  }

  private Flowable<?> pidTerminator(Flowable<Double> error, double tolerance, long delay, TimeUnit unit) {
    return error.map(x -> abs(x) < tolerance)
        .distinctUntilChanged().debounce(delay, unit)
        .filter(x -> x);
  }

  /**
   * A command that turns the Drivetrain by a given angle, stopping after the angle remains
   * within the specified tolerance for a certain period of time.
   * The command will abort after a certain period of time, to avoid
   * remaining stuck due to an imperfect turn.
   *
   * @param angle     angle the robot should turn, positive is CW, negative is CCW
   * @param tolerance the robot should attempt to remain within this error of the target
   */
  public Command turn(double angle, double tolerance) {
    Flowable<Double> targetHeading = currentHeading.map(y -> y + angle).take(1);
    // Sets the targetHeading to the sum of one currentHeading value, with angle added to it.
    Flowable<Double> error = currentHeading.withLatestFrom(targetHeading, (y, z) -> y - z).publish().autoConnect();
    Flowable<?> terminator = pidTerminator(error, tolerance);
    return Command.just(x -> {
      // driveHoldHeading, with 0.0 ideal left and right speed, to turn in place.
      Flowable<? extends Terminator> terms = driveHoldHeading(Flowable.just(0.0), Flowable.just(0.0),
          Flowable.just(true), targetHeading)
          // Makes sure the final terminator will stop the motors
          .addFinalTerminator(() -> {
            leftDrive.runAtPower(0);
            rightDrive.runAtPower(0);
          })
          // Terminate when the error is below the tolerance for long enough
          .terminable(terminator)
          .execute(x);
      return terms;
      // Ensure we do not spend too long attempting to turn
    }).endsWhen(Flowable.timer(1500, TimeUnit.MILLISECONDS), true);
  }

  /**
   * Drive the robot until a certain distance is reached,
   * while using the gyro to hold the current heading.
   *
   * @param distance          the distance to drive forward in feet,
   *                          (negative values drive backwards)
   * @param distanceTolerance the tolerance for distance error, which is based on encoder values;
   *                          this error is based on encoder readings.
   * @param angleTolerance    the tolerance for angle error in a sucessful PID loop;
   *                          this error is based on gyro readings.
   */
  public Command driveDistance(double distance,
                               double distanceTolerance,
                               double angleTolerance) {
    // Get the current heading at the beginning
    Flowable<Double> targetHeading = currentHeading.take(1);
    ControllerEvent reset = Events.resetPosition(0);
    double setpoint = distance / FEET_PER_ENCODER_ROTATION;

    // Construct closed-loop streams for distance / encoder based PID
    Flowable<Double> distanceError = toFlow(() ->
        (rightDrive.position() + leftDrive.position()) / 2.0 - setpoint);
    Flowable<Double> distanceOutput = distanceError
        .compose(pidLoop(DISTANCE_P, DISTANCE_BUFFER_LENGTH, DISTANCE_I, DISTANCE_D));

    // Construct closed-loop streams for angle / gyro based PID
    Flowable<Double> angleError = combineLatest(targetHeading, currentHeading, (x, y) -> x - y).onBackpressureDrop().publish().autoConnect();
    Flowable<Double> angleOutput = pidAngle(targetHeading);

    Flowable<ControllerEvent> rightSource = combineLatest(distanceOutput, angleOutput, (x, y) -> x + y).publish().autoConnect().onBackpressureDrop().map(limitWithin(-1.0, 1.0)).map(Events::power);
    Flowable<ControllerEvent> leftSource = combineLatest(distanceOutput, angleOutput, (x, y) -> x - y).publish().autoConnect().onBackpressureDrop()
        .map(limitWithin(-1.0, 1.0)).map(x -> -x).map(Events::power);
    // Create commands for each motor
    Command right = Command.fromSubscription(() -> rightSource.subscribe(rightDrive));
    Command left = Command.fromSubscription(() -> leftSource.subscribe(leftDrive));
    Flowable<Double> noDrive = Flowable.just(0.0);

    Flowable<?> distanceTerminator = pidTerminator(distanceError,
        distanceTolerance / WHEEL_ROTATION_PER_ENCODER_ROTATION, 100, TimeUnit.MILLISECONDS);
    Flowable<?> angleTerminator = pidTerminator(angleError, angleTolerance, 200, TimeUnit.MILLISECONDS);
    // Chaining all the commands together
    return Command.fromAction(() -> {
      rightDrive.accept(reset);
      leftDrive.accept(reset);
    }).then(Command.parallel(right, left))
        // Terminates the distance PID when within acceptable error
        .terminable(distanceTerminator)
        // Turn to the targetHeading afterwards, and stop PID when within acceptable error
        .then(driveHoldHeading(noDrive, noDrive, Flowable.just(true), targetHeading)
            .terminable(angleTerminator)
            // Afterwards, stop the motors
            .then(Command.fromAction(() -> {
              leftDrive.runAtPower(0);
              rightDrive.runAtPower(0);
            })));
  }

  /**
   * Drive the robot, and attempt to retain the desired heading with the gyro.
   *
   * @param left          the ideal power of the left motors
   * @param right         the ideal power of the right motors
   * @param holdHeading   a flowable that represents whether or not the 'hold-heading' mode should
   *                      be active.
   * @param targetHeading the desired heading the drivetrain should obtain
   */
  public Command driveHoldHeading(Flowable<Double> left, Flowable<Double> right,
                                  Flowable<Boolean> holdHeading, Flowable<Double> targetHeading) {
    Flowable<Double> output = pidAngle(targetHeading);
    return Command.fromSubscription(() -> {
      return combineDisposable(
          combineLatest(left, output, holdHeading, (x, o, h) -> x + (h ? o : 0.0))
              .onBackpressureDrop()
              .subscribeOn(Schedulers.computation())
              .onBackpressureDrop()
              .map(limitWithin(-1.0, 1.0))
              .map(Events::power)
              .subscribe(leftDrive),
          combineLatest(right, output, holdHeading, (x, o, h) -> x - (h ? o : 0.0))
              .onBackpressureDrop()
              .subscribeOn(Schedulers.computation())
              .onBackpressureDrop()
              .map(limitWithin(-1.0, 1.0))
              .map(x -> Events.power(-x))
              .subscribe(rightDrive));
    })
        // Stop motors afterwards
        .addFinalTerminator(() -> {
          leftDrive.runAtPower(0);
          leftDrive.runAtPower(0);
        });
  }

  /**
   * Drive the robot, and attempt to retain the current heading with the gyro.
   * When hold-heading mode is activated, the target heading will become its current heading.
   *
   * @param left        the ideal power of the left motors
   * @param right       the ideal power of the right motors
   * @param holdHeading a flowable that represents whether or not the 'hold-heading' mode should be
   *                    active.
   */
  public Command driveHoldHeading(Flowable<Double> left, Flowable<Double> right,
                                  Flowable<Boolean> holdHeading) {
    return driveHoldHeading(left, right, holdHeading, Flowable.just(0.0).mergeWith(
        holdHeading.filter(x -> x).withLatestFrom(currentHeading, (x, y) -> y).publish().autoConnect()));
  }

  /**
   * Constructs an output stream for a PID closed loop based on the heading of the robot.
   *
   * @param targetHeading the heading which the system should achieve
   */
  private Flowable<Double> pidAngle(Flowable<Double> targetHeading) {
    return combineLatest(targetHeading, currentHeading, (x, y) -> x - y)
        .onBackpressureDrop().publish().autoConnect()
        .compose(pidLoop(ANGLE_P, ANGLE_BUFFER_LENGTH, ANGLE_I, ANGLE_D));
  }

  /**
   * A command that will drive the robot forward for a given time.
   *
   * @param time the time to drive forwards for, in milliseconds
   */
  public Command driveTimeAction(long time) {
    Flowable<Double> move = toFlow(() -> 0.4);
    return Command.fromSubscription(() ->
        combineDisposable(
            move.map(Events::power).subscribe(leftDrive),
            move.map(x -> -x).map(Events::power).subscribe(rightDrive)
        )
    ).killAfter(time, TimeUnit.MILLISECONDS).then(Command.fromAction(() -> {
      leftDrive.runAtPower(0);
      rightDrive.runAtPower(0);
    }));
  }

  /**
   * Drive the robot using the arcade-style joystick streams that were passed to the Drivetrain.
   * This is usually run during teleop.
   */
  public Command driveTeleop() {
    return driveHoldHeading(
        combineLatest(throttle, yaw, (x, y) -> x + y).publish().autoConnect().onBackpressureDrop(),
        combineLatest(throttle, yaw, (x, y) -> x - y).publish().autoConnect().onBackpressureDrop(),
        Flowable.just(false).concatWith(this.teleHoldHeading));
  }

  @Override
  public void registerSubscriptions() {
    // Intentionally empty
  }
}
