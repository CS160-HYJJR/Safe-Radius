package hyjjr.cs160.com.safe_radius;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Wearable listener service for data layer messages
 * https://github.com/LarkspurCA/WearableMessage/blob/master/wear/src/main/java/com/androidweardocs/wearablemessage/ListenerService.java
 * Created by michaelHahn on 1/11/15.
 */
public class ReceiveMessageService extends WearableListenerService {

    private static final String MESSAGE_PATH = "/message_mobile_to_wear";
    public static final String RECEIVE_PARENT_PICTURE_PATH = "mobile_to_wear_parent_picture";
    private static final String TAG = ReceiveMessageService.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1;
    private static final String MESSAGE = "I saw your message";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on mobile is: " + message);

            // MainActivity to background
            Log.d(TAG, "start to finish MainActivity");

            Intent intent = new Intent(MainActivity.FINISH_BROADCAST);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);


            // start Vibration
            Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
            startService(vibrateIntent);

            Intent confirmIntent = new Intent(this, SendMessageService.class);
            confirmIntent.putExtra("confirmationEnabled", "true");
            confirmIntent.putExtra("message", MESSAGE.getBytes());
            confirmIntent.putExtra("message_path", SendMessageService.MESSAGE_PATH);
            confirmIntent.putExtra("notification", "true");
            PendingIntent confirmPendingIntent =
                    PendingIntent.getService(this, 0, confirmIntent, 0);
            Notification.Builder notificationBuilder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(message)
                            .setContentText("")
                            .setPriority(Notification.PRIORITY_MAX)
                            .setLargeIcon(((Global) getApplication()).getParentPicture())
                            .addAction(R.drawable.ic_done, "Got it!", confirmPendingIntent);
            Notification notification = notificationBuilder.build();
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);

        } else if (messageEvent.getPath().equals(RECEIVE_PARENT_PICTURE_PATH)) {
            byte[] bytes = messageEvent.getData();
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                Log.d(TAG, "image nulll" + " " + bytes.length);
            }
            ((Global)getApplication()).setParentPicture(bitmap);
            Log.d(TAG, "image seted");
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}