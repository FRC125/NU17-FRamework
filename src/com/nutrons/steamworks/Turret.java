package com.nutrons.steamworks;

import static com.nutrons.framework.util.FlowOperators.deadbandMap;
import static io.reactivex.Flowable.combineLatest;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.Events;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import com.nutrons.framework.util.FlowOperators;
import io.reactivex.Flowable;

public class Turret implements Subsystem {

  private static final double PVAL = 0.45;
  private static final double IVAL = 0.0;
  private static final double DVAL = 0.0;
  private static final double FVAL = 0.0;
  private static final double MOTOR_ROTATIONS_TO_TURRET_ROTATIONS = (double) 104 / 22;
  private static final double TOLERANCE_DEGREES = 1.0;
  private final Flowable<Double> angle;
  private final Flowable<Double> distance;
  private final LoopSpeedController hoodMaster;
  private final Flowable<Boolean> revLim;
  private final Flowable<Boolean> fwdLim;
  private final Flowable<Double> joyControl; //TODO: Remoove
  private final Flowable<Boolean> aimButton;
  private final Flowable<Double> setpoint;
  private Flowable<Double> joyedSetpoint;
  private Flowable<Double> position;

  /**
   * The Turret System that is used for aiming our shooter.
   *
   * @param angle  The flowable of doubles that is represent the angle the turret should be facing.
   * @param master The talon controlling the movement of the turret.
   */
  public Turret(Flowable<Double> angle,
                Flowable<Double> distance,
                LoopSpeedController master,
                Flowable<Double> joyControl,
                Flowable<Boolean> aimButton) { //TODO: remove joycontrol
    this.angle = angle.map(Math::toDegrees);
    this.distance = distance;
    this.hoodMaster = master;
    this.hoodMaster.accept(Events.resetPosition(0.0));
    this.revLim = FlowOperators.toFlow(this.hoodMaster::revLimitSwitchClosed);
    this.fwdLim = FlowOperators.toFlow(this.hoodMaster::fwdLimitSwitchClosed);
    this.joyControl = joyControl;
    this.position = FlowOperators.toFlow(this.hoodMaster::position);
    this.aimButton = aimButton;
    this.setpoint = this.angle
        .map(x -> (x * MOTOR_ROTATIONS_TO_TURRET_ROTATIONS) / 360.0).map(x -> {
          hoodMaster.accept(Events.resetPosition(0.0));
          return x;
        }); //used to be negative
  }

  public Command automagicMode() {
    return Command.fromAction(() -> {
      this.hoodMaster.setControlMode(ControlMode.LOOP_POSITION);
      this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
    }).then(Command.fromSubscription(() -> setpoint.map(Events::setpoint).subscribe(hoodMaster))
        .addFinalTerminator(() -> hoodMaster.runAtPower(0)));
  }

  /**
   * TeleControl lets the turret auto aim to any target, but also be controlled by the joystick
   * (by way of altering the setpoint!)
   * @return a command that aims the turret
   */
  public Command teleControl() {
    return Command.fromAction(() -> {
      System.out.println("doing teleControl");
      this.hoodMaster.setControlMode(ControlMode.LOOP_POSITION);
      this.hoodMaster.setPID(PVAL, IVAL, DVAL, FVAL);
    }).then(Command.fromSubscription(() -> joyedSetpoint.map(Events::setpoint).subscribe(hoodMaster))
        .addFinalTerminator(() -> {
      System.out.println("final terminator");
      hoodMaster.runAtPower(0);
        }));
  }

  @Override
  public void registerSubscriptions() {
    this.hoodMaster.setReversedSensor(false); //used to be true
    //Change joystick control into setpoints, full range is -4.7 to 4.7

    this.joyedSetpoint = combineLatest(joyControl.map(deadbandMap(-0.15, 0.15, 0.0)).map(x -> -1.125 * x), this.setpoint
        .withLatestFrom(Flowable.just(false).mergeWith(aimButton), (x, y) -> y ? x : 0.0).onBackpressureDrop()
        , (j, s) -> j + s);

    //this.joyedSetpoint.subscribe(System.out::println);

    /**FlowOperators.deadfband(joyControl).map(x -> -0.3 * x).map(Events::power).share()
        .subscribe(hoodMaster);**/

    this.angle.subscribe(new WpiSmartDashboard().getTextFieldDouble("angle_vis"));
    this.angle.map(x -> x < TOLERANCE_DEGREES).subscribe(new WpiSmartDashboard().getTextFieldBoolean("within tolerance, GO!"));
    this.distance.subscribe(new WpiSmartDashboard().getTextFieldDouble("distance_vis"));
    this.distance.map(x -> x > 108 && x < 168).subscribe(new WpiSmartDashboard().getTextFieldBoolean("within distance range, GO!"));
    this.revLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("revLim"));
    this.fwdLim.subscribe(new WpiSmartDashboard().getTextFieldBoolean("fwdLim"));
    setpoint.subscribe(new WpiSmartDashboard().getTextFieldDouble("setpoint"));
  }
}
