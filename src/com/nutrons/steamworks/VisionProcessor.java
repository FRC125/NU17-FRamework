package com.nutrons.steamworks;

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
  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update_ = null;
  public static final double CAMERA_HEIGHT = 10.0; //in inches

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    System.out.println("VisionProcessor gotInstance");
    return instance_;
  };

  public double getYawHorizAngle() {
    if (update_ == null) {
      System.out.println("vision: error: got null update");
      return 0.0;
    } else if (update_.getTargets() == null) {
      System.out.println("vision: error: got null targets");
      return 0.0;
    } else if (update_.getTargets().isEmpty()) {
      System.out.println("vision: error: got empty targets");
      return 0.0;
    } else {
      TargetInfo target = update_.getTargets().get(0);
      System.out.println("vision: got target with Y: " + target.getY());
      return target.getY();
    }
  }

  public double getPitchVertAngle() {
    if (!(update_.getTargets() == null || update_.getTargets().isEmpty())) {
      for (TargetInfo target : update_.getTargets()) {
        return target.getZ();
      }
    }
    return 0.0;
  }

  double getDistance(double angleToTargetY, double targetHeight){
    return (targetHeight - CAMERA_HEIGHT)/Math.tan(Math.toRadians((angleToTargetY)));
  }

  public Flowable<Double> getHorizAngleFlow(){
    //System.out.println(this.getYawHorizAngle());
    return toFlow(() -> this.getYawHorizAngle());
  }

  @Override
  public synchronized void gotUpdate(VisionUpdate update) {
    update_ = update;
    //System.out.println("update is valid: " + update.isValid());
  }

}