#include <SPI.h>
#include <Pixy.h>

Pixy pixy;
uint16_t blocks;
String toSend;
String stateToSend; 

const double FOV_X = 75; //horizontal Field of View in degrees
const double FOV_Y = 47; //vertical Field of View in degrees
const double RESOLUTION_WIDTH = 320; //in pixels, 320 x 200
const double RESOLUTION_HEIGHT = 200; //in pixels
const double CAMERA_ANGLE = 0.0; //in degrees -- see wiki
const double CAMERA_HEIGHT = 0.0; //in inches

//TODO: Edit these values
const double VERTICAL_ZERO_OFFSET_BOILER = 23.5; //in degrees -- see wiki
const double TARGET_HEIGHT_BOILER = 84.0; //in inches

const double VERTICAL_ZERO_OFFSET_GEAR = 23.5; //in degrees -- see wiki
const double TARGET_HEIGHT_GEAR = 10.75; //in inches

double angleToTarget_x; //horizontal angle in degrees
double angleToTarget_y; //vertical angle in degrees
double distanceToTarget; //inches

double latestDistance;
double latestAngle;

enum state {
  NONE,
  BOIL,
  GEAR
};

//default values
state currentState = NONE;

void setup() {
  Serial.begin(9600);
  pixy.init();
}

void loop() { 
  blocks = pixy.getBlocks();
  
  //make sure we see two blocks
  if (blocks) { 
     if(abs(pixy.blocks[0].y - pixy.blocks[1].y) > abs(pixy.blocks[0].x - pixy.blocks[1].x)){ //checks that vertical distance between blocks is greater than horizontal distance between blocks, meaning that we are seeing the boiler target
       angleToTarget_x = getHorizontalAngleOffset(pixy.blocks[0].x);
       angleToTarget_y = getVerticalAngleOffset(pixy.blocks[0].y, VERTICAL_ZERO_OFFSET_BOILER);
       distanceToTarget = getDistance(angleToTarget_y, TARGET_HEIGHT_BOILER);
       if(angleToTarget_x != 0){
        latestAngle = angleToTarget_x;
       }

       currentState = BOIL;
       writeBytes(distanceToTarget, angleToTarget_x);
     }else{ //then that means we're seeing the gears!
       angleToTarget_x = getHorizontalAngleOffset(pixy.blocks[0].x);
       angleToTarget_y = getVerticalAngleOffset(pixy.blocks[0].y, VERTICAL_ZERO_OFFSET_GEAR);
       distanceToTarget = getDistance(angleToTarget_y, TARGET_HEIGHT_GEAR);
       if(angleToTarget_x != 0.0){
        latestAngle = angleToTarget_x;
       }
       
       currentState = GEAR;
       writeBytes(distanceToTarget, angleToTarget_x);
     }
  }else{
    //If we don't see anything, just send 0.0 for both values!
  currentState = NONE;
  writeBytes(latestAngle, latestAngle);
  }
 
}

//******THE IMPORTANT STUFF******

double getHorizontalAngleOffset(double x){
  return (x*FOV_X/RESOLUTION_WIDTH) - 37.5;
}

double getVerticalAngleOffset(double y, double verticalZeroOffset) {
  return (verticalZeroOffset - (y*FOV_Y/RESOLUTION_HEIGHT )) + CAMERA_ANGLE; 
}

double degreesToRadians(double deg){
  return (deg * 3.1415926)/180;
}

double getDistance(double angleToTargetY, double targetHeight){
  return (targetHeight - CAMERA_HEIGHT)/tan(degreesToRadians((angleToTargetY)));
}

void writeBytes(double distance, double angle){
  switch(currentState) {
    case NONE:
      stateToSend = "NONE";
      break;
    case GEAR:
      stateToSend = "GEAR";
      break;
    case BOIL:
      stateToSend = "BOIL";
      break;
    default:
      Serial.print("Easter egg! You have been visited by the cat of good wishes! =(^-^)=");
      break;
  }
  toSend = stateToSend + ":" + String(distance, 4).substring(0,5) + ":" + String(angle, 4).substring(0,5) + "\n";
  byte sendBytes[toSend.length() + 1];
  toSend.getBytes(sendBytes, toSend.length() + 1);
  Serial.write(sendBytes, toSend.length() + 1);
  Serial.flush();
}

