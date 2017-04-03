package br.com.helderfarias.pushandroid;

import java.util.Map;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "Remote message received");

        Intent i = new Intent(Constants.INTENT_RECEIVE_REMOTE_NOTIFICATION);
        i.putExtra("data", remoteMessage);
        buildLocalNotification(remoteMessage);
        sendOrderedBroadcast(i, null);
    }

    public void buildLocalNotification(RemoteMessage remoteMessage) {
        if (remoteMessage.getData() == null){
            return;
        }

        Map<String, String> data = remoteMessage.getData();

        if (data.get("custom_notification") != null){
            try {
                Bundle bundle = ConverterHelper.fromJsonToBundle(new JSONObject(data.get("custom_notification")));
                NotificationHelper helper = new NotificationHelper(this.getApplication());
                helper.sendNotification(bundle);
            } catch (JSONException e) {
                Log.e(TAG, "build local notification", e);
            }
        }
    }
}