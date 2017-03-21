package com.libKudos254.vision.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Used to convert Strings into OffWireMessage objects, which can be interpreted
 * as generic VisionMessages.
 */
public class OffWireMessage extends VisionMessage {

  private boolean valid = false;
  private String type = "unknown";
  private String message = "{}";

  /**
   * Initializes OffWireMessage object.
   * @param message specifies message
   */
  public OffWireMessage(String message) {
    JSONParser parser = new JSONParser();
    try {
      JSONObject jsonObj = (JSONObject) parser.parse(message);
      type = (String) jsonObj.get("type");
      this.message = (String) jsonObj.get("message");
      valid = true;
    } catch (ParseException exc) {
      exc.printStackTrace();
    }
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getMessage() {
    return message;
  }
}
