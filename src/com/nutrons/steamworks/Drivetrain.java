package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.inputs.HeadingGyro;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

import static com.nutrons.framework.util.FlowOperators.toFlow;
import static io.reactivex.Flowable.combineLatest;
import static java.lang.Math.abs;

public class Drivetrain implements Subsystem {  // Right Trigger
    private final Flowable<Double> throttle;
    private final Flowable<Double> yaw;
    private final Consumer<ControllerEvent> leftDrive;
    private final Consumer<ControllerEvent> rightDrive;
    private final Flowable<Boolean> holdHeading;
    private boolean holdHeadingEnabled = false;
    private final Flowable<Double> gyroAngles;
    private double coeff = 1.0;
    private double gyroSetpoint = 0.0;
    private final HeadingGyro headingGyro;
    private final Flowable<Double> setpoint;
    private final Flowable<Double> error;



    public Drivetrain(Flowable<Double> throttle, Flowable<Double> yaw, Flowable<Boolean> holdHeading,
                      Consumer<ControllerEvent> leftDrive, Consumer<ControllerEvent> rightDrive) {

        this.throttle = deadzone(throttle);
        this.yaw = deadzone(yaw);
        this.leftDrive = leftDrive;
        this.rightDrive = rightDrive;
        this.headingGyro  = new HeadingGyro();
        this.holdHeading = holdHeading;  // Right Trigger
        this.gyroAngles = toFlow(() -> headingGyro.getAngle());
        this.setpoint = toFlow(() -> getSetpoint());
        this.error = combineLatest(setpoint, gyroAngles, (x, y) -> x - y);



    }

    private Flowable<Double> deadzone(Flowable<Double> input) {
        return input.map((x) -> abs(x) < 0.2 ? 0.0 : x);
    }

    private double getSetpoint() { return gyroSetpoint; }

    private void setSetpoint(double setpoint) { gyroSetpoint = setpoint; }

    @Override
    public void registerSubscriptions() {
        holdHeading.map(x -> holdHeadingEnabled = x);
        if (holdHeadingEnabled) {
            headingGyro.reset();
            setSetpoint(0.0);
            combineLatest(throttle, yaw, error, (x, y, z) -> x + y + z).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(leftDrive);
            combineLatest(throttle, yaw, error, (x, y, z) -> x - y - z).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(rightDrive);
        } else {
            combineLatest(throttle, yaw, (x, y) -> x + y).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(leftDrive);
            combineLatest(throttle, yaw, (x, y) -> x - y).map(x -> x * coeff).map(RunAtPowerEvent::new).subscribe(rightDrive);
        }
    }
}
