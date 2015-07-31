package hyjjr.cs160.com.safe_radius;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by main on 7/29/15.
 */
public class Global extends Application {

    private static final String TAG = Global.class.getSimpleName();

    private MainActivity mainActivity;
    private GoogleApiClient googleApiClient;
    private boolean isOn;

    public void turnOn() {
        isOn = true;
    }

    public void turnOff() {
        isOn = false;
    }

    public boolean checkPowerStatus() {
        return isOn;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public GoogleApiClient getGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                    .addApi(Wearable.API)
                    .build();
        }
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        return googleApiClient;
    }

}
