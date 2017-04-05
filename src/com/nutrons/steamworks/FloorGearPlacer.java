package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;


public class FloorGearPlacer implements Subsystem {

  private final static double CURRENT_THRESHOLD_INTAKE = 20.0;
  private final static double CURRENT_THRESHOLD_WRIST = 4.0;
  // counterclockwise
  private final static double INTAKE_SPEED = -1.0; // collecting gear speed
  private final static double WRIST_DESCENT_SPEED = -0.3;
  private final static double WRIST_ASCENT_SPEED = 0.3;
  private final static double WRIST_PLACE_SPEED = -0.3; // descending again to place the gear onto the peg
  private final static double INTAKE_REVERSE_SPEED = 1.0; // expelling the gear onto the peg
  private final static double PLACE_TIMEOUT_TIME = 1.0; // seconds
  private final Flowable<Boolean> placeButton;
  private final Flowable<Boolean> intakeButton;
  private final LoopSpeedController intakeMotor;
  private final LoopSpeedController wristMotor;
  private final Flowable<Double> armUp;
  private final Flowable<Double> armDown;

  public FloorGearPlacer(Flowable<Boolean> placeButton,
      Flowable<Boolean> intakeButton,
      Flowable<Double> armUp,
      Flowable<Double> armDown,
      LoopSpeedController intakeMotor,
      LoopSpeedController wristMotor) {
    this.placeButton = placeButton;
    this.intakeButton = intakeButton;
    this.intakeMotor = intakeMotor;
    this.wristMotor = wristMotor;
    this.armUp = armUp;
    this.armDown = armDown;
  }

  public Command pulse(){
    return Command.just(x -> {
      wristMotor.runAtPower(1.0);
      wristMotor.runAtPower(1.0);
      return Flowable.just(() -> {
        wristMotor.runAtPower(0);
        wristMotor.runAtPower(0);
      });
    });
  }

  @Override
  public void registerSubscriptions() {
    this.intakeButton.map(b -> b ? INTAKE_SPEED : 0.0).map(Events::power).subscribe(intakeMotor);
    this.placeButton.map(b -> b ? INTAKE_REVERSE_SPEED : 0.0).map(Events::power).subscribe(intakeMotor);

    this.armUp.map(x -> x > 0.9 ? true : false).distinctUntilChanged().map(x -> x ? Events.power(-1.0) : Events.power(0.0)).subscribe(wristMotor);
    this.armDown.map(x -> x > 0.9 ? true : false).distinctUntilChanged().map(x -> x ? Events.power(1.0) : Events.power(0.0)).subscribe(wristMotor);

    FlowOperators.toFlow(() -> this.intakeMotor.getCurrent())
        .subscribe(new WpiSmartDashboard().getTextFieldDouble("intake current"));
    FlowOperators.toFlow(() -> this.wristMotor.getCurrent())
        .subscribe(new WpiSmartDashboard().getTextFieldDouble("wrist current"));
  }
}
