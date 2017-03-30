package com.nutrons.steamworks;
import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;


public class FloorGearPlacer implements Subsystem {

    private final Flowable<Boolean> placeButton;
    private final Flowable<Boolean> intakeButton;
    private final Command intakeCommand;
    private final Command placeCommand;
    private final LoopSpeedController intakeMotor;
    private final LoopSpeedController wristMotor;
    private final static double CURRENT_THRESHOLD = 10.0;

    // counterclockwise
    private final static double INTAKE_SPEED = 1.0; // collecting gear speed
    private final static double INTAKE_IDLE_SPEED = 0.1; // keeping gear in place
    private final static double WRIST_DESCENT_SPEED = 1.0;
    private final static double WRIST_ASCENT_SPEED = -1.0;
    private final static double WRIST_PLACE_SPEED = 0.5; // descending again to place the gear onto the peg
    private final static double INTAKE_REVERSE_SPEED = -0.3; // expelling the gear onto the peg
    private final static double PLACE_TIMEOUT_TIME = 1.0; // seconds

    public FloorGearPlacer(Flowable<Boolean> placeButton,
                           Flowable<Boolean> intakeButton, LoopSpeedController intakeMotor,
                           LoopSpeedController wristMotor) {
        this.placeButton = placeButton;
        this.intakeButton = intakeButton;
        this.intakeMotor = intakeMotor;
        this.wristMotor = wristMotor;

        /*
         * Brings the wrist down until it has touched the floor.
         * Runs the intake motor until the gear has been collected,
         * then brings the wrist back up with the gear.
         */

        this.intakeCommand = Command.fromAction(() -> {
            intakeMotor.runAtPower(INTAKE_SPEED);
            wristMotor.runAtPower(WRIST_DESCENT_SPEED);
        }).until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD)
            .then(Command.fromAction(() -> {
                wristMotor.runAtPower(0.0);
            })).until(() -> intakeMotor.getCurrent() > CURRENT_THRESHOLD)
            .then(Command.fromAction(() -> {
                wristMotor.runAtPower(WRIST_ASCENT_SPEED);
            })).until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD)
            .then(Command.fromAction(() -> {
                intakeMotor.runAtPower(INTAKE_IDLE_SPEED);
            }));

        /*
         * Brings the wrist down again, more slowly, onto the peg to place the gear.
         */

        this.placeCommand = Command.fromAction(() -> {
            wristMotor.runAtPower(WRIST_PLACE_SPEED);
            intakeMotor.runAtPower(INTAKE_REVERSE_SPEED);
        }).until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD)
            .then(Command.fromAction(() -> {
                wristMotor.runAtPower(WRIST_ASCENT_SPEED);
            }));

    }

    @Override
    public void registerSubscriptions() {
        this.intakeButton.subscribe((x) -> intakeCommand.endsWhen(this.intakeButton.filter(y -> !y), true).execute(true));
        this.placeButton.subscribe((x) -> placeCommand.endsWhen(this.placeButton.filter(y -> !y), true).execute(true));
    }
}
