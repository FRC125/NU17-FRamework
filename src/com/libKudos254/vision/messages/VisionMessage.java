package com.libKudos254.vision.messages;

import org.json.simple.JSONObject;

/**
 * An abstract class used for messages about the vision subsystem.
 */
public abstract class VisionMessage {

  public abstract String getType();

  public abstract String getMessage();

  /**
   * Returns JSONObject as a string.
   * @return returns the JSONObject as a string
   */
  public String toJson() {
    JSONObject jsonObj = new JSONObject();
    jsonObj.put("type", getType());
    jsonObj.put("message", getMessage());
    return jsonObj.toString();
  }

}
