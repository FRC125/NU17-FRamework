package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import io.reactivex.functions.Consumer;

public class Logging implements Subsystem {
  private WpiSmartDashboard sd;
  private Consumer<String> state;
  private Consumer<Double> angle;
  private Consumer<Double> distance;

  Logging() {
    this.sd = new WpiSmartDashboard();
    this.angle = sd.getTextFieldDouble("angle");
    this.distance = sd.getTextFieldDouble("distance");
    this.state = sd.getTextFieldString("state");
  }

  @Override
  public void registerSubscriptions() {
    Vision.getInstance().getAngle().subscribe(this.angle);
    Vision.getInstance().getDistance().subscribe(this.distance);
    Vision.getInstance().getState().subscribe(this.state);
  }
}
