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
            Log.d(TAG, "receive message success messagePath: " + messageEvent.getPath()
                    + " message: " + message);

            Intent notificationIntent = new Intent(this, NotificationService.class);
            notificationIntent.putExtra("EXTRA_PARAM1", message);
            startService(notificationIntent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}