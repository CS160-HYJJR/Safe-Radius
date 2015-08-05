package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GcmSendMessage extends IntentService {

    private static final String API_KEY = "AIzaSyDKG3SmtawUFzy4ZezCqLqIL0CwVLHNvKs";

    public GcmSendMessage() {
        super("GcmSendMessage");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                // Prepare JSON containing the GCM message content. What to send and where to send.
                JSONObject jGcmData = new JSONObject();
                JSONObject jData = new JSONObject();
                String messagePath = (String) intent.getExtras().get("message_path");
                byte[] message = (byte[]) intent.getExtras().get("message");
                jData.put("message", message);
                // Where to send GCM message.
                jGcmData.put("to", "/topics/global");
                // What to send in GCM message.
                jGcmData.put("data", jData);

                // Create connection to send GCM Message request.
                URL url = new URL("https://android.googleapis.com/gcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Authorization", "key=" + API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                // Send GCM message content.
                OutputStream outputStream = conn.getOutputStream();
                outputStream.write(jGcmData.toString().getBytes());

                // Read GCM response.
                InputStream inputStream = conn.getInputStream();
                String resp = IOUtils.toString(inputStream);
                System.out.println(resp);
                System.out.println("Check your device/emulator for notification or logcat for " +
                        "confirmation of the receipt of the GCM message.");
            } catch (IOException e) {
                System.out.println("Unable to send GCM message.");
                System.out.println("Please ensure that API_KEY has been replaced by the server " +
                        "API key, and that the device's registration token is correct (if specified).");
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
