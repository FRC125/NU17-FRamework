package com.libKudos254.vision.messages;

/**
 * A Message that contains and can set the state of the camera and intake
 * systems.
 */
public class SetCameraModeMessage extends VisionMessage {

  private static final String K_VISION_MODE = "vision";
  private static final String K_INTAKE_MODE = "intake";

  private String message = K_VISION_MODE;

  private SetCameraModeMessage(String message) {
    this.message = message;
  }

  public static SetCameraModeMessage getVisionModeMessage() {
    return new SetCameraModeMessage(K_VISION_MODE);
  }

  public static SetCameraModeMessage getIntakeModeMessage() {
    return new SetCameraModeMessage(K_INTAKE_MODE);
  }

  @Override
  public String getType() {
    return "camera_mode";
  }

  @Override
  public String getMessage() {
    return message;
  }
}
