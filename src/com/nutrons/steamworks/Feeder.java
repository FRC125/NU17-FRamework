package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

public class Feeder implements Subsystem {

    // TODO: tune as needed
    private static final double SPIN_POWER = 0.9;
    private final LoopSpeedController feederController;
    private final Flowable<Boolean> feederButton;

    public Feeder(LoopSpeedController feederController, Flowable<Boolean> feederButton) {
        this.feederController = feederController;
        this.feederButton = feederButton;
    }

    @Override
    public void registerSubscriptions() {
        feederButton.map(b -> b ? SPIN_POWER : 0.0).map(Events::power).subscribe(feederController);
    }
}