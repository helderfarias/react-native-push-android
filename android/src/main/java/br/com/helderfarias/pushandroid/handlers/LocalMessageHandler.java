package br.com.helderfarias.pushandroid.handlers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;

import br.com.helderfarias.pushandroid.Constants;
import br.com.helderfarias.pushandroid.helpers.EventManagerHelper;

public class LocalMessageHandler extends BroadcastReceiver {

    private final ReactApplicationContext reactContext;
    private final EventManagerHelper eventManager;

    public LocalMessageHandler(ReactApplicationContext reactContext, EventManagerHelper eventManager) {
        this.reactContext = reactContext;
        this.eventManager = eventManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!this.reactContext.hasActiveCatalystInstance()) {
            return;
        }

        eventManager.send(Constants.EVENT_LOCAL_NOTIFICATION_RECEIVED, Arguments.fromBundle(intent.getExtras()));
    }

}