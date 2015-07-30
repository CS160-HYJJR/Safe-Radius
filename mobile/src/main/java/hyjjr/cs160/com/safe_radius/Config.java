package hyjjr.cs160.com.safe_radius;

import android.util.Log;

public abstract class Config {
    private static final String TAG = Config.class.getSimpleName();
    private static boolean isOn;

    public static void turnOn() {
        isOn = true;
        Log.d(TAG, "Turned on");
    }

    public static void turnOff() {
        isOn = false;
        Log.d(TAG, "Turned off");
    }

    public static boolean checkPowerStatus() {
        return isOn;
    }

}
