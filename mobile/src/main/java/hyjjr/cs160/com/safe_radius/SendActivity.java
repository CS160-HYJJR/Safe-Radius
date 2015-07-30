package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import hyjjr.cs160.com.safe_radius.sendActivity_events.SwitchListener;


public class SendActivity extends Activity {

    private static final String TAG = SendActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        setSwitchListener();
        setRadiusSpinnerContent();
        setMessageSpinnerContent();
    }

    private void setRadiusSpinnerContent() {
        Spinner spinner = (Spinner) findViewById(R.id.radius_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.radius_choices));
        spinner.setAdapter(adapter);
    }

    private void setMessageSpinnerContent() {
        Spinner spinner = (Spinner) findViewById(R.id.message_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.message_choices));
        spinner.setAdapter(adapter);
    }


    private void setSwitchListener() {
        Switch switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new SwitchListener(this));
    }


    /*
        hide all except switch
     */
    public void hideAll() {
        setVisibilityAll(View.INVISIBLE);
        Log.d(TAG, "hide all");
    }

    /*
        show all except switch
     */
    public void showAll() {
        setVisibilityAll(View.VISIBLE);
        Log.d(TAG, "show all");
    }

    /*
        Set invisibility of All views except switch
     */
    private void setVisibilityAll(int visibility) {
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.send_activity_layout);
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View view = viewGroup.getChildAt(i);
            if (view != null && !(view instanceof Switch)) {
                view.setVisibility(visibility);
            }
        }
    }


}
