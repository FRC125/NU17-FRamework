package com.nutrons.steamworks;

import com.nutrons.libKudos254.Rotation2d;
import com.nutrons.libKudos254.vision.TargetInfo;
import com.nutrons.libKudos254.vision.VisionUpdate;
import com.nutrons.libKudos254.vision.VisionUpdateReceiver;
import io.reactivex.Flowable;

import static com.nutrons.framework.util.FlowOperators.toFlow;

/**
 * Kudos 254! =(^_^)=
 *
 * This function adds vision updates (from the Nexus smartphone) to a list in
 * RobotState. This helps keep track of goals detected by the vision system. The
 * code to determine the best goal to shoot at and prune old Goal tracks is in
 * GoalTracker.java
 *
 * @see
 */
public class VisionProcessor implements VisionUpdateReceiver {
  // Pose of the camera frame w.r.t. the turret frame
  public static double kCameraZOffset = 19.75;
  public static double kCameraPitchAngleDegrees = 35.75; // calibrated 4/22
  public static double kCameraYawAngleDegrees = -1.0;

  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update_ = null;
  public static final double CENTER_OF_TARGET_HEIGHT = 89.0;
  double differential_height_ = CENTER_OF_TARGET_HEIGHT - kCameraZOffset;
  Rotation2d camera_pitch_correction_ = Rotation2d.fromDegrees(kCameraPitchAngleDegrees);;
  Rotation2d camera_yaw_correction_ = Rotation2d.fromDegrees(kCameraYawAngleDegrees);

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    System.out.println("VisionProcessor gotInstance");
    return instance_;
  };

  public double getYawHorizAngle() {

    if (!(update_.getTargets() == null || update_.getTargets().isEmpty())) {
      for (TargetInfo target : update_.getTargets()) {
        double yaw_angle_radians = Math.atan2(target.getY() , target.getX());
        return yaw_angle_radians;
      }
    }
    return 0.0;
  }

  public double getPitchVertAngle() {
    if (!(update_.getTargets() == null || update_.getTargets().isEmpty())) {
      for (TargetInfo target : update_.getTargets()) {
        return target.getZ();
      }
    }
    return 0.0;
  }

  double getDistance(){
    if (!(update_.getTargets() == null || update_.getTargets().isEmpty())) {
      for (TargetInfo target : update_.getTargets()) {
        double xyaw = target.getX() * camera_yaw_correction_.cos() + camera_yaw_correction_.sin();
        double yyaw = camera_yaw_correction_.cos() - target.getX() * camera_yaw_correction_.sin();
        double zyaw = target.getZ();

        // Compensate for camera pitch
        double xr = zyaw * camera_pitch_correction_.sin() + xyaw * camera_pitch_correction_.cos();
        double yr = yyaw;
        double zr = zyaw * camera_pitch_correction_.cos() - xyaw * camera_pitch_correction_.sin();

        // find intersection with the goal
        if (zr > 0) {
          double scaling = differential_height_ / zr;
          double distance = Math.hypot(xr, yr) * scaling;
          Rotation2d angle = new Rotation2d(xr, yr, true);
        }
      }
    }
    return 0.0;
  }

  public Flowable<Double> getHorizAngleFlow(){
    return toFlow(() -> this.getYawHorizAngle());
  }

  @Override
  public synchronized void gotUpdate(VisionUpdate update) {
    update_ = update;
  }

}