package hyjjr.cs160.com.safe_radius;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;

import java.util.ArrayList;


public class SendActivity extends Activity {

    private static final String TAG = SendActivity.class.getSimpleName();
    private String[] messages;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        messages = getResources().getStringArray(R.array.message_choices);

        ((Switch) findViewById(R.id.switch1)).setOnCheckedChangeListener(new SwitchListener());

        ((Spinner) findViewById(R.id.message_spinner)).setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.message_choices)));

        ((Spinner) findViewById(R.id.message_spinner)).setOnItemSelectedListener(new MessageSpinnerListener());

        (findViewById(R.id.send_button)).setOnClickListener(new SendButtonListener());
    }

    /*
        hide all except switch
     */
    public void hideAll() {
        setVisibilityAll(View.INVISIBLE);
        ((Global) getApplication()).getMainActivity().disableTab();
    }

    /*
        show all except switch
     */
    public void showAll() {
        setVisibilityAll(View.VISIBLE);
        ((Global) getApplication()).getMainActivity().enableTab();
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

    private String getCurrentMessage() {
        Spinner spinner = (Spinner) findViewById(R.id.message_spinner);
        return spinner.getSelectedItem().toString();
    }


    public class SwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                ((Global) getApplication()).turnOn();
                SendActivity.this.showAll();
            } else {
                ((Global) getApplication()).turnOff();
                SendActivity.this.hideAll();
            }
        }
    }


    public class MessageSpinnerListener implements AdapterView.OnItemSelectedListener {
        private final String TAG = MessageSpinnerListener.class.getSimpleName();

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == parent.getCount() - 1) { // last message selected
                final EditText input = new EditText(SendActivity.this);

                new AlertDialog.Builder(SendActivity.this)
                        .setTitle("Please write the new message")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newMessage = input.getText().toString();
                                SendActivity.this.messageSpinnerAddItem(newMessage);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do nothing.
                            }
                        }).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }


    public class SendButtonListener implements View.OnClickListener {
        private static final String MESSAGE_PATH = "/message_mobile_to_wear";
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SendActivity.this, SendMessageService.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", getCurrentMessage());
            startService(intent);
        }
    }
}
