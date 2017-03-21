package com.libKudos254.vision;

/**
 * Runnable class with reports all uncaught throws to CrashTracker.
 */
public abstract class CrashTrackingRunnable implements Runnable {

  @Override
  public final void run() {
    try {
      runCrashTracked();
    } catch (Throwable exc) {
      CrashTracker.logThrowableCrash(exc);
      throw exc;
    }
  }

  public abstract void runCrashTracked();
}