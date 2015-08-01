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
    private GoogleApiClient mGoogleApiClient;

    public SendMessageService() {
        super(SendMessageService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String messagePath = (String) intent.getExtras().get("message_path");
            String message = (String) intent.getExtras().get("message");
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
            }
            mGoogleApiClient.connect();
            if (!mGoogleApiClient.isConnected()) {
                ConnectionResult connectionResult = mGoogleApiClient
                        .blockingConnect(10, TimeUnit.SECONDS);
                if (!connectionResult.isSuccess()) {
                    Log.e(TAG, "Service failed to connect to GoogleApiClient.");
                    return;
                }
            }
            sendMessage(messagePath, message.getBytes());
            mGoogleApiClient.disconnect();
        }
    }

    void sendMessage(String messagePath, byte[] message) {

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(mGoogleApiClient).await();

        boolean isConnectionGood = false;
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), messagePath,
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
        }
    }
}