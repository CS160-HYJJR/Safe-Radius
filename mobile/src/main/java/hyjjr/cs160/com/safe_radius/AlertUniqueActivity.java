package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

// used for disconnection alert
public class AlertUniqueActivity extends Activity {

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setFinishOnTouchOutside(false);
        CharSequence title = getIntent().getExtras().getCharSequence("title");
        CharSequence text = new String((byte[])getIntent().getExtras().get("text"));

        if (alertDialog != null)
            alertDialog.dismiss();

        final byte[] voiceBytes = (byte[])getIntent().getExtras().get("voice");
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setCanceledOnTouchOutside(false);
        if (voiceBytes != null) {
            alertDialog.setButton(0, "PLAY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertUniqueActivity.this.finish();
                            AudioTrack at = new AudioTrack(AudioManager.STREAM_NOTIFICATION,
                                    RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
                                    voiceBytes.length, AudioTrack.MODE_STATIC);
                            at.play();
                        }
                    });
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DISMISS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        AlertUniqueActivity.this.finish();
                    }
                });
        if (!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Global.KEY_FOREGROUND_BOOLEAN, false)) {
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "GO TO APP",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(AlertUniqueActivity.this, MainActivity.class));
                            AlertUniqueActivity.this.finish();

                        }
                    });

            Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
            startService(vibrateIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (alertDialog != null) {
            alertDialog.show();
            TextView textView= (TextView) alertDialog.findViewById(android.R.id.message);
            Typeface face=Typeface.createFromAsset(getAssets(),"fonts/gotham.ttf");
            textView.setTypeface(face);
            int titleId = getResources().getIdentifier("alertTitle", "id", "android");
            if (titleId > 0) {
                TextView dialogTitle = (TextView) alertDialog.findViewById(titleId);
                if (dialogTitle != null) {
                    dialogTitle.setTypeface(face, Typeface.BOLD);
                }
            }
            for (int i = -10; i < 10; i++) { // -10 ---- 10 , just a large range
                if (alertDialog.getButton(i) != null) {
                    alertDialog.getButton(i).setTypeface(face, Typeface.BOLD);
                }
            }
        }

        // start vibration and sound
        Intent vibrateIntent = new Intent(this, VibrationService.class);
        startService(vibrateIntent);
   }

    @Override
    public void onStop() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        super.onStop();
    }
}

