package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Created by Brian on 1/31/2017.
 */
public class Feeder implements Subsystem {
    private final Flowable<ControllerEvent> intakeSpeed;
    private final Consumer<ControllerEvent> intakeController;

    public Feeder(Consumer<ControllerEvent> intakeController) {
        this.intakeSpeed = Flowable.just(new RunAtPowerEvent(1.0));
        this.intakeController = intakeController;
    }

    @Override
    public void registerSubscriptions() {
        intakeSpeed.subscribe(intakeController);
    }
}
