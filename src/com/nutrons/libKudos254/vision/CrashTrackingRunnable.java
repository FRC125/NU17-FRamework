package com.nutrons.libKudos254.vision;

/**
 * Runnable class with reports all uncaught throws to CrashTracker.
 */
public abstract class CrashTrackingRunnable implements Runnable {

  @Override
  public final void run() {
    try {
      runCrashTracked();
    } catch (Throwable throwable) {
      CrashTracker.logThrowableCrash(throwable);
      throw throwable;
    }
  }

  public abstract void runCrashTracked();
}