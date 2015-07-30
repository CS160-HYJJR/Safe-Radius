package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class SendMessageService extends IntentService {

    private static final String TAG = SendMessageService.class.getSimpleName();
    private static final String MESSAGE_PATH = "/message_mobile_to_wear";

    public SendMessageService() {
        super(SendMessageService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            byte[] data = (byte[]) intent.getExtras().get("data");
            if (data != null)
                sendMessage(MESSAGE_PATH, data);
        }
    }

    void sendMessage(String messagePath, byte[] message) {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(Global.getGoogleApiClient()).await();

        boolean isConnectionGood = false;
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result =
                    Wearable.MessageApi.sendMessage(Global.getGoogleApiClient(), node.getId(), messagePath,
                            message).await();
            if (result.getStatus().isSuccess()) {
                isConnectionGood = true;
                Log.d(TAG, "send message success");
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