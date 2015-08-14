package hyjjr.cs160.com.safe_radius;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayOutputStream;

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
    private SharedPreferences prefs;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
            Intent mainIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingMainIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);

            String portraitString = prefs.getString(Global.PARENT_PORTRAIT, null);
            Bitmap portrait;
            if (portraitString == null) {
                portrait = BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_new_ppl);
            } else {
                portrait = string2Bitmap(portraitString);
            }

            Notification.Builder notificationBuilder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(message)
                            .setContentText("")
                            .setPriority(Notification.PRIORITY_MAX)
                            .setLargeIcon(portrait)
                            .addAction(R.drawable.ic_done, "Got it!", confirmPendingIntent)
                            .addAction(R.drawable.ic_open_in_browser_black_48dp, "Go to app", pendingMainIntent);
            Notification notification = notificationBuilder.build();
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);

        } else if (messageEvent.getPath().equals(RECEIVE_PARENT_PICTURE_PATH)) {
            byte[] bytes = messageEvent.getData();
            Log.d(TAG, "receive picture size: " + bytes.length);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap == null) {
                Log.d(TAG, "image null" + " " + bytes.length);
            } else {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(Global.PARENT_PORTRAIT, bitmap2String(bitmap));
                editor.commit();
                Log.d(TAG, "image seted");
            }
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }

    public static String bitmap2String(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap string2Bitmap(String imageString) {
        byte[] b = Base64.decode(imageString, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }
}