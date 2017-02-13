package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;


public class Shooter implements Subsystem {
  private static final double SHOOTER_POWER = 1.0;
  private static final double SETPOINT = 2950.0;
  private static final double PVAL = 0.05;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.33;
  private static final double FVAL = 0.035;
  private final LoopSpeedController shooterController;
  private final Flowable<Boolean> shooterButton;

  public Shooter(LoopSpeedController shooterController, Flowable<Boolean> shooterButton) {
    this.shooterController = shooterController;
    this.shooterButton = shooterButton;
  }

  @Override
  public void registerSubscriptions() {
    /**Flowable<ControllerEvent> source = Flowable.just(Events.pid(SETPOINT, PVAL, IVAL, DVAL, FVAL));
    shooterButton.map(b -> b ? source.mergeWith(toFlow(() -> new LoopModeEvent(ControlMode.LOOP_SPEED))).subscribe(shooterController) : 0.0);**/
    shooterButton.map(b -> b ? SHOOTER_POWER : 0.0).map(Events::power).subscribe(shooterController);
  }
}
