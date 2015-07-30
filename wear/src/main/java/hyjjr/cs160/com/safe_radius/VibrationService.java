package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class VibrationService extends IntentService {

    public VibrationService() {
        super(VibrationService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(500);
        }
    }
}
