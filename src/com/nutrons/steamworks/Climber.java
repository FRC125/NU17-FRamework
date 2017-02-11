package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

/**
 * Created by Brian on 2/10/2017.
 */
public class Climber implements Subsystem {
    private final Flowable<ControllerEvent> climb;
    private final Consumer<ControllerEvent> climberController;

    public Climber(Consumer<ControllerEvent> climberController) {
        this.climb = Flowable.just(new RunAtPowerEvent(1.0));
        this.climberController = climberController;
    }

    public void registerSubscriptions() {
        climb.subscribe(climberController);
    }
}
