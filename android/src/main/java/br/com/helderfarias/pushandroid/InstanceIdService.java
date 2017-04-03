package br.com.helderfarias.pushandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        Intent i = new Intent(Constants.INTENT_RECEIVE_REMOTE_REFRESH_TOKEN);
        Bundle bundle = new Bundle();
        bundle.putString("token", refreshedToken);
        i.putExtras(bundle);
        sendBroadcast(i);
    }

}
