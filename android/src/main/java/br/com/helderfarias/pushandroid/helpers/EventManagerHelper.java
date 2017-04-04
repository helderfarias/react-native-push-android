package br.com.helderfarias.pushandroid.helpers;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import android.support.annotation.Nullable;

public class EventManagerHelper {

    private ReactContext reactContext;

    public EventManagerHelper(ReactContext reactContext) {
        this.reactContext = reactContext;
    }

    public final void send(String event, @Nullable WritableMap params) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }

    public final void send(String event) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, null);
    }

    public final void send(String event, String params) {
        this.reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(event, params);
    }

}
