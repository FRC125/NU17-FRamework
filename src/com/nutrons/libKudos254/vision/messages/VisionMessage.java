package com.nutrons.libKudos254.vision.messages;

import org.json.simple.JSONObject;

/**
 * An abstract class used for messages about the vision subsystem.
 */
public abstract class VisionMessage {

  public abstract String getType();

  public abstract String getMessage();

  /**
   * Turns the VisionMessage into a String type.
   * @return VisionMessage String.
   */
  public String toJson() {
    JSONObject objJ = new JSONObject();
    objJ.put("type", getType());
    objJ.put("message", getMessage());
    return objJ.toString();
  }

}
