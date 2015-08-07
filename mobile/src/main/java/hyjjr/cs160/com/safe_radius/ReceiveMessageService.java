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

    public static final String SEND_MESSAGE_TO_ACTIVITY_BROADCAST = "SEND_MESSAGE_TO_ACTIVITY_BROADCAST";
    private static final String MESSAGE_PATH = "/message_wear_to_mobile";
    private static final String LOCATION_PATH = "/location_wear_to_mobile";
    private static final String TAG = ReceiveMessageService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;
    public static boolean receivedSthFromWatch;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (!((Global)getApplication()).isTurnedOn()) {
            return;
        }
        Log.d(TAG, "message received: " + new String(messageEvent.getData()));
        receivedSthFromWatch = true;
        Intent intent = new Intent(this, GcmSendMessage.class);
        intent.putExtra("message_path", messageEvent.getPath());
        intent.putExtra("message", messageEvent.getData());
        intent.putExtra("source", "watch");
        startService(intent);
        super.onMessageReceived(messageEvent);

    }
}