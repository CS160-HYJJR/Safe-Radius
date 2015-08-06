package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by main on 8/3/15.
 */
public class Global extends Application{
    private Bitmap parentPicture;
    private boolean power;

    // TODO receive message for turn on/off
    @Override
    public void onCreate() {
        super.onCreate();
        power = true;
        parentPicture = BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_new_ppl);
    }

    public Bitmap getParentPicture() {
        return parentPicture;
    }

    public void setParentPicture(Bitmap parentPicture) {
        this.parentPicture = parentPicture;
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
