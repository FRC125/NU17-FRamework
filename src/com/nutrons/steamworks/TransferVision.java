package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.libKudos254.vision.VisionServer;

public class TransferVision implements Subsystem {
  TransferVision(){
  }

  @Override
  public void registerSubscriptions() {
    VisionProcessor.getInstance().getHorizAngleFlow().subscribe(System.out::println);
  }
}
