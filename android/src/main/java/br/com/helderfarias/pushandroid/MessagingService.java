package br.com.helderfarias.pushandroid;

import java.util.Map;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.helderfarias.pushandroid.helpers.ConverterHelper;
import br.com.helderfarias.pushandroid.helpers.NotificationHelper;

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

        try {
            JSONObject params = new JSONObject();

            if (remoteMessage.getNotification() != null) {
                RemoteMessage.Notification notification = remoteMessage.getNotification();
                params.put("title", notification.getTitle());
                params.put("body", notification.getBody());
                params.put("color", notification.getColor());
                params.put("icon", notification.getIcon());
                params.put("tag", notification.getTag());
                params.put("action", notification.getClickAction());
            }

            if (remoteMessage.getData() != null) {
                Map<String, String> data = remoteMessage.getData();
                Set<String> keysIterator = data.keySet();
                for (String key : keysIterator) {
                    params.put(key, data.get(key));
                }
            }

            Log.d(TAG, "params by remote message => " + params);

            Bundle bundle = ConverterHelper.fromJsonToBundle(params);
            NotificationHelper helper = new NotificationHelper(this.getApplication());
            helper.sendNotification(bundle);
        } catch (JSONException e) {
            Log.e(TAG, "build local notification", e);
        }
    }
}