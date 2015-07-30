package hyjjr.cs160.com.safe_radius.sendActivity_events;

import android.widget.CompoundButton;

import hyjjr.cs160.com.safe_radius.Global;

public class SwitchListener implements CompoundButton.OnCheckedChangeListener {


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Global.turnOn();
            Global.getSendActivity().showAll();
        } else {
            Global.turnOff();
            Global.getSendActivity().hideAll();
        }
    }
}
