package br.com.helderfarias.pushandroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SystemBootEventReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("SystemBootEventReceiver", "Received reboot event");
    
        NotificationHelper helper = new NotificationHelper(context);

        for (Bundle bundle: helper.getScheduledNotifications()){
            helper.sendNotificationScheduled(bundle);
        }
    }

}