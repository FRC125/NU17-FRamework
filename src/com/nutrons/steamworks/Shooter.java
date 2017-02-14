package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;


public class Shooter implements Subsystem {
  private static final double SHOOTER_POWER = 1.0;
  private static final double SETPOINT = 3250.0;
  private static final double PVAL = 0.05;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.33;
  private static final double FVAL = 0.035;
  private final Talon shooterController;
  private final Flowable<Boolean> shooterButton;
  private Consumer<Double> speedLog;
  private WpiSmartDashboard sd;

  public Shooter(Talon shooterController, Flowable<Boolean> shooterButton) {
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
    this.sd = new WpiSmartDashboard();
  }

  @Override
  public void registerSubscriptions() {
    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReverseSensor(true);
    Events.pid(PVAL, IVAL, DVAL, FVAL).actOn(shooterController);

    this.speedLog = sd.getTextFieldDouble("shooter speed");
    //FlowOperators.toFlow(this.shooterController::speed).subscribe(speedLog);
    shooterButton.subscribe(System.out::println);

    shooterButton.map(x -> x ? Events.combine(Events.mode(ControlMode.LOOP_SPEED),
        Events.setpoint(SETPOINT)) : Events.combine(Events.setpoint(0), Events.power(0)))
        .subscribe(shooterController);
  }
}
