package hyjjr.cs160.com.safe_radius.sendActivity_events;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;

import hyjjr.cs160.com.safe_radius.Global;

/**
 * Created by main on 7/29/15.
 */
public class MessageSpinnerListener implements AdapterView.OnItemSelectedListener {

    private static final String TAG = MessageSpinnerListener.class.getSimpleName();

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == parent.getCount() - 1) { // last message selected
            final EditText input = new EditText(Global.getMainActivity());

            new AlertDialog.Builder(Global.getMainActivity())
                    .setTitle("Please write the new message")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newMessage = input.getText().toString();
                            Global.getSendActivity().messageSpinnerAddItem(newMessage);
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
