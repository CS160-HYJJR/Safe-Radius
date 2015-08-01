package hyjjr.cs160.com.safe_radius;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Wearable listener service for data layer messages
 * https://github.com/LarkspurCA/WearableMessage/blob/master/wear/src/main/java/com/androidweardocs/wearablemessage/ListenerService.java
 * Created by michaelHahn on 1/11/15.
 */
public class ReceiveMessageService extends WearableListenerService {

    private static final String MESSAGE_PATH = "/message_mobile_to_wear";
    private static final String TAG = ReceiveMessageService.class.getSimpleName();

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on mobile is: " + message);


            Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alertIntent.putExtra("title", "Message From your mom");
            alertIntent.putExtra("text", message);
            startActivity(alertIntent);

            // start Vibration
            Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
            startService(vibrateIntent);

            Intent notificationIntent = new Intent(getApplicationContext(), NotificationService.class);
            notificationIntent.putExtra("title", "Message From your mom");
            notificationIntent.putExtra("text", message);
            startService(notificationIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}