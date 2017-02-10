package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import java.util.concurrent.TimeUnit;

import static com.nutrons.framework.util.FlowOperators.toFlow;

public class Turret implements Subsystem {
    private final Flowable<Double> angle;
    private final Consumer<ControllerEvent> hoodMaster;

    private volatile double motorRotation;
    private static final double HOOD_RADIUS_IN = 10.5;

    public Turret(Flowable<Double> angle, LoopSpeedController master) {
        this.angle = angle;
        this.hoodMaster = master;
        this.angle.map(x -> x / 360.0).subscribe(x -> this.motorRotation = x); // need to find out how many rotations of the motor to turn around turret 360 degrees
        //Calculates arc length turret needs to travel to reach a certain angle,
        //Finds ratio of angle to 360 and creates a proportion to ratio with arc length to full circumference
    }

    @Override
    public void registerSubscriptions() {
        Flowable<ControllerEvent> source = toFlow(() -> new LoopPropertiesEvent(0 /** get current position of encoder**/ + this.motorRotation, 0.03, 0.0, 0.0, 0.0));
               source.mergeWith(toFlow(() -> new LoopModeEvent(ControlMode.LOOP_POSITION))).subscribe(hoodMaster);
    }
}
