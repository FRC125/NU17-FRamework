package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class ButtonTest implements Subsystem {
  private Flowable<Boolean> button;
  private WpiSmartDashboard log;
  private Consumer<Boolean> reader;

  public ButtonTest(Flowable<Boolean> button) {
    this.button = button;
    log = new WpiSmartDashboard();
    reader = log.getTextFieldBoolean("Pressed");

  }

  @Override
  public void registerSubscriptions() {
    button.subscribe(reader);
  }


}
