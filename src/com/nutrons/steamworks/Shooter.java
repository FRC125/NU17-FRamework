package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;


public class Shooter implements Subsystem {

  private final LoopSpeedController shooterController;
  private static final double SETPOINT = 2950.0;
  private static final double PVAL = 0.05;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.33;
  private static final double FVAL = 0.035;

  public Shooter(LoopSpeedController shooterController) {
    this.shooterController = shooterController;
  }

  @Override
  public void registerSubscriptions() {
    Flowable<ControllerEvent> source = Flowable.just(Events.pid(SETPOINT, PVAL, IVAL, DVAL, FVAL));
    source.mergeWith(toFlow(() -> new LoopModeEvent(ControlMode.LOOP_SPEED))).subscribe(shooterController);
  }
}
