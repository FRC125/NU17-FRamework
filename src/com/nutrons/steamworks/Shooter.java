package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class Shooter implements Subsystem {

  private static final double SHOOTER_POWER = 1.0;
  private static final double PVAL = 1.0;
  private static final double IVAL = 0.0;
  private static final double DVAL = 3.0;
  private static final double FVAL = 0.035;
  private static final ControllerEvent stopEvent = Events
      .combine(Events.setpoint(0), Events.power(0));
  private static final Function<Double, ControllerEvent> aimEvent = x ->
      Events.combine(Events.mode(ControlMode.LOOP_SPEED), Events.setpoint(x));
  private static double SETPOINT = 3250.0;
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;
  private final Flowable<Double> setpointHint;
  edu.wpi.first.wpilibj.Preferences prefs;
  private Flowable<Double> variableSetpoint;
  private Flowable<Double> distance;


  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton, Flowable<Double> distance, Flowable<Double> setpointHint) {
    this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
    this.distance = distance;
    this.setpointHint = setpointHint;
  }

  public Command pulse() {
    return Command.fromSubscription(() -> setpointHint.withLatestFrom(Flowable.just(SETPOINT).share()
        .mergeWith(variableSetpoint), (x, y) -> x + y).share()
        .map(aimEvent).subscribe(shooterController))
        .addFinalTerminator(() -> shooterController.accept(stopEvent));
  }

  @Override
  public void registerSubscriptions() {
    this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();

    this.variableSetpoint = this.distance.filter(x -> x != 0.0).map(x -> 111.0 * x / 12.0 + 1950.0).share();

    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReversedSensor(true);
    this.shooterController.setPID(PVAL, IVAL, DVAL, FVAL);
    Consumer<Double> speed = new WpiSmartDashboard().getTextFieldDouble("shooter speed");
    toFlow(this.shooterController::speed).subscribe(speed);
    this.variableSetpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("calculated setpoint"));
    this.shooterButton.filter(x -> x).map(x -> pulse().terminable(shooterButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));

    toFlow(this.shooterController::speed).withLatestFrom(this.variableSetpoint, (x, y) -> x + 100 > y && x - 100 < y).onBackpressureDrop().share()
        .subscribe(new WpiSmartDashboard().getTextFieldBoolean("shooter rpm within range GO!!"));
  }
}
