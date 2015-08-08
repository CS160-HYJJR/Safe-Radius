package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class VibrationService extends IntentService {

    public VibrationService() {
        super(VibrationService.class.getSimpleName());
    }
    private Ringtone r;
    private Vibrator v;
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        }
    }
}
