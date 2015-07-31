package hyjjr.cs160.com.safe_radius;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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


public class SendFragment extends Fragment {

    private static final String TAG = SendFragment.class.getSimpleName();
    private String[] messages;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        messages = getResources().getStringArray(R.array.message_choices);
        view = getView();
        if (view == null) {
            assert false;
        }
        ((Switch) view.findViewById(R.id.switch1)).setOnCheckedChangeListener(new SwitchListener());

        ((Spinner) view.findViewById(R.id.message_spinner)).setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, messages));

        ((Spinner) view.findViewById(R.id.message_spinner)).setOnItemSelectedListener(new MessageSpinnerListener());

        (view.findViewById(R.id.send_button)).setOnClickListener(new SendButtonListener());
    }

    /*
        hide all except switch
     */
    public void hideAll() {
        setVisibilityAll(View.INVISIBLE);
        //((Global) getApplication()).getMainActivity().disableTab();
    }

    /*
        show all except switch
     */
    public void showAll() {
        setVisibilityAll(View.VISIBLE);
        //((Global) getApplication()).getMainActivity().enableTab();
    }

    /*
        Set invisibility of All views except switch
     */
    private void setVisibilityAll(int visibility) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.send_fragment_layout);
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View view = viewGroup.getChildAt(i);
            if (view != null && !(view instanceof Switch)) {
                view.setVisibility(visibility);
            }
        }
    }

    public void messageSpinnerAddItem(String item) {
        Spinner spinner = (Spinner) view.findViewById(R.id.message_spinner);
        SpinnerAdapter sa = spinner.getAdapter();
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < sa.getCount() - 1; i++) {
            list.add((String) sa.getItem(i));
        }
        list.add(item);
        list.add((String) sa.getItem(sa.getCount() - 1));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);
    }

    private String getCurrentMessage() {
        Spinner spinner = (Spinner) view.findViewById(R.id.message_spinner);
        return spinner.getSelectedItem().toString();
    }


    public class SwitchListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                //((Global) getApplication()).turnOn();
                //getTabWidget().setEnabled(false);
                SendFragment.this.showAll();
            } else {
                //((Global) getApplication()).turnOff();
                SendFragment.this.hideAll();
            }
        }
    }


    public class MessageSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (position == parent.getCount() - 1) { // last message selected
                final EditText input = new EditText(getActivity());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Please write the new message")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newMessage = input.getText().toString();
                                SendFragment.this.messageSpinnerAddItem(newMessage);
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
            Intent intent = new Intent(getActivity(), SendMessageService.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", getCurrentMessage());
            getActivity().startService(intent);
        }
    }
}
