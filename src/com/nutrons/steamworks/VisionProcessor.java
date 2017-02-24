package com.nutrons.steamworks;

import com.nutrons.libKudos254.Rotation2d;
import com.nutrons.libKudos254.vision.TargetInfo;
import com.nutrons.libKudos254.vision.VisionUpdate;
import com.nutrons.libKudos254.vision.VisionUpdateReceiver;
import io.reactivex.Flowable;
import com.nutrons.framework.util.FlowOperators;

/**
 * Kudos 254 for letting us use your code and app.
 * Thanks so much to Tombot for helping us so much! =(^_^)=
 */
public class VisionProcessor implements VisionUpdateReceiver {

  public static final double CENTER_OF_TARGET_HEIGHT = 89.0;
  // Pose of the camera frame w.r.t. the turret frame
  public static double CAMERA_INCHES_FROM_FLOOR = 20.0;
  public static double kCameraPitchAngleDegrees = 25.5;
  public static double kCameraYawAngleDegrees = 0.0;
  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update = null;
  double differentialHeight = CENTER_OF_TARGET_HEIGHT - CAMERA_INCHES_FROM_FLOOR;
  Rotation2d cameraPitchCorrection = Rotation2d.fromDegrees(kCameraPitchAngleDegrees);
  Rotation2d cameraYawRotation = Rotation2d.fromDegrees(kCameraYawAngleDegrees);

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    System.out.println("VisionProcessor gotInstance");
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
        return target.getZ();
      }
    }
    return 0.0;
  }

  double getDistance() {
    if (!(update.getTargets() == null || update.getTargets().isEmpty())) {
      for (TargetInfo target : update.getTargets()) {
        double xyaw = target.getX() * cameraYawRotation.cos() + cameraYawRotation.sin();
        double yyaw = cameraYawRotation.cos() - target.getX() * cameraYawRotation.sin();
        double zyaw = target.getZ();

        // Compensate for camera pitch
        double xr = zyaw * cameraPitchCorrection.sin() + xyaw * cameraPitchCorrection.cos();
        double yr = yyaw;
        double zr = zyaw * cameraPitchCorrection.cos() - xyaw * cameraPitchCorrection.sin();

        // find intersection with the goal
        if (zr > 0) {
          double scaling = differentialHeight / zr;
          double distance = Math.hypot(xr, yr) * scaling;
          Rotation2d angle = new Rotation2d(xr, yr, true);
        }
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