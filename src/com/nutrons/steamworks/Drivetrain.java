package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.inputs.HeadingGyro;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

import static com.nutrons.framework.util.FlowOperators.*;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {

  private final Talon leftLeader;
  private final Talon rightLeader;
  private final HeadingGyro gyro;
  private final Flowable<Double> throttle;
  private final Flowable<Double> yaw;
  private final Consumer<ControllerEvent> leftDrive;
  private final Consumer<ControllerEvent> rightDrive;
  private Flowable<Double> targetHeading;
  private final Flowable<Double> error;
  private final Flowable<Double> output;
  private final double deadband = 0.2;
  private final Flowable<Boolean> holdHeading;
  private final double ANGLE_P = 0.045;
  private final double ANGLE_I = 0.0;
  private final double ANGLE_D = 0.0065;
  private final int ANGLE_BUFFER_LENGTH = 10;

  private final Flowable<Boolean> slowDrive;
  private final double slowDriveCoeff = 0.33;




  /**
   * A drivetrain which uses Arcade Drive.
   *
   * @param holdHeading    whether or not the drivetrain should maintain the target heading
   * @param currentHeading the current heading of the drivetrain
   * @param targetHeading  the target heading for the drivetrain to aquire
   * @param leftDrive      all controllers on the left of the drivetrain
   * @param rightDrive     all controllers on the right of the drivetrain
   */
  public Drivetrain(Talon leftLeader,
                    Talon rightLeader,
                    HeadingGyro gyro,
                    Flowable<Boolean> holdHeading,
                    Flowable<Boolean> slowDrive,
                    Flowable<Double> currentHeading,
                    Flowable<Double> targetHeading,
                    Flowable<Double> throttle,
                    Flowable<Double> yaw,
                    Consumer<ControllerEvent> leftDrive,
                    Consumer<ControllerEvent> rightDrive) {

    this.leftLeader = leftLeader;
    this.rightLeader = rightLeader;
    this.gyro = gyro;
    this.throttle = throttle.map(deadbandMap(-deadband, deadband, 0.0));
    this.yaw = yaw.map(deadbandMap(-deadband, deadband, 0.0));
    this.leftDrive = leftDrive;
    this.rightDrive = rightDrive;
    this.targetHeading = targetHeading;
    this.error = combineLatest(targetHeading, currentHeading, (x, y) -> x - y);
    this.output = error
        .compose(pidLoop(ANGLE_P, ANGLE_BUFFER_LENGTH, ANGLE_I, ANGLE_D));
    this.holdHeading = holdHeading;

    this.slowDrive = slowDrive;
  }

  public Command driveTimeAction(long time) {
    Flowable<Double> move = toFlow(() -> 1.0);
    return Command.fromSubscription(() -> combineDisposable(move.map(x -> Events.power(x)).subscribe(leftDrive),
        move.map(x -> Events.power(-x)).subscribe(rightDrive))).killAfter(time, TimeUnit.MILLISECONDS);
  }

    /**
     * Drive the motors a desired amount of revolutions
     * 1 Revolution = 0.85 Feet
     * @param targetRevolutions   Desired amount of revolutions... 1 Revolution = 0.85 Feet
     */
    public  Command driveDistanceAction(double targetRevolutions) {
        return Command.fromAction(() -> {
            // 1 Revolution = 0.85 Feet
            System.out.println("Ran Drive Distance Action");
            leftLeader.setControlMode(ControlMode.LOOP_SPEED);
            rightLeader.setControlMode(ControlMode.LOOP_SPEED);
            leftLeader.setPID(0.001, 0.0, 0.0, 0.0);
            rightLeader.setPID(0.001, 0.0, 0.0, 0.0);
            leftLeader.setSetpoint(targetRevolutions);
            rightLeader.setSetpoint(targetRevolutions);
        });
    }

    /**
     * Drive the motors a desired amount of revolutions
     * 1 Revolution = 0.85 Feet
     * @param targetAngle  Desired amount of revolutions... 1 Revolution = 0.85 Feet
     */
    public  Command turnToAngleAction(double targetAngle) {
        System.out.println("Ran Turn To Angle Action");
        gyro.reset();
        targetHeading = Flowable.just(targetAngle);
        return Command.fromSubscription(() -> combineDisposable(
                output
                        .subscribeOn(Schedulers.io())
                        .onBackpressureDrop()
                        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
                        .map(x -> -x)
                        .map(Events::power).subscribe(leftDrive),

                output
                        .subscribeOn(Schedulers.io())
                        .onBackpressureDrop()
                        .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
                        .map(Events::power)
                        .subscribe(rightDrive)
        ));
    }

  @Override
  public void registerSubscriptions() {
      throttle.map(x -> slowDrive.map(y -> y ? x * slowDriveCoeff : x * 1.0));
      yaw.map(x -> slowDrive.map(y -> y ? x * slowDriveCoeff : x * 1.0));
      combineLatest(throttle, yaw, output, holdHeading, (t, y, o, h) -> t - y - (h ? o : 0.0))
        .subscribeOn(Schedulers.io())
        .onBackpressureDrop()
        .compose(limitWithin(-1.0, 1.0))
        .map(Events::power)
        .subscribe(leftDrive);

      combineLatest(throttle, yaw, output, holdHeading, (t, y, o, h) -> t - y - (h ? o : 0.0))
        .subscribeOn(Schedulers.io())
        .onBackpressureDrop()
        .compose(limitWithin(-1.0, 1.0))
        .map(Events::power)
        .subscribe(rightDrive);
  }
}
