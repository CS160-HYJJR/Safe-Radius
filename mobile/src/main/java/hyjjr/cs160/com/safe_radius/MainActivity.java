package hyjjr.cs160.com.safe_radius;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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

    private boolean hasAlerted; // Once alert once every time open the map.
    private RepeatAction routine_check_connection;
    private RepeatAction routine_check_history;
    private static final int CHECK_CONNECTION_INTERVAL = 5000;
    private static final int CHECK_HISTORY_INTERVAL = 1000;
    private static int UPDATE_INTERVAL_MS = 2000;
    private static int FASTEST_INTERVAL_MS = 1000;
    private static final int ZOOM_LEVEL = 19;
    private Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/gotham.ttf");
        ((TextView)findViewById(R.id.connection_status)).setTypeface(custom_font);

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.tab_name1)).setIndicator("", getResources().getDrawable(R.drawable.ic_main)),
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

        if (routine_check_connection == null) {
            routine_check_connection = new RepeatAction(new Runnable() {
                @Override
                public void run() {
                    checkConnection();
                }
            }, CHECK_CONNECTION_INTERVAL);
        }
        if (routine_check_history == null) {
            routine_check_history = new RepeatAction(new Runnable() {
                @Override
                public void run() {
                    checkMessageHistory();
                }
            }, CHECK_HISTORY_INTERVAL);
        }

    }

    public void checkMessageHistory() {
        String output = "";
        Long sentTime = ((Global)getApplication()).getSentMessageTime();
        Long receiveTime = ((Global)getApplication()).getReceiveMessageTime();
        long currentTime = System.currentTimeMillis();
        if (sentTime != null || receiveTime != null) {
            output += "message";
        }
        if (sentTime != null) {
            output += " sent ";
            long timeDiff = currentTime - sentTime;
            if (timeDiff < 1000*60) {
                output += timeDiff/1000 + "s";
            } else if (timeDiff < 1000*60*60) {
                output += timeDiff/(1000*60) + "min";
            } else if (timeDiff < 1000*60*60*24) {
                output += timeDiff/(1000*60*60) + "h";
            } else {
                output += timeDiff/(1000*60*60*24) + "days";
            }
        }
        if (receiveTime != null) {
            output += " received ";
            long timeDiff = currentTime - receiveTime;
            if (timeDiff < 1000*60) {
                output += timeDiff/1000 + "s";
            } else if (timeDiff < 1000*60*60) {
                output += timeDiff/(1000*60) + "min";
            } else if (timeDiff < 1000*60*60*24) {
                output += timeDiff/(1000*60*60) + "h";
            } else {
                output += timeDiff/(1000*60*60*24) + "days";
            }
        }
        if (sentTime != null || receiveTime != null) {
            output += " ago";
        }
        if (findViewById(R.id.message_history) != null) {
            ((Global)getApplication()).setMessageHistory(output);
            ((TextView)findViewById(R.id.message_history)).setText(output);
        }
    }

    public void checkConnection() {
        boolean isReceived = ((Global)getApplication()).isReceivedMessageFromWearInInterval();
        boolean isDisconnected = ((Global)getApplication()).isDisconnectedToWatch();
        boolean isConnected = ((Global)getApplication()).isConnectedToWatch();
        if (ReceiveMessageService.receivedSthFromWatch) {
            ((Global)getApplication()).connectToWatch();
            ((TextView)findViewById(R.id.connection_status)).setText("Connected");
        } else if (isReceived){
            ((Global) getApplication()).setReceivedMessageFromWearInInterval(false);
            ((Global)getApplication()).connectToWatch();
            ((TextView)findViewById(R.id.connection_status)).setText("Connected");
        } else if (!isDisconnected && !isReceived){
            String title = "Error";
            String text = "You lost connection and did not connect to your child's watch. Please go to their last " +
                    "known location to reestablish connection.";
            Intent alertUniqueIntent = new Intent(MainActivity.this, AlertUniqueActivity.class);
            alertUniqueIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            alertUniqueIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            alertUniqueIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alertUniqueIntent.putExtra("title", title);
            alertUniqueIntent.putExtra("text", text.getBytes());
            ((Global) getApplication()).disconenctToWatch();
            ((TextView) findViewById(R.id.connection_status)).setText("Disconnected");
            ((Global) getApplication()).setReceivedMessageFromWearInInterval(false);
            // TODO enable this alert after bug fixed
            //startActivity(alertUniqueIntent);
        } else {
            ((TextView) findViewById(R.id.connection_status)).setText("Disconnected");
        }
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
        if (!((Global)getApplication()).isTurnedOn()) {
            return;
        }
        routine_check_connection.stopUpdates();
        routine_check_history.stopUpdates();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                routine_check_connection.startUpdates();
                handler.removeCallbacks(this);
            }
        }, CHECK_CONNECTION_INTERVAL);

        routine_check_history.startUpdates();

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
            mGoogleApiClient.disconnect();
        }
        if (routine_check_connection != null){
            routine_check_connection.stopUpdates();
        }
        if (routine_check_history != null){
            routine_check_history.stopUpdates();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (((Global)getApplication()).isTurnedOn()) {
            startRequestLocation2();

        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (radar != null)
            radar.onLocationChanged(location);

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

        //Log.d(TAG, "parent lat: " + location.getLatitude() + " lon: " + location.getLongitude());
        currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (childLatLng != null) {
          //  Log.d(TAG, "child lat: " + childLatLng.latitude + " lon: " + childLatLng.longitude);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
