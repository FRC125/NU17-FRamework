package com.nutrons.libKudos254.vision.messages;

/**
 * A message that acts as a "heartbeat"- ensures that the vision system is working,
 * the message simply contains the instance of the VisionServer object.
 */
public class HeartbeatMessage extends VisionMessage {

  static HeartbeatMessage sInst = null;

  /**
   * Gets the latest instance of the HeartbeatMessage.
   * @return the most latest instance.
   */
  public static HeartbeatMessage getInstance() {
    if (sInst == null) {
      sInst = new HeartbeatMessage();
    }
    return sInst;
  }

  @Override
  public String getType() {
    return "heartbeat";
  }

  @Override
  public String getMessage() {
    return "{}";
  }
}
