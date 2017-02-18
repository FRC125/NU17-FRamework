package com.nutrons.steamworks;

import io.reactivex.Flowable;

public class Vision {

  private static final String DUMMY_VALUE = "NONE:-1000:-1000"; //Arduino sends -1000.0 over serial when it doesn't see anything, to prevent
  //robot sending an exception "no serial port found"
  private static Vision instance;
  private Flowable<byte[]> dataStream;
  private Flowable<String[]> dataStreamString;
  private Flowable<Double> angle;
  private Flowable<Double> distance;
  private Flowable<String> state;

  private Vision(Flowable<byte[]> dataStream) {
    this.dataStream = dataStream;
    this.dataStreamString = this.dataStream
        .filter(x -> x.length == 17)
        .map(x -> new String(x, "UTF-8"))
        .map(
            x -> x == DUMMY_VALUE ? "NONE:0.0:0.0" : x) //vision will change any dummy values to 0.0
        .map(x -> x.split(":")).filter(x -> x.length == 3);
    //Returns a string array[state, distance, angle]
    //states are NONE, GEAR, or BOIL

    this.state = dataStreamString.map(x -> x[0]);
    this.distance = dataStreamString.map(x -> Double.valueOf(x[1]));
    this.angle = dataStreamString.map(x -> Double.valueOf(x[2]));
  }

  public static Vision getInstance(Flowable<byte[]> dataStream) {
    if (instance == null) {
      instance = new Vision(dataStream);
    }
    return instance;
  }

  public static Vision getInstance() {
    return instance;
  }

  public Flowable<Double> getAngle() {
    return this.angle;
  }

  public Flowable<Double> getDistance() {
    return this.distance;
  }

  public Flowable<String> getState() {
    return this.state;
  }
}
