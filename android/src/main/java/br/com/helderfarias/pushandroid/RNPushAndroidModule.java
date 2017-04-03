package br.com.helderfarias.pushandroid;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.messaging.RemoteMessage.Notification;

import android.os.Bundle;
import android.util.Log;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class RNPushAndroidModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    private final static String TAG = "RNPushAndroidModule";

    private final NotificationHelper notificationHelper;
    private final ReactApplicationContext reactContext;
    private boolean inForeground;

    public RNPushAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.notificationHelper = new NotificationHelper(this.reactContext);
        this.reactContext.addLifecycleEventListener(this);
        this.reactContext.addActivityEventListener(this);
        registerTokenRefreshHandler();
        registerMessageHandler();
        registerLocalMessageHandler();
    }

    @Override
    public String getName() {
        return "RNPushAndroid";
    }

    @ReactMethod
    public void getInitialNotification(Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            promise.resolve(null);
            return;
        }
        promise.resolve(parseIntent(getCurrentActivity().getIntent()));
    }

    @ReactMethod
    public void getToken(Promise promise) {
        Log.d(TAG, "Firebase token: " + FirebaseInstanceId.getInstance().getToken());

        promise.resolve(FirebaseInstanceId.getInstance().getToken());
    }

    @ReactMethod
    public void sendLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);

        notificationHelper.sendNotification(bundle, this.inForeground);
    }

    @ReactMethod
    public void scheduleLocalNotification(ReadableMap details) {
        Bundle bundle = Arguments.toBundle(details);
        notificationHelper.sendNotificationScheduled(bundle);
    }

    @ReactMethod
    public void cancelLocalNotification(String notificationID) {
        notificationHelper.cancelNotification(notificationID);
    }

    @ReactMethod
    public void cancelAllLocalNotifications() {
        notificationHelper.cancelAllNotifications();
    }

    @ReactMethod
    public void removeDeliveredNotification(String notificationID) {
        notificationHelper.removeDeliveredNotification(notificationID);
    }

    @ReactMethod
    public void removeAllDeliveredNotifications() {
        notificationHelper.removeAllDeliveredNotifications();
    }

    @ReactMethod
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic);
    }

    @ReactMethod
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
    }

    @ReactMethod
    public void getScheduledLocalNotifications(Promise promise) {
        ArrayList<Bundle> bundles = notificationHelper.getScheduledNotifications();
        WritableArray array = Arguments.createArray();
        for (Bundle bundle : bundles) {
            array.pushMap(Arguments.fromBundle(bundle));
        }
        promise.resolve(array);
    }

    @Override
    public void onHostResume() {
        this.inForeground = true;
    }

    @Override
    public void onHostPause() {
        this.inForeground = false;
    }

    @Override
    public void onHostDestroy() {
        // nothing
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // nothing
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // nothing
    }

    public void onNewIntent(Intent intent) {
        sendEvent(Constants.EVENT_NOTIFICATION_RECEIVED, parseIntent(intent));
    }

    private void sendEvent(String eventName, Object params) {
        getReactApplicationContext()
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    private void registerTokenRefreshHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_REMOTE_REFRESH_TOKEN);

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                String token = intent.getStringExtra("token");
                sendEvent(Constants.EVENT_TOKEN_RECEIVED, token);
            }
            }
        }, intentFilter);
    }

    private void registerMessageHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_REMOTE_NOTIFICATION);

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
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
                sendEvent(Constants.EVENT_NOTIFICATION_RECEIVED, params);

            }
            }
        }, intentFilter);
    }

    private void registerLocalMessageHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_LOCAL_NOTIFICATION);

        getReactApplicationContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            if (getReactApplicationContext().hasActiveCatalystInstance()) {
                sendEvent(Constants.EVENT_LOCAL_NOTIFICATION_RECEIVED, Arguments.fromBundle(intent.getExtras()));
            }
            }
        }, intentFilter);
    }

    private WritableMap parseIntent(Intent intent) {
        WritableMap params = ConverterHelper.fromIntentToWritableMap(intent);

        WritableMap fcm = Arguments.createMap();
        fcm.putString("action", intent.getAction());

        params.putMap("fcm", fcm);
        params.putInt("opened_from_tray", 1);
        return params;
    }

}