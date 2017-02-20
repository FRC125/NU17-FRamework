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
  private static final double DVAL = 0.0;
  private static final double FVAL = 0.0;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;
  private final Flowable<Double> angle;
  private final Flowable<String> state;
  private final Talon hoodMaster;
  private final Flowable<Boolean> revLim;
  private final Flowable<Boolean> fwdLim;
  private final Flowable<Double> joyControl; //TODO: Remoove
  private Flowable<Double> position;

  public Turret(Flowable<Double> angle, Flowable<String> state, Talon master, Flowable<Double> joyControl) { //TODO: remove joycontrol
    this.angle = angle;
    this.state = state;
    this.hoodMaster = master;
    Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.revLim = FlowOperators.toFlow(() -> this.hoodMaster.revLimitSwitchClosed());
    this.fwdLim = FlowOperators.toFlow(() -> this.hoodMaster.fwdLimitSwitchClosed());
    this.joyControl = joyControl;
    this.position = FlowOperators.toFlow(() -> this.hoodMaster.position());
  }

  @Override
  public void registerSubscriptions() {
    FlowOperators.deadband(joyControl).map(FlowOperators::printId).map(x -> Events.power(x / 4)).subscribe(hoodMaster); //TODO: remove this joystick


    Flowable<Double> angles = this.angle.map(x -> (-x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS) / 360.0);
    Flowable<Double> setpoint = Flowable.combineLatest(angles, position, (s, p) -> s).subscribeOn(Schedulers.io());
    setpoint = Flowable.combineLatest(setpoint, state.filter(st -> st.equals("NONE")), (sp, st) -> sp).subscribeOn(Schedulers.io());
    this.hoodMaster.setReversedSensor(true);
    this.hoodMaster.reverseOutput(false);

    this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
    setpoint.map(FlowOperators::printId).subscribe(x -> Events.setpoint(x).actOn(hoodMaster));

    /**Flowable<ControllerEvent> source = this.angle
        .map(x -> x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS / 360.0)
        .map(x -> Events.pid(hoodMaster.position() + x, PVAL, IVAL, DVAL, FVAL));**/


    //joyControl.map(b -> b ? Events.power(-.5) : Events.power(0.0)).subscribe(hoodMaster);

    FlowOperators.toFlow(() -> hoodMaster.position()).subscribe(new WpiSmartDashboard().getTextFieldDouble("position"));
    this.angle.subscribe(new WpiSmartDashboard().getTextFieldDouble("angle"));
    this.state.subscribe(new WpiSmartDashboard().getTextFieldString("state"));
    this.revLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("revLim"));
    this.fwdLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("fwdLim"));
    setpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("setpoint"));

    //source.subscribe(hoodMaster);
  }
}
