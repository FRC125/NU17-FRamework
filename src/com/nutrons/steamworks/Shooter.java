package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.Tuneable;
import com.nutrons.framework.controllers.TuneablePID;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class Shooter implements Subsystem {

  private static final double SHOOTER_POWER = 1.0;
  private static final double STARTING_SETPOINT = 3250.0;
  private static final double STARTING_PVAL = 0.05;
  private static final double STARTING_IVAL = 0.0;
  private static final double STARTING_DVAL = 0.33;
  private static final double STARTING_FVAL = 0.035;
  private Tuneable Setpoint = new Tuneable("Setpoint", STARTING_SETPOINT);
  private TuneablePID PID = new TuneablePID("PID");
  private static final ControllerEvent stopEvent = Events
      .combine(Events.setpoint(0), Events.power(0));
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;

  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton) {
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
  }

  @Override
  public void registerSubscriptions() {
    this.shooterController.setControlMode(ControlMode.MANUAL);
    this.shooterController.setReversedSensor(true);
    this.shooterController.setPID(STARTING_PVAL, STARTING_IVAL, STARTING_DVAL, STARTING_FVAL);
    this.PID.getPID();
    Consumer<Double> speed = new WpiSmartDashboard().getTextFieldDouble("shooter speed");

    //toFlow(() -> this.shooterController.speed()).subscribe(speed);

    //shooterButton.subscribe(System.out::println);
    //toFlow( () -> this.shooterController.speed()).subscribe(System.out::println);
    //Consumer<Double> cle = new WpiSmartDashboard().getTextFieldDouble("error");
    //toFlow(() -> ((Talon)this.shooterController).getClosedLoopError()).subscribe((cle));
    shooterButton.map(FlowOperators::printId)
        .map(x -> x ? Events.combine(Events.mode(ControlMode.LOOP_SPEED),
            Events.setpoint(STARTING_SETPOINT)) : stopEvent)
        .subscribe(shooterController);
  }
}
