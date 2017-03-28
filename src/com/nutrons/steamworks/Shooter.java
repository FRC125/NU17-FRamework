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
import edu.wpi.first.wpilibj.Preferences;
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
  private static final double AUTO_SETPOINT = 2800;
  private static double SETPOINT = 3080.0;
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;
  private final Flowable<Double> setpointHint;
  //edu.wpi.first.wpilibj.Preferences prefs;
  private Flowable<Double> variableSetpoint;
  private Flowable<Double> distance;
  private double latestSetpoint;


  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton, Flowable<Double> distance, Flowable<Double> setpointHint) {
    //this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
    this.distance = distance;
    this.setpointHint = setpointHint;
    this.variableSetpoint = this.distance.filter(x -> x != 0.0).map(x -> 9.76 * x + 1966.4).share();
  }

  public Command auto() {
    Flowable<ControllerEvent> setpoint = variableSetpoint.take(1).map(aimEvent);
    return Command.fromSubscription(() ->
        setpoint.subscribe(shooterController))
        .addFinalTerminator(() -> shooterController.accept(stopEvent));
  }

  public Command pulse() {
    Flowable<ControllerEvent> combined = setpointHint.withLatestFrom(Flowable.just(SETPOINT)
        .mergeWith(variableSetpoint.take(1)
        ), (x, y) -> x + y).map(aimEvent);
    return Command.fromSubscription(() ->
        combined.subscribe(shooterController))
        .addFinalTerminator(() -> shooterController.accept(stopEvent));
  }

  @Override
  public void registerSubscriptions() {
    //this.prefs = Preferences.getInstance();
    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReversedSensor(true);
    this.shooterController.setPID(PVAL, IVAL, DVAL, FVAL);
    Consumer<Double> speed = new WpiSmartDashboard().getTextFieldDouble("shooter speed");
    toFlow(this.shooterController::speed).subscribe(speed);
    this.variableSetpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("calculated setpoint"));
    this.shooterButton.filter(x -> x).map(x -> pulse().terminable(shooterButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));

    /**toFlow(this.shooterController::speed).withLatestFrom(this.variableSetpoint, (x, y) -> x + 100 > y && x - 100 < y)
        .onBackpressureDrop().map(x -> RobotBootstrapper.feeder.pulse().terminable(shooterButton.filter(y -> !y)))
        .subscribe(x -> x.execute(true));**/

    toFlow(this.shooterController::speed).withLatestFrom(this.variableSetpoint, (x, y) -> x + 100 > y && x - 100 < y).onBackpressureDrop()
        .subscribe(new WpiSmartDashboard().getTextFieldBoolean("shooter rpm within range GO!!"));
  }
}
