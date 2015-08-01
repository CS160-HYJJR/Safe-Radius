package hyjjr.cs160.com.safe_radius;

import android.app.Application;

/**
 * Created by main on 7/31/15.
 */
public class Global extends Application {
    private boolean power;
    private String[] messages;
    private int safeRadiusSelected;
    private int messageSelected;
    private double safeRadius;

    @Override
    public void onCreate() {
        super.onCreate();
        messages = getResources().getStringArray(R.array.message_choices);
        safeRadiusSelected = 1;
        messageSelected = 0;
    }


    public boolean isTurnedOn() {
        return power;
    }

    public void turnOn() {
        power = true;
    }

    public void turnOff() {
        power = false;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public int getSafeRadiusSelected() {
        return safeRadiusSelected;
    }

    public void setSafeRadiusSelected(int safeRadiusSelected) {
        this.safeRadiusSelected = safeRadiusSelected;
    }

    public int getMessageSelected() {
        return messageSelected;
    }

    public void setMessageSelected(int messageSelected) {
        this.messageSelected = messageSelected;
    }

    public double getSafeRadius() {
        return safeRadius;
    }

    public void setSafeRadius(double safeRadius) {
        this.safeRadius = safeRadius;
    }

    public double getSafeRadiusInMeter() {
        return safeRadius * 0.3048;
    }

    public String getMessage() {
        return messages[getMessageSelected()];
    }
}
