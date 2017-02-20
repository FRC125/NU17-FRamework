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
  double angleToTarget_x; //horizontal angle in degrees
  double angleToTarget_y; //vertical angle in degrees
  double distanceToTarget; //inches
  //default values
  State currentState = NONE;

  ;

  public void visionLogic(Block[] blocks) {
    //make sure we see two blocks
    if (blocks.length == 2) {
      //checks that vertical distance between blocks is greater
      // than horizontal distance between blocks, meaning that we are seeing the boiler target
      if (abs(blocks[0].y - blocks[1].y) > abs(blocks[0].x - blocks[1].x)) {
        angleToTarget_x = getHorizontalAngleOffset(blocks[0].x);
        angleToTarget_y = getVerticalAngleOffset(blocks[0].y, VERTICAL_ZERO_OFFSET_BOILER);
        distanceToTarget = getDistance(angleToTarget_y, TARGET_HEIGHT_BOILER);

      }
    }
  }

  public double getHorizontalAngleOffset(double x) {
    return (x * FOV_X / RESOLUTION_WIDTH) - 37.5;
  }

  public double getVerticalAngleOffset(double y, double verticalZeroOffset) {
    return (verticalZeroOffset - (y * FOV_Y / RESOLUTION_HEIGHT)) + CAMERA_ANGLE;
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
    private final double x;
    private final double y;

    Block(double x, double y) {
      this.x = x;
      this.y = y;
    }
  }
}
