package br.com.helderfarias.pushandroid;

import android.app.Activity;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import br.com.helderfarias.pushandroid.handlers.LocalMessageHandler;
import br.com.helderfarias.pushandroid.handlers.RemoteMessageHandler;
import br.com.helderfarias.pushandroid.handlers.RemoteTokenRefreshHandler;
import br.com.helderfarias.pushandroid.helpers.ConverterHelper;
import br.com.helderfarias.pushandroid.helpers.EventManagerHelper;
import br.com.helderfarias.pushandroid.helpers.NotificationHelper;

public class RNPushAndroidModule extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener {

    private final static String TAG = "RNPushAndroidModule";

    private final NotificationHelper notificationHelper;
    private final ReactApplicationContext reactContext;
    private final EventManagerHelper eventManager;
    private final RemoteMessageHandler remoteMessageHandler;
    private final LocalMessageHandler localMessageHandler;
    private final RemoteTokenRefreshHandler remoteTokenRefreshHandler;
    private boolean inForeground;

    public RNPushAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.notificationHelper = new NotificationHelper(this.reactContext);
        this.eventManager = new EventManagerHelper(this.reactContext);
        this.remoteMessageHandler = new RemoteMessageHandler(this.reactContext, this.eventManager);
        this.localMessageHandler = new LocalMessageHandler(this.reactContext, this.eventManager);
        this.remoteTokenRefreshHandler = new RemoteTokenRefreshHandler(this.reactContext, this.eventManager);
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

        notificationHelper.sendNotification(bundle);
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
        this.eventManager.send(Constants.EVENT_NOTIFICATION_RECEIVED, parseIntent(intent));
    }

    private void registerTokenRefreshHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_REMOTE_REFRESH_TOKEN);

        this.reactContext.registerReceiver(this.remoteTokenRefreshHandler, intentFilter);
    }

    private void registerMessageHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_REMOTE_NOTIFICATION);

        this.reactContext.registerReceiver(this.remoteMessageHandler, intentFilter);
    }

    private void registerLocalMessageHandler() {
        IntentFilter intentFilter = new IntentFilter(Constants.INTENT_RECEIVE_LOCAL_NOTIFICATION);

        this.reactContext.registerReceiver(this.localMessageHandler, intentFilter);
    }

    private WritableMap parseIntent(Intent intent) {
        WritableMap params = ConverterHelper.fromIntentToWritableMap(intent);

        WritableMap fcm = Arguments.createMap();
        fcm.putString("action", intent.getAction());

        params.putMap("fcm", fcm);
        params.putBoolean("opened_from_tray", true);
        return params;
    }

}