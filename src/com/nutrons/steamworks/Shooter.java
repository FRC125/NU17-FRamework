package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.*;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;


public class Shooter implements Subsystem {

  private final LoopSpeedController shooterController;

  public Shooter(LoopSpeedController shooterController) {
    this.shooterController = shooterController;
  }

  @Override
  public void registerSubscriptions() {
    Flowable<ControllerEvent> source = toFlow(() -> new LoopPropertiesEvent(2950.0, 0.05, 0.0, 0.33, 0.035));
    source.mergeWith(toFlow(() -> new LoopModeEvent(ControlMode.LOOP_SPEED))).subscribe(shooterController);
  }
}