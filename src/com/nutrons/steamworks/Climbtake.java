package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.LoopSpeedController;

public class Climbtake implements Subsystem {
    private static final double INTAKE_SPEED = 1.0;
    private static final double CLIMBER_SPEED = -1.0;
    private final LoopSpeedController intakeController;
    private final LoopSpeedController climberController;

    public Climbtake(LoopSpeedController intakeController, LoopSpeedController climberController) {
        this.intakeController = intakeController;
        this.climberController = climberController;
    }

    @Override
    public void registerSubscriptions() {
        //TODO: CHANGE VALUES
        intakeController.runAtPower(INTAKE_SPEED);
        climberController.runAtPower(CLIMBER_SPEED);
    }
}
