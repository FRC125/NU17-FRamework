package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
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
  private static final ControllerEvent stopEvent = Events
      .combine(Events.setpoint(0), Events.power(0));
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;
  edu.wpi.first.wpilibj.Preferences prefs;


  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton) {
    this.prefs = edu.wpi.first.wpilibj.Preferences.getInstance();
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
  }

  @Override
  public void registerSubscriptions() {
    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReversedSensor(true);
    this.shooterController.setPID(prefs.getDouble("shooter_p", 0.05), prefs.getDouble("shooter_i", 0.0), prefs.getDouble("shooter_d", 0.33), prefs.getDouble("shooter_f", 0.035));
    Consumer<Double> speed = new WpiSmartDashboard().getTextFieldDouble("shooter speed");
    toFlow(this.shooterController::speed).subscribe(speed);
    shooterButton.map(FlowOperators::printId)
        .map(x -> x ? Events.combine(Events.mode(ControlMode.LOOP_SPEED),
            Events.setpoint(SETPOINT)) : stopEvent)
        .subscribe(shooterController);
  }
}
