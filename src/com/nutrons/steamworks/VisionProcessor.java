package com.nutrons.steamworks;

import com.nutrons.framework.util.FlowOperators;
import com.libKudos254.Rotation2d;
import com.libKudos254.vision.TargetInfo;
import com.libKudos254.vision.VisionUpdate;
import com.libKudos254.vision.VisionUpdateReceiver;
import io.reactivex.Flowable;

/**
 * Kudos 254 for letting us use your code and app.
 * Thanks so much to Tombot for helping us so much! =(^_^)=
 */
public class VisionProcessor implements VisionUpdateReceiver {

  public static final double CENTER_OF_TARGET_HEIGHT = 40.5;
  // Pose of the camera frame w.r.t. the turret frame
  public static double CAMERA_INCHES_FROM_FLOOR = 24.75;
  public static double kCameraPitchAngleDegrees = 25.5;
  public static double kCameraYawAngleDegrees = 0.2;
  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update = null;
  double differentialHeight = CENTER_OF_TARGET_HEIGHT - CAMERA_INCHES_FROM_FLOOR;
  Rotation2d cameraPitchCorrection = Rotation2d.fromDegrees(kCameraPitchAngleDegrees);
  Rotation2d cameraYawRotation = Rotation2d.fromDegrees(kCameraYawAngleDegrees);
  double distance;

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    return instance_;
  }

  /**
   * Gets the horizontal angle offset from the target.
   * @return horizontal angle offset from the target
   */
  public double getYawHorizAngle() {
    if (!(update.getTargets() == null || update.getTargets().isEmpty())) {
      for (TargetInfo target : update.getTargets()) {
        double yawAngleRadians = Math.atan2(target.getY(), target.getX());
        return yawAngleRadians;
      }
    }
    return 0.0;
  }

  /**
   * TODO: get the vertical angle offset from the target.
   * @return vertical angle offset from the target
   */
  public double getPitchVertAngle() {
    if (!(update.getTargets() == null || update.getTargets().isEmpty())) {
      for (TargetInfo target : update.getTargets()) {
        double pitchAngleRadians = Math.atan2(target.getZ(), target.getX());
        return pitchAngleRadians;
      }
    }
    return 0.0;
  }

  double getDistance() {
    if (!(update.getTargets() == null || update.getTargets().isEmpty())) {
      for (TargetInfo target : update.getTargets()) {

        distance = differentialHeight / Math.tan(Math.atan(target.getZ() / target.getX()) + Math.toRadians(kCameraPitchAngleDegrees));
        return distance;
        /**double xyaw = target.getX() * cameraYawRotation.cos() + cameraYawRotation.sin();
        double yyaw = cameraYawRotation.cos() - target.getX() * cameraYawRotation.sin();
        double zyaw = target.getZ();

        double xr = zyaw * cameraPitchCorrection.sin() + xyaw * cameraPitchCorrection.cos();
        double yr = yyaw;
        double zr = zyaw * cameraPitchCorrection.cos() - xyaw * cameraPitchCorrection.sin();

        if (zr > 0) {
          double scaling = differentialHeight / zr;
          System.out.println(distance + "DISTANCE");
          distance = Math.hypot(xr, yr) * scaling;
          return distance;
        }**/


      }
    }
    return 0.0;
  }

  public Flowable<Double> getHorizAngleFlow() {
    return FlowOperators.toFlowFast(() -> this.getYawHorizAngle());
  }

  @Override
  public synchronized void gotUpdate(VisionUpdate update) {
    this.update = update;
  }

}