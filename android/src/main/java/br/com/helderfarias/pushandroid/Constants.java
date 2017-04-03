package br.com.helderfarias.pushandroid;

/**
 * Created by helder on 01/04/17.
 */

public class Constants {

    public static final String INTENT_RECEIVE_REMOTE_REFRESH_TOKEN = "br.com.helderfarias.pushandroid.FCMRefreshToken";

    public static final String INTENT_RECEIVE_REMOTE_NOTIFICATION = "br.com.helderfarias.pushandroid.FCMReceiveNotification";

    public static final String INTENT_RECEIVE_LOCAL_NOTIFICATION = "br.com.helderfarias.pushandroid.ReceiveLocalNotification";

    public static final String EVENT_NOTIFICATION_RECEIVED = "FCMNotificationReceived";

    public static final String EVENT_LOCAL_NOTIFICATION_RECEIVED = "FCMLocalNotificationReceived";

    public static final String EVENT_TOKEN_RECEIVED = "FCMTokenRefreshed";

}
