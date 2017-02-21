package com.nutrons.steamworks;

import static junit.framework.TestCase.assertTrue;

import com.nutrons.framework.controllers.ControllerEvent;
import com.nutrons.framework.controllers.RunAtPowerEvent;
import com.nutrons.framework.controllers.VirtualSpeedController;
import io.reactivex.Flowable;
import org.junit.Test;

public class TestDriveTime {

  @Test
  public void testDT() throws InterruptedException {
    final int[] record = new int[2];
    final int[] done = new int[1];
    long start = System.currentTimeMillis();
    Drivetrain dt = new Drivetrain(Flowable.never(), Flowable.never(),
        Flowable.never(), Flowable.never(),
        new VirtualSpeedController() {
          @Override
          public void accept(ControllerEvent ce) {
            assertTrue(ce instanceof RunAtPowerEvent);
            double power = ((RunAtPowerEvent) ce).power();
            assertTrue(power == 0.4 || power == 0.0);
            record[0] = 1;
            assertTrue(System.currentTimeMillis() - 2000 < start);
          }
        },
        new VirtualSpeedController() {
          @Override
          public void accept(ControllerEvent ce) {
            assertTrue(ce instanceof RunAtPowerEvent);
            double power = ((RunAtPowerEvent) ce).power();
            assertTrue(power == -0.4 || power == -0.0);
            record[1] = -1;
            assertTrue(System.currentTimeMillis() - 2000 < start);
          }
        });
    dt.driveTimeAction(500).execute(true);
    assertTrue(System.currentTimeMillis() - start < 1000);
    Thread.sleep(4000);
    assertTrue(record[0] == 1.0);
    assertTrue(record[1] == -1.0);
  }
}
