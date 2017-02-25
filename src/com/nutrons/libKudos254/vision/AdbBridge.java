package com.nutrons.libKudos254.vision;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AdbBridge interfaces to an Android Debug Bridge (adb) binary, which is needed
 * to communicate to Android devices over USB.
 * adb binary provided by https://github.com/Spectrum3847/RIOdroid
 */
public class AdbBridge {
  Path binLocation;
  public static final Path DEFAULT_LOCATION = Paths.get("/usr/bin/adb");

  /**
   * Makes an ADBBridge.
   */
  public AdbBridge() {
    Path adbLocation;
    String envVal = System.getenv("FRC_ADB_LOCATION");
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
    Runtime runtime = Runtime.getRuntime();
    String cmd = binLocation.toString() + " " + args;

    try {
      Process process = runtime.exec(cmd);
      process.waitFor();
    } catch (IOException exception) {
      System.err.println("AdbBridge: Could not run command " + cmd);
      exception.printStackTrace();
      return false;
    } catch (InterruptedException exception) {
      System.err.println("AdbBridge: Could not run command " + cmd);
      exception.printStackTrace();
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
   * Restarts the adbBridge.
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
   * Restarts the adb app.
   */
  public void restartApp() {
    System.out.println("Restarting app");
    runCommand("shell am force-stop com.team254.cheezdroid \\; "
        + "am start com.team254.cheezdroid/com.team254.cheezdroid.VisionTrackerActivity");
  }
}
