package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;

public class SendMessageService extends IntentService {

    private static final String TAG = SendMessageService.class.getSimpleName();


    public SendMessageService() {
        super(SendMessageService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String messagePath = (String) intent.getExtras().get("message_path");
            String message = (String) intent.getExtras().get("message");
            sendMessage(messagePath, message.getBytes());
        }
    }

    void sendMessage(String messagePath, byte[] message) {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(((Global) getApplication()).getGoogleApiClient()).await();

        boolean isConnectionGood = false;
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(((Global) getApplication()).getGoogleApiClient(), node.getId(), messagePath,
                            message).await();
            if (result.getStatus().isSuccess()) {
                isConnectionGood = true;
                Log.d(TAG, "send message success messagePath: " + messagePath
                        + " message: " + new String(message, StandardCharsets.UTF_8)
                        + " node: " + node.getDisplayName());
            }
        }

        if (!isConnectionGood) {
            Log.d(TAG, "send message failed");
            //Intent intent = new Intent(BROADCAST);
            //LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            //bm.sendBroadcast(intent);
        }

    }
}