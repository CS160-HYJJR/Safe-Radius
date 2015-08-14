package hyjjr.cs160.com.safe_radius;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class SendMessageService extends IntentService {

    public static final String LOCATION_PATH = "/location_wear_to_mobile";
    public static final String MESSAGE_PATH = "/message_wear_to_mobile";
    public static final String VOICE_PATH = "/voice_wear_to_mobile";

    private static final String TAG = SendMessageService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private boolean confirmationEnabled = false;
    private boolean notification = false;

    public SendMessageService() {
        super(SendMessageService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {

            if (intent.getStringExtra("confirmationEnabled") != null) {
                confirmationEnabled = true;
            }

            if (intent.getStringExtra("notification") != null) {
                notification = true;
            }

            String messagePath = (String) intent.getExtras().get("message_path");
            byte[] message = (byte[]) intent.getExtras().get("message");
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
            sendMessage(messagePath, message);
            mGoogleApiClient.disconnect();
        }
    }

    void sendMessage(String messagePath, byte[] message) {

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.
                getConnectedNodes(mGoogleApiClient).await();

        if (nodes.getNodes().isEmpty()) {
            if (confirmationEnabled) {
                Intent intent = new Intent(this, ConfirmationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                        ConfirmationActivity.FAILURE_ANIMATION);
                startActivity(intent);
            }
        } else {
            for (Node node : nodes.getNodes()) {
                MessageApi.SendMessageResult result =
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), messagePath,
                                message).await();
                if (result.getStatus().isSuccess()) {
                    Log.d(TAG, "send message success messagePath: " + messagePath
                            + " message: " + new String(message)
                            + " node: " + node.getDisplayName());

                    if (notification) {
                        Intent intent2 = new Intent(this, MainActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent2);
                    }

                    if (confirmationEnabled) {
                        Intent intent2 = new Intent(this, ConfirmationActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent2.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                ConfirmationActivity.SUCCESS_ANIMATION);
                        startActivity(intent2);
                    }

                } else {
                    if (confirmationEnabled) {
                        Intent intent = new Intent(this, ConfirmationActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE,
                                ConfirmationActivity.FAILURE_ANIMATION);
                        startActivity(intent);
                    }
                }
            }
        }
    }
}