package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;

public class Turret implements Subsystem {
  private static final double PVAL = 125.0;
  private static final double IVAL = 0.0;
  private static final double DVAL = 12.5;
  private static final double FVAL = 0.0;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;
  private final Flowable<Double> angle;
  private final Talon hoodMaster;
  private final Flowable<Boolean> revLim;
  private final Flowable<Boolean> fwdLim;
  private final Flowable<Double> joyControl; //TODO: Remoove
  private Flowable<Double> position;
  private final Flowable<Boolean> aimButton;

  public Turret(Flowable<Double> angle, Talon master, Flowable<Double> joyControl, Flowable<Boolean> aimButton) { //TODO: remove joycontrol
    this.angle = angle;
    this.hoodMaster = master;
    Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.revLim = FlowOperators.toFlow(() -> this.hoodMaster.revLimitSwitchClosed());
    this.fwdLim = FlowOperators.toFlow(() -> this.hoodMaster.fwdLimitSwitchClosed());
    this.joyControl = joyControl;
    this.position = FlowOperators.toFlow(() -> this.hoodMaster.position());
    this.aimButton = aimButton;
  }

  @Override
  public void registerSubscriptions() {
    FlowOperators.deadband(joyControl).map(x -> Events.power(x / 4)).subscribe(hoodMaster); //TODO: remove this joystick

    Flowable<Double> angles = this.angle.map(x -> (-x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS) / 360.0);
    Flowable<Double> setpoint = Flowable.combineLatest(angles, position, (s, p) -> s).subscribeOn(Schedulers.io());

    this.hoodMaster.setReversedSensor(true);
    this.hoodMaster.reverseOutput(false);

    /**Flowable<Double> finalSetpoint = setpoint;
    aimButton.map((Boolean b) -> {
      if(b) {
        this.hoodMaster.setControlMode(ControlMode.LOOP_POSITION);
        this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
        finalSetpoint.map(FlowOperators::printId).subscribe(x -> Events.setpoint(x).actOn(hoodMaster));
      }else{
        this.hoodMaster.setControlMode(ControlMode.MANUAL);
        FlowOperators.deadband(joyControl).map(FlowOperators::printId).map(x -> Events.power(x / 4)).subscribe(hoodMaster); //TODO: remove this joystick
      }
      return b;
    });**/

    FlowOperators.toFlow(() -> hoodMaster.position()).subscribe(new WpiSmartDashboard().getTextFieldDouble("position"));
    this.angle.subscribe(new WpiSmartDashboard().getTextFieldDouble("angle"));
    angles.subscribe(new WpiSmartDashboard().getTextFieldDouble("angles (setpoints)"));
    this.revLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("revLim"));
    this.fwdLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("fwdLim"));
    setpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("setpoint"));
  }
}
