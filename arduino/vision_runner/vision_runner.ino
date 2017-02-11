#include <SPI.h>
#include <Pixy.h>

Pixy pixy;
uint16_t blocks;
String toSend;
char buf[32]; 
double arr[2];

  
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
  blocks = pixy.getBlocks();

  if (blocks) {
     angleToTarget_x = getHorizontalAngleOffset(pixy.blocks[0].x);
     angleToTarget_y = getVerticalAngleOffset(pixy.blocks[0].y);
     distanceToTarget = getDistance();
     arr[0] = distanceToTarget;
     arr[1] = angleToTarget_x;
 
     toSend = String(getDistance(), 4).substring(0,5) + ":" + String(angleToTarget_x, 4).substring(0,5) + "\n";
     byte sendBytes[toSend.length() + 1];
     toSend.getBytes(sendBytes, toSend.length() + 1);
     Serial.write(sendBytes, toSend.length() + 1);
     Serial.flush();
  }

  //If we don't see anything, just send -1000 for both values!
  toSend = String(-1000.0, 5).substring(0,5) + ":" + String(-1000.0, 4).substring(0,5) + "\n";
  byte sendBytes[toSend.length() + 1];
  toSend.getBytes(sendBytes, toSend.length() + 1);
  Serial.write(sendBytes, toSend.length() + 1);
  Serial.flush();
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
