package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.Talon;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;

public class Turret implements Subsystem {

  private static final double PVAL = 125.0;
  private static final double IVAL = 0.0;
  private static final double DVAL = 12.5;
  private static final double FVAL = 0.0;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;
  private final Flowable<Double> angle;
  private final Flowable<Double> distance;
  private final Talon hoodMaster;
  private final Flowable<Boolean> revLim;
  private final Flowable<Boolean> fwdLim;
  private final Flowable<Double> joyControl; //TODO: Remoove
  private final Flowable<Boolean> aimButton;
  private Flowable<Double> position;
  private static final double TOLERANCE_DEGREES = 1.0;

  /**
   * The Turret System that is used for aiming our shooter.
   *
   * @param angle  The flowable of doubles that is represent the angle the turret should be facing.
   * @param master The talon controlling the movement of the turret.
   */
  public Turret(Flowable<Double> angle,
                Flowable<Double> distance,
                Talon master,
                Flowable<Double> joyControl,
                Flowable<Boolean> aimButton) { //TODO: remove joycontrol
    this.angle = angle.map(x -> Math.toDegrees(x));
    this.distance = distance;
    this.hoodMaster = master;
    Events.resetPosition(0.0).actOn(this.hoodMaster);
    this.revLim = FlowOperators.toFlow(this.hoodMaster::revLimitSwitchClosed);
    this.fwdLim = FlowOperators.toFlow(this.hoodMaster::fwdLimitSwitchClosed);
    this.joyControl = joyControl;
    this.position = FlowOperators.toFlow(() -> this.hoodMaster.position());
    this.aimButton = aimButton;
  }

  public Command pulse() {
    return Command.just(x -> {
      this.hoodMaster.setControlMode(ControlMode.LOOP_POSITION);
      this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
      return Flowable.just(() -> {
        this.hoodMaster.setControlMode(ControlMode.MANUAL);
      });
    });
  }

  @Override
  public void registerSubscriptions() {
    Flowable<Double> setpoint = this.angle
        .map(x -> (x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS) / 360.0); //used to be negative

    this.hoodMaster.setReversedSensor(false); //used to be true

    aimButton.subscribe((Boolean b) -> {
      if (b) {
        this.hoodMaster.setControlMode(ControlMode.LOOP_POSITION);
        this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
        setpoint.subscribe(x -> Events.setpoint(x).actOn(hoodMaster));
      } else {
        this.hoodMaster.setControlMode(ControlMode.MANUAL);
        FlowOperators.deadband(joyControl).map(x -> Events.power(x ))
            .subscribe(hoodMaster); //TODO: remove this joystick
      }
    });

    FlowOperators.toFlow(() -> hoodMaster.position())
        .subscribe(new WpiSmartDashboard().getTextFieldDouble("position"));
    this.angle.subscribe(new WpiSmartDashboard().getTextFieldDouble("angle"));
    this.angle.map(x -> x < TOLERANCE_DEGREES ? true : false).subscribe(new WpiSmartDashboard().getTextFieldBoolean("within tolerance, GO!"));
    this.distance.subscribe(new WpiSmartDashboard().getTextFieldDouble("distance"));
    this.distance.map(x -> x > 108 && x < 168 ? true : false).subscribe(new WpiSmartDashboard().getTextFieldBoolean("within distance range, GO!"));
    this.revLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("revLim"));
    this.fwdLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("fwdLim"));
    setpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("setpoint"));
  }
}
