package com.nutrons.steamworks.simulation;

import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.nutrons.framework.controllers.ControlMode;
import com.nutrons.framework.controllers.LoopSpeedController;
import com.nutrons.framework.controllers.VirtualSpeedController;
import io.reactivex.Flowable;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

class GUISpeedController extends VBox {
  private final VirtualSpeedController controller;
  private final Pane sources;
  private final ProgressBar output;
  private final Label modeLabel;
  private volatile double inverted;

  GUISpeedController(String name) {
    super();
    this.sources = new VBox();
    this.controller = new VirtualSpeedController(guiSlider(), guiSlider());
    this.getChildren().add(new Label(name));
    this.getChildren().add(sources);
    this.getChildren().add(this.output = new ProgressBar());
    this.getChildren().add(this.modeLabel = new Label());
    Platform.runLater(() -> this.output.setProgress(0.5));
    this.inverted = 1.0;
    this.controller.rawOutput().subscribe(x -> this.output.setProgress(this.inverted * x / 2.0 + 0.5));
    this.controller.mode().subscribe(this::updateMode);
    this.controller.outputDirection().subscribe(x -> this.inverted = x ? -1.0 : 1.0);
  }

  GUISpeedController() {
    this("");
  }

  private void updateMode(ControlMode mode) {
    this.output.setDisable(!mode.equals(ControlMode.MANUAL));
    Platform.runLater(() -> this.modeLabel.setText(mode.name()));
  }

  LoopSpeedController controller() {
    return this.controller;
  }

  private Flowable<Double> guiSlider() {
    Slider slide = new Slider(-1.0, 1.0, 0.0);
    //sources.getChildren().add(slide);
    return toFlow(slide::getValue);
  }
}
