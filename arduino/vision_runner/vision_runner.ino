#include <SPI.h>
#include <Pixy.h>

Pixy pixy;
uint16_t blocks;
String toSend;
String stateToSend; 
  
struct Vars {
  double VERTICAL_ZERO_OFFSET;
  double CAMERA_ANGLE;
  double CAMERA_HEIGHT;
  double TARGET_HEIGHT;
};

const double FOV_X = 75; //horizontal Field of View in degrees
const double FOV_Y = 47; //vertical Field of View in degrees
const double RESOLUTION_WIDTH = 320; //in pixels, 320 x 200
const double RESOLUTION_HEIGHT = 200; //in pixels
double angleToTarget_x; //horizontal angle in degrees
double angleToTarget_y; //vertical angle in degrees
double distanceToTarget; //inches

enum state {
  NONE,
  BOIL,
  GEAR
};

//default values
state currentState = NONE;
Vars constants = {23.5, 19, 86, 42.5};

void setup() {
  Serial.begin(9600);
  pixy.init();
}

void loop() { 
  blocks = pixy.getBlocks(2);
  
  //make sure we see two blocks
  if (blocks == 2) { 
     if(abs(pixy.blocks[0].y - pixy.blocks[1].y) > abs(pixy.blocks[0].x - pixy.blocks[1].x)){ //checks that vertical distance between blocks is greater than horizontal distance between blocks, meaning that we are seeing the boiler target
       constants = {23.5, 19, 86, 42.5};
       currentState = BOIL;
       updateValuesBoiler(pixy.blocks[0].x, pixy.blocks[0].y);
       writeBytes(distanceToTarget, angleToTarget_x);
     }else{ //then that means we're seeing the gears!
       constants = {23.5, 19, 86, 42.5};
       currentState = GEAR;
       updateValuesGears(pixy.blocks[0].x, pixy.blocks[1].x, pixy.blocks[0].y);
       writeBytes(distanceToTarget, angleToTarget_x);
     }
  }

  //If we don't see anything, just send -1000 for both values!
  currentState = NONE;
  writeBytes(-1000.0, -1000.0);
}

//******THE IMPORTANT STUFF******

double getHorizontalAngleOffset(double x){
  return (x*FOV_X/RESOLUTION_WIDTH) - 37.5;
}

double getVerticalAngleOffset(double y) {
  return (constants.VERTICAL_ZERO_OFFSET - (y*FOV_Y/RESOLUTION_HEIGHT )) + constants.CAMERA_ANGLE; //
}

double degreesToRadians(double deg){
  return (deg * 3.1415926)/180;
}

double getDistance(double angleToTargetY){
  return (constants.TARGET_HEIGHT-constants.CAMERA_HEIGHT)/tan(degreesToRadians((angleToTargetY)));
}

void updateValuesBoiler(double x, double y){
  angleToTarget_x = getHorizontalAngleOffset(x);
  angleToTarget_y = getVerticalAngleOffset(y);
  distanceToTarget = getDistance(angleToTarget_y);
}

void updateValuesGears(double x1, double x2, double y){
  angleToTarget_x = getHorizontalAngleOffset((x1 + x2) / 2); //find the midline between the gear targets, this is what we want to turn to
  angleToTarget_y = getVerticalAngleOffset(y);
  distanceToTarget = getDistance(angleToTarget_y);
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

