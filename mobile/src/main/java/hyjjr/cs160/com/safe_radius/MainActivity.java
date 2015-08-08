package hyjjr.cs160.com.safe_radius;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static RadarFragment radar;
    private FragmentTabHost mTabHost;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private GoogleApiClient mGoogleApiClient;

    private double lonChildVelocity = 0.000015; // in unit of degree per second
    private double latChildVelocity = 0.000015; // in unit of degree per second
    private boolean hasAlerted; // Once alert once every time open the map.
    private RepeatAction routine;
    private static final int ROUTINE_INTERVAL = 6000;
    private static int UPDATE_INTERVAL_MS = 5000;
    private static int FASTEST_INTERVAL_MS = 2500;
    private static final int ZOOM_LEVEL = 19;
    // Actually, we should detect the movement once the app is opened, not only in the map fragment only.
    // change later.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab_name1)).setIndicator("", getResources().getDrawable(R.drawable.ic_send)),
                SendFragment.class, null);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab2_name)).setIndicator("", getResources().getDrawable(R.drawable.ic_map)),
                RadarFragment.class, null);


        for (int i = 0; i < mTabHost.getTabWidget().getTabCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).getLayoutParams().height = 200;
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_drawable);
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
                    Log.d(TAG, "gcm ok");
                } else {
                    Log.d(TAG, "gcm failed");
                }
            }
        };

        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);


        routine = new RepeatAction(new Runnable() {
            @Override
            public void run() {

                boolean isReceived = ((Global)getApplication()).isReceivedMessageFromWearInInterval();
                boolean isDisconnected = ((Global)getApplication()).isDisconnectedToWatch();
                boolean isConnected = ((Global)getApplication()).isConnectedToWatch();
                if (isReceived && !isDisconnected && !isConnected) {
                    ((Global) getApplication()).setReceivedMessageFromWearInInterval(false);
                }
                else if (!isReceived && !isDisconnected) {
                    String title = "Error";
                    String text = "You lost connection and did not connect to your child's watch. Please go to their last " +
                            "known location to reestablish connection.";
                    Intent alertIntent = new Intent(MainActivity.this, AlertActivity.class);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    alertIntent.putExtra("title", title);
                    alertIntent.putExtra("text", text.getBytes());
                    MainActivity.this.startActivity(alertIntent);
                    ((Global)getApplication()).disconenctToWatch();
                    ((TextView)findViewById(R.id.connection_status)).setText("Disconnected");
                    ((Global) getApplication()).setReceivedMessageFromWearInInterval(false);
                } else if (isConnected){
                    ((Global) getApplication()).setReceivedMessageFromWearInInterval(false);
                    ((Global)getApplication()).connectToWatch();
                    ((TextView)findViewById(R.id.connection_status)).setText("Connected");
                }
            }
        }, ROUTINE_INTERVAL);

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }


    @Override
    public void onStart()
    {
        super.onStart();
        ((Global)getApplication()).setForeground(true);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();


    }

    @Override
    public void onStop() {
        ((Global)getApplication()).setForeground(false);
        super.onStop();
    }


    @Override
    public void onPause() {
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        //mGoogleApiClient.disconnect();
        super.onPause();
    }


    public void startRequestLocation() {
        // Build a request for continual location updates
        if (mGoogleApiClient == null)
            return;
        else if (mGoogleApiClient.isConnected()) {
            startRequestLocation2();
        }
        else
            mGoogleApiClient.connect();
    }

    private void startRequestLocation2() {
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
                });
    }
    public void stopRequestLocation() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        if (routine != null){
            routine.stopUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (((Global)getApplication()).isTurnedOn()) {
            startRequestLocation2();
            routine.startUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (radar != null)
            radar.onLocationChanged(location);

        Log.d(TAG, "on location changed");
        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        double currentAltitude = location.getAltitude();
        LatLng childLatLng = ((Global)getApplication()).getChildLatLng();
        Double childAltitude = ((Global)getApplication()).getChildAltitude();
        // Alert if child is too far
        boolean isDialogOpened;
        float[] distance = new float[1];
        if (currentLatLng != null && childLatLng != null && childAltitude != null) {
            Location.distanceBetween(childLatLng.latitude, childLatLng.longitude,
                    currentLatLng.latitude, currentLatLng.longitude, distance);
            Double dist = (double)distance[0]; // meter
            Double altitudeDiff = childAltitude - currentAltitude;
            if (distance[0] > ((Global) getApplication()).getSafeRadiusInMeter()) {
                if (!hasAlerted) { // avoid duplicate alert
                    hasAlerted = true;
                    String title = "Warning";
                    String text = "Your child went out of the safe radius.";
                    Intent alertIntent = new Intent(this, AlertActivity.class);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    alertIntent.putExtra("title", title);
                    alertIntent.putExtra("text", text.getBytes());
                    startActivity(alertIntent);
                }
            } else {
                hasAlerted = false;
            }
        }

        Log.d(TAG, "parent lat: " + location.getLatitude() + " lon: " + location.getLongitude());
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (childLatLng != null) {
            Log.d(TAG, "child lat: " + childLatLng.latitude + " lon: " + childLatLng.longitude);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
