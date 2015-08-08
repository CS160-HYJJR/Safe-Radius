package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AlertActivity extends Activity {

    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("alert", "onCre");
        CharSequence title = getIntent().getExtras().getCharSequence("title");
        CharSequence text = getIntent().getExtras().getCharSequence("text");
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "GO TO APP",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startActivity(new Intent(AlertActivity.this, MainActivity.class));
                    }
                });

    }

    @Override
    public void onStop() {
        super.onStop();
        alertDialog.dismiss();
        Log.d("alert", "onS");
    }

    @Override
    public void onDestroy() {
        alertDialog.dismiss();
        Log.d("alert", "ondes");
        super.onDestroy();
    }


    @Override
    public void onResume() {
        Log.d("alert", "onResume");
        super.onResume();
        alertDialog.show();
    }

}
