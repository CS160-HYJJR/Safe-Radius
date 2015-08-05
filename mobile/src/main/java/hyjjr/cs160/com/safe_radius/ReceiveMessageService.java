package hyjjr.cs160.com.safe_radius;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;

/**
 * Wearable listener service for data layer messages
 * https://github.com/LarkspurCA/WearableMessage/blob/master/wear/src/main/java/com/androidweardocs/wearablemessage/ListenerService.java
 * Created by michaelHahn on 1/11/15.
 */
public class ReceiveMessageService extends WearableListenerService {

    public static final String SEND_MESSAGE_TO_ACTIVITY_BROADCAST = "SEND_MESSAGE_TO_ACTIVITY_BROADCAST";
    private static final String MESSAGE_PATH = "/message_wear_to_mobile";
    private static final String LOCATION_PATH = "/location_wera_to_mobile";
    private static final String TAG = ReceiveMessageService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (!((Global)getApplication()).isTurnedOn()) {
            return;
        }

        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on mobile is: " + message);

            Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            alertIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            alertIntent.putExtra("title", "Message from your child");
            alertIntent.putExtra("text", message);
            startActivity(alertIntent);

            // start Vibration
            Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
            startService(vibrateIntent);

        } else if (messageEvent.getPath().equals(LOCATION_PATH)) {
            DoubleBuffer doubleBuf =
                    ByteBuffer.wrap(messageEvent.getData())
                            .order(ByteOrder.BIG_ENDIAN)
                            .asDoubleBuffer();
            double[] positions = new double[doubleBuf.remaining()];
            Log.d(TAG, "Location path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Location received lat: " + positions[0] + " lon: " + positions[1] + " alt" + positions[2]);


        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}