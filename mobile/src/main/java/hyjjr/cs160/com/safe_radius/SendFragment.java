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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;


public class SendFragment extends Fragment {

    private static final String TAG = SendFragment.class.getSimpleName();
    GoogleApiClient mGoogleApiClient;
    private View view;
    private NodeApi.NodeListener connectionListener = new NodeApi.NodeListener() {

        @Override
        public void onPeerConnected(Node node) {

        }

        @Override
        public void onPeerDisconnected(Node node) {
            lossConnectionAlert();
        }
    };
    private CompoundButton.OnCheckedChangeListener switchListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                turnOn();
            } else {
                turnOff();
            }
        }
    };
    private AdapterView.OnItemSelectedListener messageSpinnerListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
            if (position != parent.getCount() - 1)
                ((Global) getActivity().getApplication()).setMessageSelected(position);
            else { // last message selected
                final EditText input = new EditText(getActivity());

                new AlertDialog.Builder(getActivity())
                        .setTitle("Please write the new message")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String newMessage = input.getText().toString();
                                SendFragment.this.messageSpinnerAddItem(newMessage);
                                ((Global) getActivity().getApplication()).setMessageSelected(position);
                                ((Spinner) SendFragment.this.view.findViewById(R.id.message_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getMessageSelected());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((Spinner) SendFragment.this.view.findViewById(R.id.message_spinner)).setSelection(
                                        ((Global) getActivity().getApplication()).getMessageSelected());
                            }
                        }).show();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };



    private View.OnClickListener sendButtonListener = new View.OnClickListener() {
        private static final String MESSAGE_PATH = "/message_mobile_to_wear";

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), SendMessageService.class);
            intent.putExtra("message_path", MESSAGE_PATH);
            intent.putExtra("message", ((Global) getActivity().getApplication()).getMessage().getBytes());
            getActivity().startService(intent);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_send, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        view = getView();
        if (view == null) {
            assert false;
        }
        //((Switch) view.findViewById(R.id.switch1)).setOnCheckedChangeListener(switchListener);



        ((Spinner) view.findViewById(R.id.message_spinner)).setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, ((Global) getActivity().getApplication()).getMessages())); //ArrayAdapter<String>?
        ((Spinner) view.findViewById(R.id.message_spinner)).setOnItemSelectedListener(messageSpinnerListener);
        ((Spinner) view.findViewById(R.id.message_spinner)).setSelection(((Global) getActivity().getApplication()).getMessageSelected());

        (view.findViewById(R.id.send_button)).setOnClickListener(sendButtonListener);

        if (((Global) getActivity().getApplication()).isTurnedOn())
            turnOn();
        else
            turnOff();
    }

    private void turnOff() {
        ((Global) getActivity().getApplication()).turnOff();
        //((Switch) view.findViewById(R.id.switch1)).setChecked(false);
        setVisibilityAll(View.INVISIBLE);
        getActivity().findViewById(android.R.id.tabs).setVisibility(view.GONE);
        getActivity().findViewById(android.R.id.tabs).setEnabled(false);
        if (mGoogleApiClient != null) {
            Wearable.NodeApi.removeListener(mGoogleApiClient, connectionListener);
            mGoogleApiClient.disconnect();
        }
    }

    private void turnOn() {
        ((Global) getActivity().getApplication()).turnOn();
        //((Switch) view.findViewById(R.id.switch1)).setChecked(true);
        setVisibilityAll(View.VISIBLE);
        getActivity().findViewById(android.R.id.tabs).setVisibility(view.VISIBLE);
        getActivity().findViewById(android.R.id.tabs).setEnabled(true);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
        Wearable.NodeApi.addListener(mGoogleApiClient, connectionListener);

        // check connection
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(
                new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                        if (getConnectedNodesResult.getNodes().isEmpty()) { // connection failed
                            turnOff();
                            noConnectionAlert();
                        }
                    }
                }
        );
    }


    /*
        Set invisibility of All views except switch
     */
    private void setVisibilityAll(int visibility) {
        ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.send_fragment_layout);
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            final View view = viewGroup.getChildAt(i);
            if (view == null)
                continue;
            else if (view.getId() == R.id.switch1)
                continue;
            else if (view.getId() == R.id.title1)
                continue;
            else
                view.setVisibility(visibility);
        }
    }

    public void messageSpinnerAddItem(String item) {
        Spinner spinner = (Spinner) view.findViewById(R.id.message_spinner);
        SpinnerAdapter sa = spinner.getAdapter();
        ArrayList<String> list = new ArrayList<>(); //ArrayAdapter<String>?
        for (int i = 0; i < sa.getCount() - 1; i++) {
            list.add((String) sa.getItem(i));
        }
        list.add(item);
        list.add((String) sa.getItem(sa.getCount() - 1));
        ((Global) getActivity().getApplication()).setMessages(list.toArray(new String[1]));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), //ArrayAdapter<String>?
                android.R.layout.simple_spinner_item, list);
        spinner.setAdapter(adapter);
    }

    public void noConnectionAlert() {
        String title = "Warning";
        String text = "Your Phone are not connected to the watch.";
        Intent alertIntent = new Intent(getActivity(), AlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("text", text);
        startActivity(alertIntent);
    }

    public void lossConnectionAlert() {
        String title = "Warning";
        String text = "Lose signal to your child's watch. Please go to their last " +
                "known location to restablish connection.";
        Intent alertIntent = new Intent(getActivity(), AlertActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertIntent.putExtra("title", title);
        alertIntent.putExtra("text", text);
        startActivity(alertIntent);

        // start Vibration
        Intent vibrateIntent = new Intent(getActivity(), VibrationService.class);
        getActivity().startService(vibrateIntent);
    }
}
