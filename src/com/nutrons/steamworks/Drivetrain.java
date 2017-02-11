package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.inputs.HeadingGyro;
import com.nutrons.framework.util.Command;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static com.nutrons.framework.util.FlowOperators.deadband;
import static com.nutrons.framework.util.FlowOperators.toFlow;
import static io.reactivex.Flowable.combineLatest;

public class Drivetrain implements Subsystem {  // Right Trigger
    private final Flowable<Double> throttle;
    private final Flowable<Double> yaw;
    private final Consumer<ControllerEvent> leftDrive;
    private final Consumer<ControllerEvent> rightDrive;
    private final Flowable<Command> holdHeading;
    private boolean holdHeadingEnabled = false;
    private final Flowable<Double> gyroAngles;
    private double coeff = 1.0;
    private double gyroSetpoint = 0.0;
    private final HeadingGyro headingGyro;
    private final Flowable<Double> setpoint;
    private final Flowable<Double> error;
    private final Flowable<Double> errorP;
    private final Flowable<Double> errorI;
    private final Flowable<Double> errorD;
    private final Flowable<Double> controlOutput;
    private static final double PROPORTIONAL = 0.3;
    private static final double INTEGRAL = 0.3;
    private final Command holdHeadingCmd;
    private final Command driveNormalCmd;


    public Drivetrain(Flowable<Double> throttle, Flowable<Double> yaw, Flowable<Boolean> holdHeading,
                      Consumer<ControllerEvent> leftDrive, Consumer<ControllerEvent> rightDrive) {

        this.throttle = deadband(throttle);
        this.yaw = deadband(yaw);
        this.leftDrive = leftDrive;
        this.rightDrive = rightDrive;
        this.headingGyro  = new HeadingGyro();
        this.gyroAngles = toFlow(() -> headingGyro.getAngle());
        this.setpoint = toFlow(() -> getSetpoint());
        this.error = combineLatest(setpoint, gyroAngles, (x, y) -> x - y);
        this.errorP = error.map(x -> x * PROPORTIONAL);
        this.errorI = error.buffer(10, 1).map(list -> list.stream().reduce(0.0, (x, acc) -> x + acc)).map(x -> x * INTEGRAL);
        this.errorD = error.buffer(2, 1).map(last -> last.stream().reduce(0.0, (x, y) -> x - y));
        this.controlOutput = combineLatest(errorP, errorI, errorD, (p, i, d) -> p + i + d);
        this.holdHeadingCmd = Command.create(() -> holdHeadingAction());
        this.driveNormalCmd = Command.create(() -> driveNormalAction());
        this.holdHeading = holdHeading.map(x -> x ? holdHeadingCmd : driveNormalCmd); // Right Trigger
        Command.fromSwitch(this.holdHeading);
    }

    private double getSetpoint() { return gyroSetpoint; }

    private void setSetpoint(double setpoint) { gyroSetpoint = setpoint; }

    private void holdHeadingAction() {
        combineLatest(throttle, yaw, controlOutput, (x, y, z) -> x + y + z).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(leftDrive);
        combineLatest(throttle, yaw, controlOutput, (x, y, z) -> x - y - z).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(rightDrive);
    }
    private void driveNormalAction() {
        combineLatest(throttle, yaw, (x, y) -> x + y ).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(leftDrive);
        combineLatest(throttle, yaw, (x, y) -> x - y ).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(rightDrive);
    }

    @Override
    public void registerSubscriptions() {

    }

}
