package com.nutrons.steamworks.simulation;

import static com.nutrons.framework.util.CompMode.AUTO;
import static com.nutrons.framework.util.CompMode.TELE;
import static com.nutrons.framework.util.CompMode.TEST;
import static com.nutrons.framework.util.FlowOperators.toFlow;

import com.nutrons.framework.StreamManager;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.util.CompMode;
import com.nutrons.steamworks.Climbtake;
import com.nutrons.steamworks.Drivetrain;
import com.nutrons.steamworks.Feeder;
import com.nutrons.steamworks.Shooter;
import com.nutrons.steamworks.Turret;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;

public class Controller implements Initializable {
  private final static String DEV_ID = "usb-mayflash_limited_MAYFLASH_GameCube_Controller_Adapter-event-mouse";
  public static Controller instance;
  private final PublishProcessor<Boolean> enabled;
  private final PublishProcessor<CompMode> mode;
  public Button enable;
  public Button disable;
  public Button tele;
  public Button auto;
  public Button test;
  public VBox controllers;
  private StreamManager sm;
  private Drivetrain drivetrain;
  private Climbtake climb;
  private Feeder feeder;
  private Shooter shooter;
  private Turret turret;

  public Controller() {
    enabled = PublishProcessor.create();
    mode = PublishProcessor.create();
  }

  private void updateMode(CompMode mode) {
    System.out.println("updating mode");
    tele.setDisable(mode.equals(TELE));
    auto.setDisable(mode.equals(AUTO));
    test.setDisable(mode.equals(TEST));
    this.mode.onNext(mode);
  }

  private void updateEnabled(boolean enabled) {
    enable.setDisable(enabled);
    disable.setDisable(!enabled);
    this.enabled.onNext(enabled);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    System.out.println("initializing");
    tele.setOnAction(x -> updateMode(TELE));
    auto.setOnAction(x -> updateMode(AUTO));
    test.setOnAction(x -> updateMode(TEST));
    enable.setOnAction(x -> updateEnabled(true));
    disable.setOnAction(x -> updateEnabled(false));
    instance = this;
    ObservableList<Node> table = controllers.getChildren();
    GUISpeedController climb1 = new GUISpeedController("climb1");
    GUISpeedController climb2 = new GUISpeedController("climb2");
    table.add(climb1);
    table.add(climb2);
    this.sm = new StreamManager(enabled.replay(1).autoConnect(), mode.replay(1).autoConnect());
    this.climb = new Climbtake(climb1.controller(), climb2.controller(),
        Simulation.button(DEV_ID, "BTN_THUMB"), Simulation.button(DEV_ID, "BTN_THUMB2"));
    sm.registerSubsystem(climb);
    GUISpeedController leftDrive = new GUISpeedController("leftDrive");
    GUISpeedController rightDrive = new GUISpeedController("rightDrive");
    table.addAll(leftDrive, rightDrive);
    Slider gyro = new Slider(-180, 180, 0.0);
    leftDrive.controller().setOutputFlipped(true);
    this.drivetrain = new Drivetrain(Simulation.button(DEV_ID, "BTN_TRIGGER"), toFlow(gyro::getValue), Simulation.joy(DEV_ID, "ABS_Y"), Simulation.joy(DEV_ID, "ABS_RZ"), leftDrive.controller(), rightDrive.controller());
    sm.registerSubsystem(drivetrain);
    GUISpeedController shooter1 = new GUISpeedController("shooter1");
    this.shooter = new Shooter(shooter1.controller(), Simulation.button(DEV_ID, "BTN_BASE2"), Flowable.just(0.0), Flowable.just(0.0));
    GUISpeedController roller = new GUISpeedController("roller");
    GUISpeedController spinner = new GUISpeedController("spinner");
    this.feeder = new Feeder(roller.controller(), spinner.controller(), Simulation.button(DEV_ID, "BTN_TOP"), Simulation.button(DEV_ID, "BTN_TOP"), Flowable.empty());
    Slider turretAngle = new Slider(-45, 45, 0.0);
    Slider turretDistance = new Slider(0, 200, 0.0);
    GUISpeedController turretController = new GUISpeedController("turret");
    this.turret = new Turret(toFlow(turretAngle::getValue), toFlow(turretDistance::getValue), turretController.controller(), Simulation.joy(DEV_ID, "ABS_X"), Simulation.button(DEV_ID, "BTN_BASE4"));
    table.addAll(shooter1, roller, spinner, turretController);
    sm.registerSubsystem(shooter);
    sm.registerSubsystem(feeder);
    sm.registerSubsystem(turret);
    table.addAll(turretAngle, turretDistance, gyro);
  }

  void startCompetition() {
    sm.startCompetition(() ->
        Command.parallel(
            climb.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS),
            Command.serial(drivetrain.driveDistance(6.25, 0.5, 10)
                    .endsWhen(Flowable.timer(1300, TimeUnit.MILLISECONDS), true),
                drivetrain.turn(85, 10)
                    .endsWhen(Flowable.timer(1000, TimeUnit.MILLISECONDS), true),
                Command.parallel(
                    turret.automagicMode().delayFinish(15000, TimeUnit.MILLISECONDS),
                    //floorGearPlacer.pulse().delayFinish(250, TimeUnit.MILLISECONDS),
                    shooter.auto().delayStart(1500, TimeUnit.MILLISECONDS)
                        .delayFinish(15, TimeUnit.SECONDS),
                    drivetrain.driveDistance(5.3, 0.5, 10)
                        .endsWhen(Flowable.timer(1300, TimeUnit.MILLISECONDS), true),
                    feeder.pulseSafe().delayStart(3300, TimeUnit.MILLISECONDS)
                        .delayFinish(15000, TimeUnit.MILLISECONDS),
                    climb.pulse(true).delayStart(6300, TimeUnit.MILLISECONDS)
                        .delayFinish(10300, TimeUnit.MILLISECONDS)
                )
            )
        ) , () -> drivetrain.driveTeleop().terminable(Flowable.never()));
  }
}
