package hyjjr.cs160.com.safe_radius;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 0;

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        CharSequence message = intent.getExtras().getCharSequence("EXTRA_PARAM1");
        Log.d(TAG, "notification " + message);
        Notification.Builder notificationBuilder =
                new Notification.Builder(((Global) getApplication()).getMainActivity())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(message)
                        .setContentText(message)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setAutoCancel(true)
                        .setShowWhen(true)
                        .setOngoing(true)
                        .setWhen(System.currentTimeMillis())
                        .setCategory(Notification.CATEGORY_ALARM);
        Notification notification = notificationBuilder.build();
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
