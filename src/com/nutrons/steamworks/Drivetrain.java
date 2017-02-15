package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import com.nutrons.framework.inputs.HeadingGyro;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
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
  private final Flowable<Double> angleSetpoint;
  private final Flowable<Double> gyroAngles;
  private final Flowable<Double> headingError;
  private double gyroSetpoint = 0.0;
  //public final Command holdHeadingCmd;
  //public final Command driveNormalCmd;
  private final FlowingPID pidHeadingControl;
  private Consumer<Double> pidHeadingControlLog;
  private final Flowable<Double> distanceSetpoint;
  private final Flowable<Double> encoderValues;
  private final Flowable<Double> distanceError;
  private double encoderSetpoint = 0.0;
  private final FlowingPID pidDistanceControl;
  private Consumer<Double> pidDistanceControlLog;
  private WpiSmartDashboard sd;
  private final double deadband = 0.3;

  /**
   * A drivetrain which uses Arcade Drive. AKA Cheezy Drive
   *
   *
   * @param leftDrive  all controllers on the left of the drivetrain
   * @param rightDrive all controllers on the right of the drivetrain
   */

  public Drivetrain(Flowable<Double> throttle, Flowable<Double> yaw, Flowable<Boolean> holdHeading, Talon encoderMotor,
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
    this.angleSetpoint = toFlow(() -> getAngleSetpoint()).subscribeOn(Schedulers.io());
    this.headingError = combineLatest(angleSetpoint, gyroAngles, (x, y) -> x - y).subscribeOn(Schedulers.io()).onBackpressureDrop();
    this.pidHeadingControl = new FlowingPID(headingError, 0.01, 0.0, 0.0);
    //this.holdHeadingCmd = Command.create(() -> holdHeadingAction());
    //this.driveNormalCmd = Command.create(() -> driveNormalAction());
    //this.holdHeading = holdHeading.map(x -> x ? holdHeadingCmd : driveNormalCmd); // Right Trigger
    this.encoderValues = toFlow(() -> encoderMotor.position())
            .map(x -> {
              System.out.println(x + " = Encoder Readings");
              return x;
            });
    this.
    this.distanceSetpoint = toFlow(() -> getEncoderSetpoint()).subscribeOn(Schedulers.io());
    this.distanceError = combineLatest(distanceSetpoint, encoderValues, (x, y) -> x - y).subscribeOn(Schedulers.io()).onBackpressureDrop();
    this.pidDistanceControl = new FlowingPID(distanceError, 0.01, 0.0, 0.0);
    this.sd = new WpiSmartDashboard();
    pidHeadingControlLog = sd.getTextFieldDouble("Heading Control Output");
    pidDistanceControlLog =  sd.getTextFieldDouble("Distance Control Log");

  }
  private double getEncoderSetpoint() {
    return encoderSetpoint;
  }

  private void setEncoderSetpoint (double setpoint) {
    this.encoderSetpoint = setpoint;
  }
  private synchronized double getAngleSetpoint() {
    return gyroSetpoint;
  }

  private void setAngleSetpoint(double angleSetpoint) {
    this.gyroSetpoint = angleSetpoint;
  }

  private void holdHeadingAction() {
    headingGyro.reset();
    setAngleSetpoint(0.0);
    combineLatest(throttle, yaw, pidHeadingControl.getOutput(), (x, y, z) -> x + y - z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(leftDrive);

    combineLatest(throttle, yaw, pidHeadingControl.getOutput(), (x, y, z) -> x - y - z)
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
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power).subscribe(leftDrive);
    combineLatest(throttle, yaw, (x, y) -> x - y)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(rightDrive);
  }

  private void driveDistanceAction() {
    encoderValues..reset();
    setAngleSetpoint(0.0);
    combineLatest(throttle, yaw, pidHeadingControl.getOutput(), (x, y, z) -> x + y - z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(leftDrive);

    combineLatest(throttle, yaw, pidHeadingControl.getOutput(), (x, y, z) -> x - y - z)
            .subscribeOn(Schedulers.io())
            .onBackpressureDrop()
            .map(x -> x > 1.0 ? 1.0 : x).map(x -> x < -1.0 ? -1.0 : x)
            .map(Events::power)
            .subscribe(rightDrive);
  }

  @Override
  public void registerSubscriptions() {

    this.pidHeadingControl.getOutput().subscribe(pidHeadingControlLog);
    this.pidDistanceControl.getOutput().subscribe(pidDistanceControlLog);
  }
}
