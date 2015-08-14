package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by main on 8/3/15.
 */
public class Global extends Application{
    private boolean power;
    public static final String PARENT_PORTRAIT = "0";

    // TODO receive message for turn on/off
    @Override
    public void onCreate() {
        super.onCreate();
        power = true;
    }

    public boolean isTurnedOn() {
        return power;
    }

    public void turnOn(){
        power = true;
    }

    public void turnOff() {
        power = false;
    }

}
