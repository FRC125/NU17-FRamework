package com.libKudos254.vision;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AdbBridge interfaces to an Android Debug Bridge (adb) binary, which is needed
 * to communicate to Android devices over USB.
 * <p/>
 * adb binary provided by https://github.com/Spectrum3847/RIOdroid
 */
public class AdbBridge {
  Path binLocation;
  public static final Path DEFAULT_LOCATION = Paths.get("/usr/bin/adb");

  /**
   *  Constructor for AdBridge.
   */
  public AdbBridge() {
    Path adbLocation;
    String envVal = System.getenv("FRC_adbLocation");
    if (envVal == null || "".equals(envVal)) {
      adbLocation = DEFAULT_LOCATION;
    } else {
      adbLocation = Paths.get(envVal);
    }
    binLocation = adbLocation;
  }

  public AdbBridge(Path location) {
    binLocation = location;
  }

  private boolean runCommand(String args) {
    Runtime run = Runtime.getRuntime();
    String cmd = binLocation.toString() + " " + args;
    try {
      Process proc = run.exec(cmd);
      proc.waitFor();
    } catch (IOException exc) {
      System.err.println("AdbBridge: Could not run command " + cmd);
      exc.printStackTrace();
      return false;
    } catch (InterruptedException exc) {
      System.err.println("AdbBridge: Could not run command " + cmd);
      exc.printStackTrace();
      return false;
    }
    return true;
  }
    
  public void start() {
    System.out.println("Starting adb");
    runCommand("start");
  }

  public void stop() {
    System.out.println("Stopping adb");
    runCommand("kill-server");
  }

  /**
   * Stops and starts the adb, restarting it.
   */
  public void restartAdb() {
    System.out.println("Restarting adb");
    stop();
    start();
  }

  public void portForward(int localPort, int remotePort) {
    runCommand("forward tcp:" + localPort + " tcp:" + remotePort);
  }

  public void reversePortForward(int remotePort, int localPort) {
    runCommand("reverse tcp:" + remotePort + " tcp:" + localPort);
  }

  /**
   * Restarts the app.
   */
  public void restartApp() {
    System.out.println("Restarting app");
    runCommand("shell am force-stop com.team254.cheezdroid \\; "
            + "am start com.team254.cheezdroid/com.team254.cheezdroid.VisionTrackerActivity");
  }
}
