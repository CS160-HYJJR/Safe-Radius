package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

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

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName() + "001";
    private static int UPDATE_INTERVAL_MS = 3000;
    private static int FASTEST_INTERVAL_MS = 1500;
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
        private Audio audio = new Audio();
        private boolean started;
        private static final int DELAY = 500;
        private Handler myHandler = new Handler();
        private Toast recording; // for indefinite long Toast
        private Toast voiceSent;
        private static final int MAX_TIME = 5000;


        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (recording == null) {
                recording = Toast.makeText(MainActivity.this, "Recording", Toast.LENGTH_SHORT);
                recording.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 40);
            }
            if (voiceSent == null) {
                voiceSent = Toast.makeText(getApplication(), "Voice Sent", Toast.LENGTH_SHORT);
                voiceSent.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 40);
            }
            Runnable startRecording = new Runnable() {
                public void run() {
                    started = true;
                    fireRecodingToastIndefinite();
                    recording.show();
                    audio.startRecording();
                }
            };
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (!started) {
                        voiceSent.cancel();
                        myHandler.postDelayed(startRecording, DELAY);//Message will be delivered in 1 second.
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    started = false;
                    recording.cancel();
                    voiceSent.show();
                    byte[] bytes = audio.stopRecording();
                    Intent intent = new Intent(MainActivity.this, SendMessageService.class);
                    intent.putExtra("message_path", SendMessageService.VOICE_PATH);
                    intent.putExtra("message", bytes);
                    intent.putExtra("confirmationEnabled", "true");
                    startService(intent);
                    break;
            }
            return false;
        }

        private void fireRecodingToastIndefinite() {
            Thread t = new Thread() {
                public void run() {
                    try {
                        while (started) {
                            recording.show();
                            if (started) sleep(250);
                        }
                    } catch (Exception e) {
                        Log.e("LongToast", "", e);
                    }
                }
            };
            t.start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.send_button).setOnClickListener(sendButtonListener);
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
