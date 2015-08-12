package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.util.Arrays.copyOfRange;

public class GcmSendMessage extends IntentService {

    private static final String API_KEY = "AIzaSyDKG3SmtawUFzy4ZezCqLqIL0CwVLHNvKs";
    private static final String TAG = GcmSendMessage.class.getSimpleName();
    public static final String SEND_PARENT_PICTURE = "mobile_to_wear_parent_picture";
    public GcmSendMessage() {
        super("GcmSendMessage");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!((Global)getApplication()).isTurnedOn()) {
            return;
        }
        Log.d(TAG, "gcm started");
        if (intent != null) {
            try {
                // Prepare JSON containing the GCM message content. What to send and where to send.


                String messagePath = (String) intent.getExtras().get("message_path");
                String from = (String) intent.getExtras().get("source");
                int messageStartPos = 0;
                byte[] messageTotal = (byte[]) intent.getExtras().get("message");
                final int MAX_SIZE = 1750;
                int id = (int)Math.random()*10000;
                int no = 0;

                while (messageStartPos < messageTotal.length) {
                    byte[] message;
                    if (messageStartPos + MAX_SIZE >= messageTotal.length) {
                        message = copyOfRange(messageTotal, messageStartPos, messageTotal.length);
                    } else {
                        message = copyOfRange(messageTotal, messageStartPos, messageStartPos + MAX_SIZE);
                    }
                    messageStartPos += MAX_SIZE;
                    JSONObject jGcmData = new JSONObject();
                    JSONObject jData = new JSONObject();
                    jData.put("message", new String(message, "ISO-8859-1"));
                    jData.put("message_path", messagePath);
                    jData.put("source", from);
                    jData.put("id", String.valueOf(id));
                    jData.put("message_start_pos", messageStartPos-MAX_SIZE);
                    jData.put("total_bytes", messageTotal.length);
                    // Where to send GCM message.
                    jGcmData.put("to", "/topics/" + ((Global) getApplication()).TOPIC);
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
                    System.out.println("gcm successs id: " + id + " No. " + (no++) + " size: " + message.length);
                }
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
