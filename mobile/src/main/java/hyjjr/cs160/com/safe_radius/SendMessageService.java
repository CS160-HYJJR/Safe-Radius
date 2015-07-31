package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class SendMessageService extends IntentService {

    private static final String TAG = SendMessageService.class.getSimpleName();
    private GoogleApiClient googleApiClient;

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

    public void googleApiClientInit() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        }
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    void sendMessage(String messagePath, byte[] message) {
        googleApiClientInit();
        if (googleApiClient == null) {
            Log.e(TAG, "Google API connection failed");
            return;
        }
        if (!googleApiClient.isConnected()) {
            ConnectionResult connectionResult = googleApiClient
                    .blockingConnect(10, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "Service failed to connect to GoogleApiClient.");
                return;
            }
        }

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(googleApiClient).await();

        boolean isConnectionGood = false;
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), messagePath,
                            message).await();
            if (result.getStatus().isSuccess()) {
                isConnectionGood = true;
                Log.d(TAG, "send message success messagePath: " + messagePath
                        + " message: " + new String(message, StandardCharsets.UTF_8)
                        + " node: " + node.getDisplayName());
            }
        }

        if (!isConnectionGood) {
            Log.e(TAG, "send message failed");
            //Intent intent = new Intent(BROADCAST);
            //LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
            //bm.sendBroadcast(intent);
        }

    }
}