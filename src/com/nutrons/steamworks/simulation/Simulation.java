package com.nutrons.steamworks.simulation;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.Callable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Simulation extends Application {
  private static HashMap<String, Flowable<String>> gamepads;

  public static void main(String[] args) {
    launch(args);
  }

  private synchronized static Flowable<String> gamepad(String deviceID) {
    if (gamepads == null) {
      gamepads = new HashMap<>();
    }
    if (!gamepads.containsKey(deviceID)) {
      Callable<BufferedReader> reader = () -> new BufferedReader(new InputStreamReader(new ProcessBuilder("evtest",
          "/dev/input/by-id/" + deviceID).start().getInputStream()));
      Flowable<String> stdout = Flowable.generate(reader, (r, e) -> {
        String read = r.readLine();
        if (read != null) {
          e.onNext(read);
        }
        return r;
      });
      gamepads.put(deviceID, stdout.publish().autoConnect().subscribeOn(Schedulers.io()));
    }
    return gamepads.get(deviceID);
  }

  public static Flowable<String> events(String deviceID, String type, String inputID) {
    String keyword1 = "(" + type + ")";
    String keyword2 = "(" + inputID + ")";
    return gamepad(deviceID).filter(x -> x.contains(keyword1) && x.contains(keyword2));
  }

  public static Flowable<Double> joy(String deviceID, String inputID) {
    return events(deviceID, "EV_ABS", inputID).filter(x -> x.indexOf("value") != 0).map(x -> x.substring(x.indexOf("value") + 6)).map(Double::valueOf).map(x -> x / 128.0 - 1.0);
  }

  public static Flowable<Boolean> button(String deviceID, String inputID) {
    return events(deviceID, "EV_KEY", inputID).filter(x -> x.indexOf("value") != 0).map(x -> x.substring(x.indexOf("value") + 6)).map(x -> !"0".equals(x.trim()));
  }

  @Override
  public void start(Stage stage) throws Exception {
    Pane simulation = FXMLLoader.load(Simulation.class.getResource("simulation.fxml"));
    stage.setScene(new Scene(simulation));
    stage.show();
    Schedulers.io().scheduleDirect(() -> Controller.instance.startCompetition());
  }
}
