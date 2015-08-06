package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

/**
 * Created by main on 7/31/15.
 */
public class Global extends Application implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,{
    private boolean power;
    private String[] messages;
    private int safeRadiusSelected;
    private int messageSelected;
    private double safeRadius;
    private Bitmap parentPicture;
    private boolean lostConnection;

    @Override
    public void onCreate() {
        super.onCreate();
        power = true;
        messages = getResources().getStringArray(R.array.message_choices);
        safeRadiusSelected = 1;
        messageSelected = 0;
        parentPicture = BitmapFactory.decodeResource(getResources(), R.drawable.icon_add_new_ppl);
        safeRadius = 60; // TODO
        lostConnection = false;
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

    public Bitmap getParentPicture() {
        return parentPicture;
    }

    public void setParentPicture(Bitmap parentPicture) {
        this.parentPicture = parentPicture;
    }

    public void lostConnection() { lostConnection = true; }

    public void gotConnection() { lostConnection = false; }

    public boolean getConnection() { return lostConnection; }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
