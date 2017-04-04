package br.com.helderfarias.pushandroid.handlers;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

import com.facebook.react.bridge.ReactApplicationContext;

import br.com.helderfarias.pushandroid.Constants;
import br.com.helderfarias.pushandroid.helpers.EventManagerHelper;

public class RemoteTokenRefreshHandler extends BroadcastReceiver {

    private final ReactApplicationContext reactContext;
    private final EventManagerHelper eventManager;

    public RemoteTokenRefreshHandler(ReactApplicationContext reactContext, EventManagerHelper eventManager) {
        this.reactContext = reactContext;
        this.eventManager = eventManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!this.reactContext.hasActiveCatalystInstance()) {
            return;
        }

        String token = intent.getStringExtra("token");

        eventManager.send(Constants.EVENT_TOKEN_RECEIVED, token);
    }

}
