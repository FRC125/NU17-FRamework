package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.LoopPropertiesEvent;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;


/**
 * Created by Brian on 2/1/2017.
 */
public class Shooter implements Subsystem {
    private final Flowable<ControllerEvent> runShooter;
    private final Consumer<ControllerEvent> shooterController;

    public Shooter(Consumer<ControllerEvent> shooterController) {
        this.runShooter = Flowable.just(new LoopPropertiesEvent(2950.0, 0.05, 0.0, 0.33, 0.035));
        this.shooterController = shooterController;
    }


    @Override
    public void registerSubscriptions() {
        runShooter.subscribe(shooterController);
    }
}
