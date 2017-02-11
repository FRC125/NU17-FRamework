package com.nutrons.steamworks;

import com.nutrons.framework.Subsystem;
import com.nutrons.framework.subsystems.WpiSmartDashboard;
import io.reactivex.functions.Consumer;

public class Logging implements Subsystem {
    private WpiSmartDashboard sd;
    private Consumer<String> state;
    private Consumer<Double> angle;
    private Consumer<Double> distance;

    Logging(){
        this.sd = new WpiSmartDashboard();
        this.angle = sd.getTextField("angle");
        this.distance = sd.getTextField("distance");
        //this.state = sd.getTextField("state"); TODO: get daniel and lucia to add more smartdashboard methods
    }

    @Override
    public void registerSubscriptions() {
        Vision.getInstance().getAngle().subscribe(this.angle);
        Vision.getInstance().getDistance().subscribe(this.distance);
    }
}
