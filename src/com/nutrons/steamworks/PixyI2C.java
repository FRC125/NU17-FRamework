package com.nutrons.steamworks;

import static com.nutrons.steamworks.PixyI2C.State.NONE;
import static java.lang.Math.abs;
import static java.lang.Math.tan;

public class PixyI2C {

  private static final double FOV_X = 75; //horizontal Field of View in degrees
  private static final double FOV_Y = 47; //vertical Field of View in degrees
  private static final double RESOLUTION_WIDTH = 320; //in pixels, 320 x 200
  private static final double RESOLUTION_HEIGHT = 200; //in pixels
  private static final double CAMERA_ANGLE = 0.0; //in degrees -- see wiki
  private static final double CAMERA_HEIGHT = 0.0; //in inches
  //TODO: Edit these values
  private static final double VERTICAL_ZERO_OFFSET_BOILER = 0.0; //in degrees -- see wiki
  private static final double TARGET_HEIGHT_BOILER = 0.0; //in inches
  private static final double VERTICAL_ZERO_OFFSET_GEAR = 0.0; //in degrees -- see wiki
  private static final double TARGET_HEIGHT_GEAR = 0.0; //in inches
  int blocks;
  String toSend;
  String stateToSend;
  double angleToTargetX; //horizontal angle in degrees
  double angleToTargetY; //vertical angle in degrees
  double distanceToTarget; //inches
  //default values
  State currentState = NONE;

  /** Makes sure we see two blocks.
   * Then takes the horizontal distance between blocks,
   * meaning that we are seeing the boiler target
   * @param blocks Array of blocks
   */
  public void visionLogic(Block[] blocks) {
    if (blocks.length == 2) {
      if (abs(blocks[0].yval - blocks[1].yval) > abs(blocks[0].xval - blocks[1].xval)) {
        angleToTargetX = getHorizontalAngleOffset(blocks[0].xval);
        angleToTargetY = getVerticalAngleOffset(blocks[0].yval, VERTICAL_ZERO_OFFSET_BOILER);
        distanceToTarget = getDistance(angleToTargetY, TARGET_HEIGHT_BOILER);

      }
    }
  }

  public double getHorizontalAngleOffset(double angleOffsetX) {
    return (angleOffsetX * FOV_X / RESOLUTION_WIDTH) - 37.5;
  }

  public double getVerticalAngleOffset(double angleOffsetY, double verticalZeroOffset) {
    return (verticalZeroOffset - (angleOffsetY * FOV_Y / RESOLUTION_HEIGHT)) + CAMERA_ANGLE;
  }

  //******THE IMPORTANT STUFF******

  public double degreesToRadians(double deg) {
    return (deg * 3.1415926) / 180;
  }

  public double getDistance(double angleToTargetY, double targetHeight) {
    return (targetHeight - CAMERA_HEIGHT) / tan(degreesToRadians((angleToTargetY)));
    // You have been Visited From the cat god =(-|-)=
  }

  enum State {
    NONE,
    BOIL,
    GEAR
  }

  class Block {

    private final double xval;
    private final double yval;

    Block(double blockX, double blockY) {
      this.xval = blockX;
      this.yval = blockY;
    }
  }
}
