package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;

public class Turret implements Subsystem {

  private static final double PVAL = 0.03;
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

  /**
   * The Turret System that is used for aiming our shooter.
   *
   * @param angle  The flowable of doubles that is represent the angle the turret should be facing.
   * @param master The talon controlling the movement of the turret.
   */
  public Turret(Flowable<Double> angle, Flowable<String> state, Talon master,
                Flowable<Double> joyControl) { //TODO: remove joycontrol
    this.angle = angle;
    this.state = state;
    this.hoodMaster = master;
    Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.revLim = FlowOperators.toFlow(this.hoodMaster::revLimitSwitchClosed);
    this.fwdLim = FlowOperators.toFlow(this.hoodMaster::fwdLimitSwitchClosed);
    this.joyControl = joyControl;
  }

  @Override
  public void registerSubscriptions() {
    FlowOperators.deadband(joyControl).map(x -> Events.power(x / 4))
        .subscribe(hoodMaster); //TODO: remove this joystick

    /*this.fwdLim.map(b -> b ?
     Events.combine(Events.mode(ControlMode.MANUAL), Events.power(-0.5))  //TODO: edit these signs
     : Events.combine(Events.power(0.0), Events.mode(ControlMode.LOOP_POSITION)))
     .subscribe(hoodMaster);
     this.revLim.map(b -> b ?
     Events.combine(Events.mode(ControlMode.MANUAL), Events.power(0.5)) //TODO: edit these signs
     : Events.combine(Events.power(0.0), Events.mode(ControlMode.LOOP_POSITION)))
     .subscribe(hoodMaster);
     **/
    /*
     Flowable<ControllerEvent> source = this.angle
     .map(x -> x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS / 360.0)
     .map(x -> Events.pid(hoodMaster.position() + x, PVAL, IVAL, DVAL, FVAL));
     */

    Flowable<Double> setpoint = this.angle.map(x -> x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS / 360.0)
        .map(x -> x + hoodMaster.position());
    this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
    setpoint.subscribe(x -> Events.setpoint(x).actOn(hoodMaster));

    this.angle.subscribe(new WpiSmartDashboard().getTextFieldDouble("angle"));
    this.state.subscribe(new WpiSmartDashboard().getTextFieldString("state"));
    this.revLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("revLim"));
    this.fwdLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("fwdLim"));
    setpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("setpoint"));
  }
}
