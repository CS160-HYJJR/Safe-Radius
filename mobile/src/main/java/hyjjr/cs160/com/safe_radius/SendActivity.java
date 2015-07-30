package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import java.util.ArrayList;

import hyjjr.cs160.com.safe_radius.sendActivity_events.MessageSpinnerListener;
import hyjjr.cs160.com.safe_radius.sendActivity_events.SwitchListener;


public class SendActivity extends Activity {

    private static final String TAG = SendActivity.class.getSimpleName();
    private String[] messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Global.setSendActivity(this);

        messages = getResources().getStringArray(R.array.message_choices);
        setSwitchListener();
        setMessageSpinnerContent();
        setMessageSpinnerListener();
    }

    private void setMessageSpinnerListener() {
        Spinner spinner = (Spinner) findViewById(R.id.message_spinner);
        spinner.setOnItemSelectedListener(new MessageSpinnerListener());
    }

    private void setMessageSpinnerContent() {
        Spinner spinner = (Spinner) findViewById(R.id.message_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.message_choices));
        spinner.setAdapter(adapter);
    }


    private void setSwitchListener() {
        Switch switch1 = (Switch) findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(new SwitchListener());
    }


    /*
        hide all except switch
     */
    public void hideAll() {
        setVisibilityAll(View.INVISIBLE);
        Global.getMainActivity().disableTab();
        Log.d(TAG, "hide all");
    }

    /*
        show all except switch
     */
    public void showAll() {
        setVisibilityAll(View.VISIBLE);
        Global.getMainActivity().enableTab();
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

    public void messageSpinnerAddItem(String item) {
        Spinner spinner = (Spinner) findViewById(R.id.message_spinner);
        SpinnerAdapter sa = spinner.getAdapter();
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < sa.getCount() - 1; i++) {
            list.add((String) sa.getItem(i));
        }
        list.add(item);
        list.add((String) sa.getItem(sa.getCount() - 1));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);
    }


}
