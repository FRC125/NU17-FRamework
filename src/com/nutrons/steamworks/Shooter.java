package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.printId;
import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import com.nutrons.framework.util.Pair;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class Shooter implements Subsystem {

  private static final double SHOOTER_POWER = 1.0;
  private static double SETPOINT = 3250.0;
  private static final double PVAL = 0.05;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.33;
  private static final double FVAL = 0.035;
  private static final ControllerEvent stopEvent = Events
      .combine(Events.setpoint(0), Events.power(0));
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;
  edu.wpi.first.wpilibj.Preferences prefs;
  private Flowable<Double> variableSetpoint;
  private Flowable<Double> distance;


  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton, Flowable<Double> distance) {
    this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
    this.distance = distance;
  }

  @Override
  public void registerSubscriptions() {
    this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();

    this.variableSetpoint = this.distance.map(x -> 111.0*x/12.0 + 1950.0).share()
        .withLatestFrom(this.shooterButton, Pair::new).filter(x -> !x.right()).map(Pair::left);

    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReversedSensor(true);
    this.shooterController.setPID(PVAL, IVAL, DVAL, FVAL);
    Consumer<Double> speed = new WpiSmartDashboard().getTextFieldDouble("shooter speed");
    toFlow(this.shooterController::speed).subscribe(speed);
    shooterButton.map(FlowOperators::printId)
        .withLatestFrom(this.variableSetpoint.map(FlowOperators::printId), (x, y) -> {
          if(x) {
            System.out.println("setting setpoint");
            return Events.combine(Events.mode(ControlMode.LOOP_SPEED),
                Events.setpoint(y));
          }
          return stopEvent;
        })
        .subscribe(shooterController);
    this.variableSetpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("variableasdetj"));
  }
}
