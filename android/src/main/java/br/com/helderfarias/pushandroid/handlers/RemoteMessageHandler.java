package br.com.helderfarias.pushandroid.handlers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;

import java.util.Map;
import java.util.Set;

import br.com.helderfarias.pushandroid.Constants;
import br.com.helderfarias.pushandroid.helpers.EventManagerHelper;

public class RemoteMessageHandler extends BroadcastReceiver {

    private final ReactApplicationContext reactContext;
    private final EventManagerHelper eventManager;

    public RemoteMessageHandler(ReactApplicationContext reactContext, EventManagerHelper eventManager) {
        this.reactContext = reactContext;
        this.eventManager = eventManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!this.reactContext.hasActiveCatalystInstance()) {
            return;
        }

        RemoteMessage message = intent.getParcelableExtra("data");
        WritableMap params = Arguments.createMap();
        WritableMap fcmData = Arguments.createMap();

        if (message.getNotification() != null) {
            Notification notification = message.getNotification();
            fcmData.putString("title", notification.getTitle());
            fcmData.putString("body", notification.getBody());
            fcmData.putString("color", notification.getColor());
            fcmData.putString("icon", notification.getIcon());
            fcmData.putString("tag", notification.getTag());
            fcmData.putString("action", notification.getClickAction());
        }

        params.putMap("fcm", fcmData);

        if (message.getData() != null) {
            Map<String, String> data = message.getData();
            Set<String> keysIterator = data.keySet();
            for (String key : keysIterator) {
                params.putString(key, data.get(key));
            }
        }

        this.eventManager.send(Constants.EVENT_NOTIFICATION_RECEIVED, params);
    }

}