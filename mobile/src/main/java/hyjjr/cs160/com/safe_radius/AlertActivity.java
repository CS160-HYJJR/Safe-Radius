package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class AlertActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_alert);
    }

    @Override
    protected void onStart() {
        super.onStart();
        CharSequence title = getIntent().getExtras().getCharSequence("title");
        CharSequence text = getIntent().getExtras().getCharSequence("text");
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                        AlertActivity.this.finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Go to App",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        dialog.dismiss();
                        AlertActivity.this.finish();
                        startActivity(new Intent(AlertActivity.this, MainActivity.class));
                    }
                });
        alertDialog.show();
    }
}
