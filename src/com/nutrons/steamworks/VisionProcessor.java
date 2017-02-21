package com.nutrons.steamworks;

import com.nutrons.libKudos254.Rotation2d;
import com.nutrons.libKudos254.vision.TargetInfo;
import com.nutrons.libKudos254.vision.VisionUpdate;
import com.nutrons.libKudos254.vision.VisionUpdateReceiver;
import edu.wpi.first.wpilibj.Timer;

import java.util.List;

/**
 * Kudos 254 for letting us use your code and app, and thanks so much to Tombot for helping us so much! =(^_^)=
 * <p>
 * This function adds vision updates (from the Nexus smartphone) to a list in
 * RobotState. This helps keep track of goals detected by the vision system. The
 * code to determine the best goal to shoot at and prune old Goal tracks is in
 * GoalTracker.java
 *
 * @see
 */
public class VisionProcessor implements VisionUpdateReceiver {
  public static final double kCameraDeadband = 0.0;
  public static final double kBoilerRadius = 7.5;
  public static final double kBoilerTargetTopHeight = 86.0;
  public static final double kCameraZOffset = 0.0; // TODO: Make camera inches from floor
  public static final double kCameraYawAngleDegrees = 0.0; // TODO: Yaw error from turret to camera, probably 0ish
  public static final double kCameraPitchAngleDegrees = 20.; // TODO: Pitch angle from turret to camera (how pointed up is it?)
  public static final double kTargetTimeoutSec = .25;
  private static final double differential_height_ = kBoilerTargetTopHeight - kCameraZOffset;

  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update_ = null;
  private Rotation2d camera_pitch_correction_ = Rotation2d.fromDegrees(-kCameraPitchAngleDegrees);
  private Rotation2d camera_yaw_correction_ = Rotation2d.fromDegrees(-kCameraYawAngleDegrees);
  private Rotation2d angle_cached_ = null;
  private double distance_cached_ = 0;
  private double last_saw_target_sec_ = 0;

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    System.out.println("VisionProcessor gotInstance");
    return instance_;
  }

  public void addVisionUpdate(double timestamp, List<TargetInfo> vision_update) {
    if (!(vision_update == null || vision_update.isEmpty())) {
      for (TargetInfo target : vision_update) {
        double ydeadband = (target.getY() > -kCameraDeadband
            && target.getY() < kCameraDeadband) ? 0.0 : target.getY();

        // Compensate for camera yaw
        double xyaw = target.getX() * camera_yaw_correction_.cos() + ydeadband * camera_yaw_correction_.sin();
        double yyaw = ydeadband * camera_yaw_correction_.cos() - target.getX() * camera_yaw_correction_.sin();
        double zyaw = target.getZ();

        // Compensate for camera pitch
        double xr = zyaw * camera_pitch_correction_.sin() + xyaw * camera_pitch_correction_.cos();
        double yr = yyaw;
        double zr = zyaw * camera_pitch_correction_.cos() - xyaw * camera_pitch_correction_.sin();

        // find intersection with the goal
        if (zr > 0) {
          double scaling = differential_height_ / zr;
          double distance = Math.hypot(xr, yr) * scaling + kBoilerRadius;
          Rotation2d angle = new Rotation2d(xr, yr, true);
          synchronized (this) {
            angle_cached_ = angle;
            distance_cached_ = distance;
            last_saw_target_sec_ = Timer.getFPGATimestamp();
          }
        }
      }
    }
  }

  public synchronized double getDegreesToTarget() {
    if (angle_cached_ != null) {
      return angle_cached_.getDegrees();
    }
    return 0;
  }

  public synchronized double getDistanceToBoiler() {
    return distance_cached_;
  }

  public synchronized boolean seesTarget() {
    return Math.abs(last_saw_target_sec_ - Timer.getFPGATimestamp()) < kTargetTimeoutSec;
  }

  @Override
  public synchronized void gotUpdate(VisionUpdate update) {
    update_ = update;
  }
}