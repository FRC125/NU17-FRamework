package com.nutrons.steamworks;

import io.reactivex.Flowable;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

public class TestDriveTime {

    @Test
    public void testDT() throws InterruptedException {
//    final int[] record = new int[2];
//    final int[] done = new int[1];
//    long start = System.currentTimeMillis();
//    Drivetrain dt = new Drivetrain(Flowable.never(), Flowable.never(), Flowable.never(),
//        Flowable.never(), Flowable.never(),
//        x -> {
//          assertTrue(x instanceof RunAtPowerEvent);
//          assertTrue(((RunAtPowerEvent) x).power() == 1.0);
//          record[0] = 1;
//          assertTrue(System.currentTimeMillis() - 2000 < start);
//        },
//        x -> {
//          assertTrue(x instanceof RunAtPowerEvent);
//          assertTrue(((RunAtPowerEvent) x).power() == -1.0);
//          record[1] = -1;
//          assertTrue(System.currentTimeMillis() - 2000 < start);
//        });
//    dt.driveTimeAction(500).startExecution();
//    assertTrue(System.currentTimeMillis() - start < 1000);
//    Thread.sleep(4000);
//    assertTrue(record[0] == 1.0);
//    assertTrue(record[1] == -1.0);
    }
}
