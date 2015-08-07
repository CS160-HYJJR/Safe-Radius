package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.location.LocationListener;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName() + "001";
    private static int UPDATE_INTERVAL_MS = 5000;
    private static int FASTEST_INTERVAL_MS = 2500;
    private GoogleApiClient mGoogleApiClient;
    public static final String FINISH_BROADCAST = "FINISH";
    private static final String MESSAGE = "Come find me";
    private static final int NOTIFICATION_ID = 1;

    private View.OnClickListener sendButtonListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SendMessageService.class);
            intent.putExtra("message_path", SendMessageService.MESSAGE_PATH);
            intent.putExtra("message", MESSAGE.getBytes());
            intent.putExtra("confirmationEnabled", "true");
            startService(intent);
        }
    };

    private View.OnTouchListener micButtonListener = new View.OnTouchListener() {
        private boolean started;
        private static final int DELAY = 1;
        private static final ScheduledExecutorService worker =
                Executors.newSingleThreadScheduledExecutor();
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_UP:
                    if (started) {
                        Runnable task = new Runnable() {
                            public void run() {

                            }
                        };
                        worker.schedule(task, DELAY, TimeUnit.SECONDS);
                    }
                    break;

            }
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViewById(R.id.send_button).setOnClickListener(sendButtonListener);
        findViewById((R.id.mic_button)).setOnTouchListener(micButtonListener);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, new IntentFilter(FINISH_BROADCAST));

        /*
        if (getIntent() != null) {
            if (getIntent().getExtras() != null) {
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
                Log.d(TAG, "message reply");
                Intent intent = new Intent(MainActivity.this, SendMessageService.class);
                intent.putExtra("message_path", SendMessageService.MESSAGE_PATH);
                intent.putExtra("message", MESSAGE2.getBytes());
                startService(intent);
            }
        } */
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        //bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        //mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        if (((Global)getApplication()).isTurnedOn()) {
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
                    });
        }
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "lat: " + location.getLatitude() + " lon: " + location.getLongitude() + " alt: " + location.getAltitude());
        double[] positions = new double[]{location.getLatitude(),
                location.getLongitude(), location.getAltitude()};

        ByteBuffer byteBuffer = ByteBuffer.allocate(positions.length * 8);
        DoubleBuffer intBuffer = byteBuffer.asDoubleBuffer();
        intBuffer.put(positions).order();
        byte[] positionByte = byteBuffer.array();
        positions[0]= ByteBuffer.wrap(positionByte).getDouble(0);
        positions[1]= ByteBuffer.wrap(positionByte).getDouble(8);
        positions[2]= ByteBuffer.wrap(positionByte).getDouble(16);
        Log.d(TAG, "lat: " + positions[0] + " lon: " + positions[1] + " alt: " + positions[2]);
        String bin="";
        for (int i = 0; i < 24*8; i++) {
            if (i%64==0)
                bin+="@";
            bin += String.valueOf(positionByte[i/8]>>>(i%8)&1);
        }
        Log.d(TAG, "binary of message: " + bin);
        Intent intent = new Intent(MainActivity.this, SendMessageService.class);
        intent.putExtra("message_path", SendMessageService.LOCATION_PATH);
        intent.putExtra("message", positionByte);
        startService(intent);
        Log.d(TAG, "location sent");
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "finish received");
            MainActivity.this.finish();
        }
    };


}
