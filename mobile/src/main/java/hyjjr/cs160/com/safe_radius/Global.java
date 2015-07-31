package hyjjr.cs160.com.safe_radius;

import android.app.Application;

/**
 * Created by main on 7/31/15.
 */
public class Global extends Application {
    private boolean power;

    public boolean isTurnedOn() {
        return power;
    }

    public void turnOn() {
        power = true;
    }

    public void turnOff() {
        power = false;
    }
}
