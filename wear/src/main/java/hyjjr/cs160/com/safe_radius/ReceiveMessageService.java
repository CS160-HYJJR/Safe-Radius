package hyjjr.cs160.com.safe_radius;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
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
    public static final String RECEIVE_PARENT_PICTURE_PATH = "mobile_to_wear_parent_picture";
    private static final String TAG = ReceiveMessageService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            final String message = new String(messageEvent.getData());
            Log.d(TAG, "Message path received on mobile is: " + messageEvent.getPath());
            Log.d(TAG, "Message received on mobile is: " + message);
            /*

            Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            alertIntent.putExtra("title", "Message from your mom");
            alertIntent.putExtra("text", message);
            startActivity(alertIntent);
            */

            // MainActivity to background
            Log.d(TAG, "start to finish MainActivity");

            Intent intent = new Intent(MainActivity.FINISH_BROADCAST);
            LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            bm.sendBroadcast(intent);


            // start Vibration
            Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
            startService(vibrateIntent);

            Intent viewIntent = new Intent(this, MainActivity.class);
            viewIntent.putExtra("Message", "");
            PendingIntent viewPendingIntent =
                    PendingIntent.getActivity(this, 0, viewIntent, 0);
            Notification.Builder notificationBuilder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(message)
                            .setContentText("")
                            .setPriority(Notification.PRIORITY_MAX)
                            .setLargeIcon(((Global)getApplication()).getParentPicture())
                            .addAction(R.mipmap.ic_launcher, "Reply", viewPendingIntent);
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