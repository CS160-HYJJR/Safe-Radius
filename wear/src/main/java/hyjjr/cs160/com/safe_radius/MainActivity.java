package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName() + "001";
    private static int UPDATE_INTERVAL_MS = 1000;
    private static int FASTEST_INTERVAL_MS = 250;
    private GoogleApiClient mGoogleApiClient;
    private View.OnClickListener sendButtonListener = new View.OnClickListener() {
        private static final String MESSAGE = "Come find me";

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SendMessageService.class);
            intent.putExtra("message_path", SendMessageService.MESSAGE_PATH);
            intent.putExtra("message", MESSAGE.getBytes());
            startService(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send_button).setOnClickListener(sendButtonListener);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Build a request for continual location updates
        /*
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Send request for location updates
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Log.d(TAG, "request success");

        // Build a request for continual location updates
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        // Send request for location updates
        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient,
                        locationRequest, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.getStatus().isSuccess()) {
                            Log.d(TAG, "Location Successfully requested");
                        } else {
                            Log.e(TAG, status.getStatusMessage());
                        }
                    }
                });*/
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "lat: " + location.getLatitude() + " lon: " + location.getLongitude());
        double[] positions = new double[]{location.getLatitude(),
                location.getLongitude(), location.getAltitude()};

        ByteBuffer byteBuffer = ByteBuffer.allocate(positions.length * 8);
        DoubleBuffer intBuffer = byteBuffer.asDoubleBuffer();
        intBuffer.put(positions);

        byte[] positionByte = byteBuffer.array();
        Intent intent = new Intent(MainActivity.this, SendMessageService.class);
        intent.putExtra("message_path", SendMessageService.LOCATION_PATH);
        intent.putExtra("message", positionByte);
        startService(intent);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void NoConnectionAlert() {
        String title = "Safe Radius";
        String text = "Error: Phone and watch are not connected.";
        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("text", text);
        startActivity(alertIntent);
    }

    public void lossConnectionAlert() {
        String title = "Safe Radius";
        String text = "Warning: Your child is out of the range! Please go to their last " +
                "known location by following the radar to try and restablish connection";
        Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("text", text);
        startActivity(alertIntent);

        // start Vibration
        Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
        startService(vibrateIntent);
        /*
        Intent notificationIntent = new Intent(getApplicationContext(), NotificationService.class);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("text", text);
        startService(notificationIntent);*/
    }


}
