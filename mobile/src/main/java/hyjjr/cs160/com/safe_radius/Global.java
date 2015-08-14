package hyjjr.cs160.com.safe_radius;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.TreeMap;

/**
 * Created by main on 7/31/15.
 */
public class Global extends Application {

    public boolean connectedDirectlyToWatch;
    public static final String KEY_MESSAGE_STRING = "0";
    public static final String KEY_MESSAGE_POSITION_INT = "1";
    public static final String KEY_MESSAGE_COUNT = "2";
    public static final String KEY_MESSAGES_ = "messages";
    public static final String KEY_RADIUS_STRING = "3";
    public static final String KEY_RADIUS_POSITION_INT = "4";
    public static final String KEY_RADIUS_COUNT = "5";
    public static final String KEY_RADIUSES_ = "radiuses";
    public static final String KEY_BACKGROUND_STRING = "6";
    public static final String KEY_ADD_PARENT_STRING = "7";
    public static final String KEY_POWER_BOOLEAN = "8";
    public static final String KEY_FOREGROUND_BOOLEAN = "9";



    public static final String TOPIC = "final_submission";

    public LatLng childLatLng;
    public Double childAltitude;
    public TreeMap<String, MyGcmListenerService.ByteArray> pendingResults; // <id, message>
    public boolean receivedMessageFromWearInInterval;
    private int connectionToWatchStatus;
    private Long sentMessageTime;
    private Long receiveMessageTime;
    private String messageHistory = "";

    public SharedPreferences prefs;

    public void disconenctToWatch() {
        connectionToWatchStatus = -1;
    }

    public void connectToWatch() {
        connectionToWatchStatus = 1;
    }

    public boolean isDisconnectedToWatch() {
        return connectionToWatchStatus == -1;
    }

    public boolean isConnectedToWatch() {
        return connectionToWatchStatus == 1;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        pendingResults = new TreeMap<>();
        receivedMessageFromWearInInterval = false;
        connectionToWatchStatus = 0; // undefined connection
    }

    public double getSafeRadiusInMeter() {
        return Double.valueOf(prefs.getString(KEY_RADIUS_STRING, "100").
                replaceAll("[^\\d.]", "")) * 0.3048;
    }

    public LatLng getChildLatLng() {
        return childLatLng;
    }

    public void setChildLatLng(LatLng childLatLng) {
        this.childLatLng = childLatLng;
    }

    public Double getChildAltitude() {
        return childAltitude;
    }

    public void setChildAltitude(double childAltitude) {
        this.childAltitude = childAltitude;
    }

    public TreeMap<String, MyGcmListenerService.ByteArray> getPendingResults() {
        return pendingResults;
    }

    public boolean isReceivedMessageFromWearInInterval() {
        return receivedMessageFromWearInInterval;
    }

    public void setReceivedMessageFromWearInInterval(boolean receivedMessageFromWearInInterval) {
        this.receivedMessageFromWearInInterval = receivedMessageFromWearInInterval;
    }

    public Long getSentMessageTime() {
        return sentMessageTime;
    }

    public void setSentMessageTime(Long sentMessageTime) {
        this.sentMessageTime = sentMessageTime;
    }

    public Long getReceiveMessageTime() {
        return receiveMessageTime;
    }

    public void setReceiveMessageTime(Long receiveMessageTime) {
        this.receiveMessageTime = receiveMessageTime;
    }

    public String getMessageHistory() {
        return messageHistory;
    }

    public void setMessageHistory(String messageHistory) {
        this.messageHistory = messageHistory;
    }

}
