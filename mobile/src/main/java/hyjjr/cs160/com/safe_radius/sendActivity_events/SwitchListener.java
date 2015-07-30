package hyjjr.cs160.com.safe_radius.sendActivity_events;

import android.widget.CompoundButton;

import hyjjr.cs160.com.safe_radius.Config;
import hyjjr.cs160.com.safe_radius.SendActivity;

public class SwitchListener implements CompoundButton.OnCheckedChangeListener {

    private final SendActivity sendActivity;

    public SwitchListener(SendActivity sendActivity) {
        this.sendActivity = sendActivity;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Config.turnOn();
            sendActivity.showAll();
        } else {
            Config.turnOff();
            sendActivity.hideAll();
        }
    }
}
