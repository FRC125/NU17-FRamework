package com.nutrons.steamworks;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import io.reactivex.Flowable;

public class Vision {
    private Flowable<byte[]> dataStream;
    private Flowable<Double[]> dataStreamDouble;
    private Flowable<Double> angle;
    private Flowable<Double> distance;

    Vision(Flowable<byte[]> dataStream) {
        this.dataStream = dataStream;
        this.dataStreamDouble = this.dataStream
                .filter(x -> x.length == 10)
                .map(x -> new String(x, "UTF-8"))
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
