package hyjjr.cs160.com.safe_radius;

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Typeface;
        import android.media.AudioFormat;
        import android.media.AudioManager;
        import android.media.AudioTrack;
        import android.os.Bundle;
        import android.view.MotionEvent;
        import android.view.WindowManager;
        import android.widget.TextView;

public class AlertActivity extends Activity {

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

        final byte[] voiceBytes = (byte[])getIntent().getExtras().get("voice");
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setCanceledOnTouchOutside(false);
        if (voiceBytes != null) {
            alertDialog.setButton(0, "PLAY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertActivity.this.finish();
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
                        AlertActivity.this.finish();
                    }
                });
        if (!((Global)getApplication()).isForeground()) {
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "GO TO APP",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startActivity(new Intent(AlertActivity.this, MainActivity.class));
                            AlertActivity.this.finish();
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
        }
    }

    @Override
    public void onStop() {
        if (alertDialog != null)
            alertDialog.dismiss();
        super.onStop();
    }
}

