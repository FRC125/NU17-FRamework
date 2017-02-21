package com.nutrons.steamworks;

import com.nutrons.libKudos254.vision.VisionUpdate;
import com.nutrons.libKudos254.vision.VisionUpdateReceiver;
import edu.wpi.first.wpilibj.RobotState;

/**
 * This function adds vision updates (from the Nexus smartphone) to a list in
 * RobotState. This helps keep track of goals detected by the vision system. The
 * code to determine the best goal to shoot at and prune old Goal tracks is in
 * GoalTracker.java
 *
 * @see GoalTracker.java
 */
public class VisionProcessor implements VisionUpdateReceiver {
  static VisionProcessor instance_ = new VisionProcessor();
  VisionUpdate update_ = null;
  //RobotState robot_state_ = RobotState.getInstance();

  VisionProcessor() {
  }

  public static VisionProcessor getInstance() {
    return instance_;
  }

  public void onLoop() {
    VisionUpdate update;
    synchronized (this) {
      if (update_ == null) {
        return;
      }
      update = update_;
      update_ = null;
    }
  }

  public double getX(){
    return this.update_.getTargets().get(0).getX();
  }


  @Override
  public synchronized void gotUpdate(VisionUpdate update) {
    update_ = update;
  }

}