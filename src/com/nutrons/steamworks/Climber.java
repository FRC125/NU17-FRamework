package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;


public class Climber implements Subsystem {
    private final Flowable<ControllerEvent> climb;
    private final Consumer<ControllerEvent> climberController;

    //TODO add proper RunAtPowerEvent input
    public Climber(Consumer<ControllerEvent> climberController) {
        this.climb = Flowable.just(new RunAtPowerEvent(1.0));
        this.climberController = climberController;
    }

    public void registerSubscriptions() {
        climb.subscribe(climberController);
    }
}
