package com.nutrons.steamworks;

import com.nutrons.framework.StreamManager;
import com.nutrons.framework.commands.Command;
import com.nutrons.framework.controllers.VirtualSpeedController;
import com.nutrons.framework.util.CompMode;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class VirtualBootstrapper {
  static JFrame frame;

  public static void main(String[] args) {
    Flowable<Double> interval = Flowable.interval(10, TimeUnit.MILLISECONDS).map(Long::doubleValue).share();
    Flowable<Boolean> flip =  Flowable.interval(10, TimeUnit.MILLISECONDS).map(x -> false).share();
    frame = new JFrame();
    frame.setEnabled(true);
    frame.setSize(600, 400);
    frame.setVisible(true);
    VirtualSpeedController vs = new VirtualSpeedController();
    StreamManager sm = new StreamManager(key('e'), Flowable.just(CompMode.AUTO));
    Climbtake climb = new Climbtake(new VirtualSpeedController(), new VirtualSpeedController(), Flowable.never(), Flowable.never());
    Flowable never = Flowable.never();
    Drivetrain drivetrain = new Drivetrain(flip, Flowable.just(100.0), Flowable.just(1.0), Flowable.just(1.0), new VirtualSpeedController(), new VirtualSpeedController());
    Shooter shooter = new Shooter(new VirtualSpeedController(), flip, interval, interval);
    Feeder feeder = new Feeder(vs, vs, flip, flip);
    Turret turret = new Turret(Flowable.just(50.0), Flowable.just(10.0), vs, Flowable.just(0.0), Flowable.just(false));
    Observable.just(climb, drivetrain, shooter, feeder, turret).blockingSubscribe(sm::registerSubsystem);
    Command kpa40 = Command.parallel(turret.automagicMode().delayFinish(2, TimeUnit.SECONDS),
        shooter.pulse().delayStart(2, TimeUnit.SECONDS).delayFinish(12, TimeUnit.SECONDS),
        feeder.pulse().delayStart(4, TimeUnit.SECONDS).delayFinish(10, TimeUnit.SECONDS));


    sm.startCompetition(() ->
            Command.serial(
            drivetrain.driveDistance(9.5, 0.5, 10).endsWhen(Flowable.timer(3, TimeUnit.SECONDS), true),
        drivetrain.turn(-85, 10),
        drivetrain.driveDistance(5.0, 0.5, 10).endsWhen(Flowable.timer(3, TimeUnit.SECONDS), true),
        climb.pulse(true).delayFinish(500, TimeUnit.MILLISECONDS)).then(kpa40), () -> drivetrain.driveTeleop().terminable(Flowable.never()));
  }

  private static Flowable<Boolean> key(char c) {
    PublishProcessor pp = PublishProcessor.create();
    frame.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent keyEvent) {

      }

      @Override
      public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyChar() == c) {
          pp.onNext(true);
        }
      }

      @Override
      public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyChar() == c) {
          pp.onNext(false);
        }
      }
    });
    return pp.share();
  }

  private static Flowable<CompMode> compstream() {
    PublishProcessor pp = PublishProcessor.create();
    frame.addKeyListener(new KeyListener() {
      @Override
      public void keyTyped(KeyEvent keyEvent) {
        switch (keyEvent.getKeyChar()) {
          case 'a':
            pp.onNext(CompMode.AUTO);
          case 't':
            pp.onNext(CompMode.TELE);
        }
      }

      @Override
      public void keyPressed(KeyEvent keyEvent) {

      }

      @Override
      public void keyReleased(KeyEvent keyEvent) {

      }
    });
    return pp.share();
  }
}
