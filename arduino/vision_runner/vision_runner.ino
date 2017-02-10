#include <SPI.h>
#include <Pixy.h>

Pixy pixy;
uint16_t blocks;
String toSend;
String TARGET_TYPE;
char buf[32]; 
String STATE;
  
//******ANGLE VARIABLES******
double angleToTarget_x; //horizontal angle in degrees
double angleToTarget_y; //vertical angle in degrees
double FOV_X = 75; //horizontal Field of View in degrees
double FOV_Y = 47; //vertical Field of View in degrees
double RESOLUTION_WIDTH = 320; //in pixels, 320 x 200
double RESOLUTION_HEIGHT = 200; //in pixels
double VERTICAL_ZERO_OFFSET = 23.5; //shifts the 0 degree line down by given value, relies on consistent angle of camera
double CAMERA_ANGLE = 19; //angle of Camera

//******DISTANCE VARIABLES******
double distanceToTarget; //inches
double CAMERA_HEIGHT = 86; //inches
double TARGET_HEIGHT = 42.5; //inches

void setup() {
  Serial.begin(9600);
  pixy.init();
}

void loop() { 
  blocks = pixy.getBlocks(2);

  if (blocks == 2) { //make sure we see two blocks
     if(abs(pixy.blocks[0].y - pixy.blocks[1].y) > abs(pixy.blocks[0].x - pixy.blocks[1].x)){ //checks that vertical distance between blocks is greater than horizontal distance between blocks, meaning that we are seeing the boiler target
       STATE = "BOIL";
       updateValuesBoiler();
       writeBytes(distanceToTarget, angleToTarget_x);
     }else{ //then that means we're seeing the gears!
       //TODO: CHANGE THESE CONSTANTS
       VERTICAL_ZERO_OFFSET = 23.5;
       CAMERA_HEIGHT = 86; //inches
       TARGET_HEIGHT = 42.5; //inches

       STATE = "GEAR";
       
       updateValuesGears();
       writeBytes(distanceToTarget, angleToTarget_x);
     }
  }

  //If we don't see anything, just send -1000 for both values!
  STATE = "NONE";
  writeBytes(-1000.0, -1000.0);
}

//******THE IMPORTANT STUFF******

double getHorizontalAngleOffset(double x){
  return (x*FOV_X/RESOLUTION_WIDTH) - 37.5;
}

double getVerticalAngleOffset(double y) {
  return (VERTICAL_ZERO_OFFSET - (y*FOV_Y/RESOLUTION_HEIGHT )) + CAMERA_ANGLE; //
}

double degreesToRadians(double deg){
  return (deg * 3.1415926)/180;
}

double getDistance(){
  return (TARGET_HEIGHT-CAMERA_HEIGHT)/tan(degreesToRadians((angleToTarget_y)));
}

void updateValuesBoiler(){
  angleToTarget_x = getHorizontalAngleOffset(pixy.blocks[0].x);
  angleToTarget_y = getVerticalAngleOffset(pixy.blocks[0].y);
  distanceToTarget = getDistance();
}

void updateValuesGears(){
  angleToTarget_x = getHorizontalAngleOffset((pixy.blocks[0].x + pixy.blocks[1].x) / 2); //find the midline between the gear targets, this is what we want to turn to
  angleToTarget_y = getVerticalAngleOffset(pixy.blocks[0].y);
  distanceToTarget = getDistance();
}

void writeBytes(double distance, double angle){
  toSend = STATE + ":" + String(distance, 4).substring(0,5) + ":" + String(angle, 4).substring(0,5) + "\n";
  byte sendBytes[toSend.length() + 1];
  toSend.getBytes(sendBytes, toSend.length() + 1);
  Serial.write(sendBytes, toSend.length() + 1);
  Serial.flush();
}
