package com.nutrons.steamworks;

import io.reactivex.Flowable;

public class Vision {

  private Flowable<byte[]> dataStream;
  private Flowable<Double[]> dataStreamDouble;
  private Flowable<Double> angle;
  private Flowable<Double> distance;
  private static final String DUMMY_VALUE = "-1000:-1000"; //Arduino sends -1000.0 over serial when it doesn't see anything, to prevent
                                                               //robot sending an exception "no serial port found"

  Vision(Flowable<byte[]> dataStream) {
    this.dataStream = dataStream;
    this.dataStreamDouble = this.dataStream
        .filter(x -> x.length == 12)
        .map(x -> new String(x, "UTF-8"))
        .map(x -> x.equals(DUMMY_VALUE) ? "0.0:0.0" : x) //vision will change any dummy values to 0.0
        .map(x -> x.split(":")).filter(x -> x.length == 2)
        .map(x -> new Double[]{Double.valueOf(x[0]),
            Double.valueOf(x[1])}); //Returns a double array[distance, angle]

    this.distance = dataStreamDouble.map(x -> x[0]);
    this.angle = dataStreamDouble.map(x -> x[1]);
  }

  public Flowable<Double> getAngle() {
    return this.angle;
  }

  public Flowable<Double> getDistance() {
    return this.distance;
  }
}
