package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.util.Log;

public class NotificationService extends IntentService {
    private static final String TAG = NotificationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;

    public NotificationService() {
        super(NotificationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "phone notification");
            CharSequence title = intent.getExtras().getCharSequence("title");
            CharSequence text = intent.getExtras().getCharSequence("text");
            Notification.Builder notificationBuilder =
                    new Notification.Builder(getApplicationContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(text)
                            .setPriority(Notification.PRIORITY_MAX);
            Notification notification = notificationBuilder.build();
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
            startForeground(NOTIFICATION_ID, notification);
        }
    }
}
