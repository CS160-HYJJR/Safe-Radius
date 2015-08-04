package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by main on 8/3/15.
 */
public class Global extends Application{
    private Bitmap parentPicture;


    @Override
    public void onCreate() {
        super.onCreate();
        parentPicture = BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_new_ppl);
    }

    public Bitmap getParentPicture() {
        return parentPicture;
    }

    public void setParentPicture(Bitmap parentPicture) {
        this.parentPicture = parentPicture;
    }
}
