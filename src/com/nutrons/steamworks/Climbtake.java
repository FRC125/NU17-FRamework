package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;

public class Climbtake implements Subsystem {
    private static final double INTAKE_SPEED = 1.0;
    private static final double CLIMBER_SPEED = -1.0;
    private final LoopSpeedController intakeController;
    private final LoopSpeedController climberController;
    private final Flowable<Boolean> climberButton;
    private final Flowable<Boolean> intakeButton;

    public Climbtake(LoopSpeedController intakeController, LoopSpeedController climberController, Flowable<Boolean> climberButton, Flowable<Boolean> intakeButton) {
        this.intakeController = intakeController;
        this.climberController = climberController;
        this.climberButton = climberButton;
        this.intakeButton = intakeButton;
    }

    @Override
    public void registerSubscriptions() {
        //TODO: CHANGE VALUES
        climberButton.map(b -> b ? INTAKE_SPEED : 0.0).map(Events::power).subscribe(climberController);
        intakeButton.map(b -> b ? CLIMBER_SPEED : 0.0).map(Events::power).subscribe(intakeController);
    }
}

