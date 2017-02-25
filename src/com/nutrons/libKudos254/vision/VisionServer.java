package com.nutrons.libKudos254.vision;

import com.nutrons.libKudos254.vision.messages.HeartbeatMessage;
import com.nutrons.libKudos254.vision.messages.OffWireMessage;
import com.nutrons.libKudos254.vision.messages.VisionMessage;
import edu.wpi.first.wpilibj.Timer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This controls all vision actions, including vision updates, capture, and
 * interfacing with the Android phone with Android Debug Bridge. It also stores
 * all VisionUpdates (from the Android phone) and contains methods to add
 * to/prune the VisionUpdate list. Much like the subsystems, outside methods get
 * the VisionServer instance (there is only one VisionServer) instead of
 * creating new VisionServer instances.
 *
 * @see
 */

public class VisionServer extends CrashTrackingRunnable {

  public static int kAndroidAppTcpPort = 8254; //TODO: MIGHT NEED TO CHANGE THIS

  private static VisionServer s_instance = null;
  private ServerSocket mServerSocket;
  private boolean mRunning = true;
  private int mPort;
  private ArrayList<VisionUpdateReceiver> receivers = new ArrayList<>();
  AdbBridge adb = new AdbBridge();
  double lastMessageReceivedTime = 0;
  private boolean mUseJavaTime = false;

  private ArrayList<ServerThread> serverThreads = new ArrayList<>();
  private volatile boolean wantsAppRestart = false;

  /**
   * Gets instance of vision server
   */
  public static VisionServer getInstance() {
    System.out.println("VisionServer instance created");
    if (s_instance == null) {
      s_instance = new VisionServer(kAndroidAppTcpPort);
    }
    return s_instance;
  }

  private boolean isConnect = false;

  public boolean isConnected() {
    return isConnect;
  }

  public void requestAppRestart() {
    wantsAppRestart = true;
  }

  protected class ServerThread extends CrashTrackingRunnable {
    private Socket socket;

    public ServerThread(Socket socket) {
      this.socket = socket;
    }

    public void send(VisionMessage message) {
      String toSend = message.toJson() + "\n";
      if (socket != null && socket.isConnected()) {
        try {
          OutputStream os = socket.getOutputStream();
          os.write(toSend.getBytes());
        } catch (IOException exception) {
          System.out.println("Vision server IOException");
          System.err.println("VisionServer: Could not send data to socket");
        }
      }
    }

    public void handleMessage(VisionMessage message, double timestamp) {
      if ("targets".equals(message.getType())) {
        VisionUpdate update = VisionUpdate.generateFromJsonString(timestamp, message.getMessage());
        receivers.removeAll(Collections.singleton(null));
        if (update.isValid()) {
          for (VisionUpdateReceiver receiver : receivers) {
            receiver.gotUpdate(update);
          }
        }
      }
      if ("heartbeat".equals(message.getType())) {
        send(HeartbeatMessage.getInstance());
      }
    }

    public boolean isAlive() {
      return socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public void runCrashTracked() {
      if (socket == null) {
        return;
      }
      try {
        InputStream is = socket.getInputStream();
        byte[] buffer = new byte[2048];
        int read;
        while (socket.isConnected() && (read = is.read(buffer)) != -1) {
          double timestamp = getTimestamp();
          lastMessageReceivedTime = timestamp;
          String messageRaw = new String(buffer, 0, read);
          String[] messages = messageRaw.split("\n");
          for (String message : messages) {
            OffWireMessage parsedMessage = new OffWireMessage(message);
            if (parsedMessage.isValid()) {
              handleMessage(parsedMessage, timestamp);
            }
          }
        }
        System.out.println("Socket disconnected");
      } catch (IOException e) {
        System.err.println("Could not talk to socket");
      }
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Instantializes the VisionServer and connects to ADB via the specified
   * port.
   *
   * @param
   */
  private VisionServer(int port) {
    try {
      adb = new AdbBridge();
      mPort = port;
      mServerSocket = new ServerSocket(port);
      adb.start();
      adb.reversePortForward(port, port);
      try {
        String useJavaTime = System.getenv("USE_JAVA_TIME");
        mUseJavaTime = "true".equals(useJavaTime);
      } catch (NullPointerException e) {
        mUseJavaTime = false;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    new Thread(this).start();
    new Thread(new AppMaintainanceThread()).start();
  }

  public void restartAdb() {
    adb.restartAdb();
    adb.reversePortForward(mPort, mPort);
  }

  /**
   * If a VisionUpdate object (i.e. a target) is not in the list, add it.
   *
   * @see VisionUpdate
   */
  public void addVisionUpdateReceiver(VisionUpdateReceiver receiver) {
    System.out.println("added vision update receiver");
    if (!receivers.contains(receiver)) {
      receivers.add(receiver);
    }
  }

  public void removeVisionUpdateReceiver(VisionUpdateReceiver receiver) {
    if (receivers.contains(receiver)) {
      receivers.remove(receiver);
    }
  }

  @Override
  public void runCrashTracked() {
    while (mRunning) {
      try {
        Socket p = mServerSocket.accept();
        ServerThread s = new ServerThread(p);
        new Thread(s).start();
        serverThreads.add(s);
      } catch (IOException e) {
        System.err.println("Issue accepting socket connection!");
      } finally {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class AppMaintainanceThread extends CrashTrackingRunnable {
    @Override
    public void runCrashTracked() {
      while (true) {
        if (getTimestamp() - lastMessageReceivedTime > .1) {
          // camera disconnected
          adb.reversePortForward(mPort, mPort);
          isConnect = false;
        } else {
          isConnect = true;
        }
        if (wantsAppRestart) {
          adb.restartApp();
          wantsAppRestart = false;
        }
        try {
          Thread.sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private double getTimestamp() {
    if (mUseJavaTime) {
      return System.currentTimeMillis();
    } else {
      return Timer.getFPGATimestamp();
    }
  }
}
