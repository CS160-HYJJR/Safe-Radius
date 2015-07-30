package hyjjr.cs160.com.safe_radius;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service {
    private static final int NOTIFICATION_ID = 0;

    public NotificationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        CharSequence message = intent.getExtras().getCharSequence("EXTRA_PARAM1");
        Notification.Builder notificationBuilder =
                new Notification.Builder(this)
                        .setContentTitle(message)
                        .setPriority(Notification.PRIORITY_MAX);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .notify(NOTIFICATION_ID, notificationBuilder.build());
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
