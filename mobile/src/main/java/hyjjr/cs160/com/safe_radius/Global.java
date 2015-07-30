package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by main on 7/29/15.
 */
public class Global extends Application {

    private static final String TAG = Global.class.getSimpleName();

    private static MainActivity mainActivity;
    private static SendActivity sendActivity;
    private static RadarActivity radarActivity;
    private static GoogleApiClient googleApiClient;
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

    public static RadarActivity getRadarActivity() {
        return radarActivity;
    }

    public static void setRadarActivity(RadarActivity radarActivity) {
        Global.radarActivity = radarActivity;
    }

    public static SendActivity getSendActivity() {
        return sendActivity;
    }

    public static void setSendActivity(SendActivity sendActivity) {
        Global.sendActivity = sendActivity;
    }

    public static MainActivity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(MainActivity mainActivity) {
        Global.mainActivity = mainActivity;
    }

    public static GoogleApiClient getGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getMainActivity())
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    })
                    .addApi(Wearable.API)
                    .build();
        }
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        return googleApiClient;
    }

}
