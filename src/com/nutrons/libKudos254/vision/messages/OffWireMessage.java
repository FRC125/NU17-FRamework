package com.nutrons.libKudos254.vision.messages;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Used to convert Strings into OffWireMessage objects, which can be interpreted
 * as generic VisionMessages.
 */
public class OffWireMessage extends VisionMessage {

  private boolean validM = false;
  private String typeM = "unknown";
  private String messageM = "{}";

  /**
   * Sends an OffWireMessage with given string.
   * @param message the message.
   */
  public OffWireMessage(String message) {
    JSONParser parser = new JSONParser();
    try {
      JSONObject objJ = (JSONObject) parser.parse(message);
      typeM = (String) objJ.get("type");
      messageM = (String) objJ.get("message");
      validM = true;
    } catch (ParseException exception) {
      System.out.println("Exception caught");
    }
  }

  /**
   * Checks if validity is true.
   * @return boolean check validity
   */
  public boolean isValid() {
    return validM;
  }

  @Override
  public String getType() {
    return typeM;
  }

  @Override
  public String getMessage() {
    return messageM;
  }
}
