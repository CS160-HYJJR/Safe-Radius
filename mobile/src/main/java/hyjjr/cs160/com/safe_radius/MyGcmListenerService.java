/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hyjjr.cs160.com.safe_radius;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.maps.model.LatLng;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.TreeMap;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String MESSAGE_PATH = "/message_wear_to_mobile";
    private static final String LOCATION_PATH = "/location_wear_to_mobile";
    private static final String VOICE_PATH = "/voice_wear_to_mobile";
    public class ByteArray {
        private byte[] bytes;
        private int receivedBytes;
        public ByteArray(byte[] bytes, int receivedBytes) {
            this.bytes = bytes;
            this.receivedBytes = receivedBytes;
        }
    }

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (!((Global)getApplication()).isTurnedOn()) {
            return;
        }
        if (!from.equals("/topics/"+((Global)getApplication()).TOPIC))
            return;

        String source = data.getString("source");
        String messagePath = data.getString("message_path");
        String message = data.getString("message");
        String id = data.getString("id");
        byte[] messageBytes = null;
        try {
            messageBytes = message.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int messageStartPos = Integer.valueOf(data.getString("message_start_pos"));
        int totalBytes = Integer.valueOf(data.getString("total_bytes"));
        if (messageBytes.length < totalBytes) {
            TreeMap<String, ByteArray> pendingResults = ((Global)getApplication()).getPendingResults();
            if (!pendingResults.containsKey(id)) {
                byte[] bytes = new byte[totalBytes];
                for (int i = messageStartPos; i < messageStartPos + messageBytes.length; i++) {
                    bytes[i] = messageBytes[i-messageStartPos];
                }
                pendingResults.put(id, new ByteArray(bytes, messageBytes.length));

                return; // RETURN
            } else {
                ByteArray byteArray = pendingResults.get(id);
                for (int i = messageStartPos; i < messageStartPos + messageBytes.length; i++) {
                    byteArray.bytes[i] = messageBytes[i-messageStartPos];
                }
                byteArray.receivedBytes += messageBytes.length;
                if (byteArray.receivedBytes < totalBytes)
                    return;
                else {
                    messageBytes = byteArray.bytes;
                    pendingResults.remove(id);
                }
            }
        }

        Log.d(TAG, "gcm message size: " + messageBytes.length);
        if (source.equals("phone")) {
            Log.d(TAG, "Gcm received message from phone. Message_path " + data.getString("message_path")+ " message " + data.getString("message"));
            Intent intent = new Intent(this, SendMessageService.class);
            intent.putExtra("message_path", messagePath);
            intent.putExtra("message", messageBytes);
            startService(intent);
        }
        else if (ReceiveMessageService.receivedSthFromWatch == false){
            if (messagePath.equals(MESSAGE_PATH)) {
                Log.d(TAG, "Message path received on mobile is: " + messagePath);
                Log.d(TAG, "Message received on mobile is: " + messageBytes);

                Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //            alertIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                alertIntent.putExtra("title", "Message from your child");
                alertIntent.putExtra("text", messageBytes);
                startActivity(alertIntent);

                // start Vibration
                Intent vibrateIntent = new Intent(getApplicationContext(), VibrationService.class);
                startService(vibrateIntent);

            } else if (messagePath.equals(LOCATION_PATH)) {
                Log.d(TAG, "location reveiced ");
                double[] positions = new double[3];
                positions[0]= ByteBuffer.wrap(messageBytes).getDouble(0);
                positions[1]= ByteBuffer.wrap(messageBytes).getDouble(8);
                positions[2]= ByteBuffer.wrap(messageBytes).getDouble(16);
                //double[] positions = new double[doubleBuf.remaining()];
                Log.d(TAG, "Location path received on mobile is: " + messagePath);
                Log.d(TAG, "Location received lat: " + positions[0] + " lon: " + positions[1] + " alt " + positions[2]);
                ((Global)getApplication()).setChildLatLng(new LatLng(positions[0], positions[1]));
                ((Global)getApplication()).setChildAltitude(positions[2]);
            } else if (messagePath.equals(VOICE_PATH)) {
                Log.d(TAG, "voice received");
                Intent alertIntent = new Intent(getApplicationContext(), AlertActivity.class);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alertIntent.putExtra("voice", messageBytes);
                alertIntent.putExtra("title", "Voice from your child");
                alertIntent.putExtra("text", messageBytes);
                startActivity(alertIntent);
            }

        }
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        //sendNotification(message);
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    /* Unused */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_send)
                .setContentTitle("GCM Message")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
