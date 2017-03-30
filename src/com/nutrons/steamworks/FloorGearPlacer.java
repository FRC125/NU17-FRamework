package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.LoopSpeedController;
import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;


public class FloorGearPlacer implements Subsystem {

  private final static double CURRENT_THRESHOLD_INTAKE = 0.5;
  private final static double CURRENT_THRESHOLD_WRIST = 5.0;
  // counterclockwise
  private final static double INTAKE_SPEED = -0.6; // collecting gear speed
  private final static double INTAKE_IDLE_SPEED = -0.2; // keeping gear in place
  private final static double WRIST_DESCENT_SPEED = -0.3;
  private final static double WRIST_ASCENT_SPEED = 0.3;
  private final static double WRIST_PLACE_SPEED = -0.3; // descending again to place the gear onto the peg
  private final static double INTAKE_REVERSE_SPEED = 0.6; // expelling the gear onto the peg
  private final static double PLACE_TIMEOUT_TIME = 1.0; // seconds
  private final Flowable<Boolean> placeButton;
  private final Flowable<Boolean> intakeButton;
  private final LoopSpeedController intakeMotor;
  private final LoopSpeedController wristMotor;

  public FloorGearPlacer(Flowable<Boolean> placeButton,
                         Flowable<Boolean> intakeButton, LoopSpeedController intakeMotor,
                         LoopSpeedController wristMotor) {
    this.placeButton = placeButton;
    this.intakeButton = intakeButton;
    this.intakeMotor = intakeMotor;
    this.wristMotor = wristMotor;
  }

  /*
   * Brings the wrist down until it has touched the floor.
   * Runs the intake motor until the gear has been collected,
   * then brings the wrist back up with the gear.
   */

  Command intakeCommand() {
    return Command.fromAction(() -> {
      System.out.println("i: running intake and wrist");
      intakeMotor.runAtPower(INTAKE_SPEED);
      wristMotor.runAtPower(WRIST_DESCENT_SPEED);
    }).endsWhen(Flowable.timer(1000, TimeUnit.MILLISECONDS), true)
        .until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD_WRIST)
        .then(Command.fromAction(() -> {
          System.out.println("i: Hit thresh 1 stop wrist");
          wristMotor.runAtPower(0.0);
        }))
        .endsWhen(Flowable.timer(1000, TimeUnit.MILLISECONDS), true)
        .until(() -> intakeMotor.getCurrent() > CURRENT_THRESHOLD_INTAKE)
        .then(Command.fromAction(() -> {
          System.out.println("i: ascend wrist");
          wristMotor.runAtPower(WRIST_ASCENT_SPEED);
        }))
        .endsWhen(Flowable.timer(1000, TimeUnit.MILLISECONDS), true)
        .until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD_WRIST)
        .then(Command.fromAction(() -> {
          System.out.println("i: idle intake");
          intakeMotor.runAtPower(INTAKE_IDLE_SPEED);
        }).endsWhen(Flowable.timer(1000, TimeUnit.MILLISECONDS), true));
  }


  /*
   * Brings the wrist down again, more slowly, onto the peg to place the gear.
   */
  Command placeCommand() {
    return Command.fromAction(() -> {
      wristMotor.runAtPower(WRIST_PLACE_SPEED);
      intakeMotor.runAtPower(INTAKE_REVERSE_SPEED);
      System.out.println("running wrist and intake");
    }).until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD_WRIST)
        .then(Command.fromAction(() -> {
          System.out.println("hit current threshold 1 and running wrist");
          wristMotor.runAtPower(WRIST_ASCENT_SPEED);
        }).until(() -> wristMotor.getCurrent() > CURRENT_THRESHOLD_WRIST))
        .then(Command.fromAction(() -> {
          System.out.println("hit current threshold 2 and stopping");
          intakeMotor.runAtPower(0.0);
          wristMotor.runAtPower(0.0);
        }));
  }

  @Override
  public void registerSubscriptions() {
    this.intakeButton.filter(x -> x).map(x -> intakeCommand().terminable(intakeButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));
    this.placeButton.filter(x -> x).map(x -> placeCommand().terminable(placeButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));
  }
}
