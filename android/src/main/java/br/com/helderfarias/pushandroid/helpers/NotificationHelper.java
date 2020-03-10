package br.com.helderfarias.pushandroid.helpers;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;

import br.com.helderfarias.pushandroid.Constants;
import br.com.helderfarias.pushandroid.LocalMessagingReceiver;

public final class NotificationHelper {

  private static final String TAG = "NotificationCenterH";

  private static final long DEFAULT_VIBRATION = 300L;

  private final static String PREFERENCES_KEY = "br.com.helderfarias.pushandroid.REACT_NATIVE_NOTIFICATION_HELPER";

  private final Context context;

  private final SharedPreferences sharedConfig;

  public NotificationHelper(Context context) {
    this.context = context;
    this.sharedConfig = this.context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
  }

  public void sendNotification(Bundle bundle) {
    try {
      Class intentClass = getMainActivityClass();
      if (intentClass == null) {
        return;
      }

      if (bundle.getString("body") == null) {
        return;
      }

      NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
          .setContentTitle(getTitle(bundle)).setContentText(bundle.getString("body"))
          .setTicker(bundle.getString("ticker")).setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
          .setAutoCancel(bundle.getBoolean("auto_cancel", true)).setNumber(bundle.getInt("number"))
          .setSubText(bundle.getString("sub_text")).setGroup(bundle.getString("group"))
          .setVibrate(new long[] { 0, DEFAULT_VIBRATION })
          .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
          .setExtras(bundle.getBundle("data"));

      configurePriority(bundle, notification);

      configureSmallIcon(bundle, notification);

      configureLargeIcon(bundle, notification);

      configureBigText(bundle, notification);

      configureSound(bundle, notification);

      configureColor(bundle, notification);

      configureVibrate(bundle, notification);

      configureLights(bundle, notification);

      notifyLocalMessageWhenBackground(bundle, intentClass, notification);

      clearScheduleNotificationOnceFired(bundle);
    } catch (Exception e) {
      Log.e(TAG, "failed to send local notification", e);
    }
  }

  public void sendNotificationScheduled(Bundle bundle) {
    Class intentClass = getMainActivityClass();
    if (intentClass == null) {
      return;
    }

    String notificationId = bundle.getString("id");
    if (notificationId == null) {
      Log.e(TAG, "failed to schedule notification because id is missing");
      return;
    }

    Long fireDate = Math.round(bundle.getDouble("fire_date"));
    if (fireDate == 0) {
      Log.e(TAG, "failed to schedule notification because fire date is missing");
      return;
    }

    Intent notificationIntent = new Intent(context, LocalMessagingReceiver.class);
    notificationIntent.putExtras(bundle);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId.hashCode(), notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    Long interval = null;
    switch (bundle.getString("repeat_interval", "")) {
      case "minute":
        interval = (long) 60000;
        break;
      case "hour":
        interval = AlarmManager.INTERVAL_HOUR;
        break;
      case "day":
        interval = AlarmManager.INTERVAL_DAY;
        break;
      case "week":
        interval = AlarmManager.INTERVAL_DAY * 7;
        break;
    }

    if (interval != null) {
      getAlarmManager().setRepeating(AlarmManager.RTC_WAKEUP, fireDate, interval, pendingIntent);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      getAlarmManager().setExact(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
    } else {
      getAlarmManager().set(AlarmManager.RTC_WAKEUP, fireDate, pendingIntent);
    }

    SharedPreferences.Editor editor = sharedConfig.edit();
    try {
      JSONObject json = ConverterHelper.fromBundleToJson(bundle);
      editor.putString(notificationId, json.toString());
      editor.apply();
    } catch (JSONException e) {
      Log.e(TAG, "failed storage", e);
    }

    broadcastForLocalMessaging(bundle);
  }

  public void cancelNotification(String notificationId) {
    cancelAlarm(notificationId);
    SharedPreferences.Editor editor = sharedConfig.edit();
    editor.remove(notificationId);
    editor.apply();
  }

  public void cancelAllNotifications() {
    java.util.Map<String, ?> keyMap = sharedConfig.getAll();
    SharedPreferences.Editor editor = sharedConfig.edit();
    for (java.util.Map.Entry<String, ?> entry : keyMap.entrySet()) {
      cancelAlarm(entry.getKey());
    }
    editor.clear();
    editor.apply();
  }

  public void removeDeliveredNotification(String notificationId) {
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(notificationId.hashCode());
  }

  public void removeAllDeliveredNotifications() {
    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancelAll();
  }

  public void cancelAlarm(String notificationId) {
    Intent notificationIntent = new Intent(context, LocalMessagingReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, notificationId.hashCode(), notificationIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    getAlarmManager().cancel(pendingIntent);
  }

  public ArrayList<Bundle> getScheduledNotifications() {
    ArrayList<Bundle> array = new ArrayList<Bundle>();
    java.util.Map<String, ?> keyMap = sharedConfig.getAll();
    for (java.util.Map.Entry<String, ?> entry : keyMap.entrySet()) {
      try {
        JSONObject json = new JSONObject((String) entry.getValue());
        Bundle bundle = ConverterHelper.fromJsonToBundle(json);
        array.add(bundle);
      } catch (JSONException e) {
        Log.e(TAG, "parse json", e);
      }
    }
    return array;
  }

  private String getTitle(Bundle bundle) {
    String title = bundle.getString("title");
    if (title == null) {
      ApplicationInfo appInfo = context.getApplicationInfo();
      title = context.getPackageManager().getApplicationLabel(appInfo).toString();
    }
    return title;
  }

  private void clearScheduleNotificationOnceFired(Bundle bundle) {
    if (!bundle.containsKey("repeat_interval") && bundle.containsKey("fire_date")) {
      SharedPreferences.Editor editor = sharedConfig.edit();
      editor.remove(bundle.getString("id"));
      editor.apply();
    }
  }

  private void notifyLocalMessageWhenBackground(Bundle bundle, Class intentClass,
      NotificationCompat.Builder notification) {
    Intent intent = new Intent(context, intentClass);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    intent.putExtras(bundle);
    intent.setAction(bundle.getString("click_action"));

    int notifyID = bundle.containsKey("id") ? bundle.getString("id", "").hashCode() : (int) System.currentTimeMillis();

    PendingIntent pendingIntent = PendingIntent.getActivity(context, notifyID, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationManager notificationManager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);

    notification.setContentIntent(pendingIntent);

    Notification info = notification.build();

    if (bundle.containsKey("tag")) {
      String tag = bundle.getString("tag");
      notificationManager.notify(tag, notifyID, info);
    } else {
      notificationManager.notify(notifyID, info);
    }

    broadcastForLocalMessaging(bundle);
  }

  private void configureLights(Bundle bundle, NotificationCompat.Builder notification) {
    if (bundle.getBoolean("lights")) {
      notification.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
    }
  }

  private void configureVibrate(Bundle bundle, NotificationCompat.Builder notification) {
    if (bundle.containsKey("vibrate")) {
      long vibrate = bundle.getLong("vibrate", Math.round(bundle.getDouble("vibrate", bundle.getInt("vibrate"))));
      if (vibrate > 0) {
        notification.setVibrate(new long[] { 0, vibrate });
      } else {
        notification.setVibrate(null);
      }
    }
  }

  private void configureColor(Bundle bundle, NotificationCompat.Builder notification) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      notification.setCategory(NotificationCompat.CATEGORY_CALL);

      String color = bundle.getString("color");
      if (color != null) {
        notification.setColor(Color.parseColor(color));
      }
    }
  }

  private void configureSound(Bundle bundle, NotificationCompat.Builder notification) {
    String soundName = bundle.getString("sound", "default");

    if (!soundName.equalsIgnoreCase("default")) {
      Resources res = context.getResources();
      String packageName = context.getPackageName();

      int soundResourceId = res.getIdentifier(soundName, "raw", packageName);
      if (soundResourceId == 0) {
        soundName = soundName.substring(0, soundName.lastIndexOf('.'));
        soundResourceId = res.getIdentifier(soundName, "raw", packageName);
      }

      notification.setSound(Uri.parse("android.resource://" + packageName + "/" + soundResourceId));
    }
  }

  private void configureBigText(Bundle bundle, NotificationCompat.Builder notification) {
    String bigText = bundle.getString("big_text");

    if (bigText != null) {
      notification.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText));
    }
  }

  private void configureLargeIcon(Bundle bundle, NotificationCompat.Builder notification) {
    String largeIcon = bundle.getString("large_icon");

    if (largeIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      if (largeIcon.startsWith("http://") || largeIcon.startsWith("https://")) {
        Bitmap bitmap = getBitmapFromURL(largeIcon);
        notification.setLargeIcon(bitmap);
      } else {
        int largeIconResId = context.getResources().getIdentifier(largeIcon, "mipmap", context.getPackageName());
        Bitmap largeIconBitmap = BitmapFactory.decodeResource(context.getResources(), largeIconResId);
        if (largeIconResId != 0) {
          notification.setLargeIcon(largeIconBitmap);
        }
      }
    }
  }

  private void configureSmallIcon(Bundle bundle, NotificationCompat.Builder notification) {
    String smallIcon = bundle.getString("icon", "ic_launcher");
    int smallIconResId = context.getResources().getIdentifier(smallIcon, "mipmap", context.getPackageName());
    notification.setSmallIcon(smallIconResId);
  }

  private void configurePriority(Bundle bundle, NotificationCompat.Builder notification) {
    String priority = bundle.getString("priority", "high");

    switch (priority) {
      case "min":
        notification.setPriority(NotificationCompat.PRIORITY_MIN);
        break;
      case "high":
        notification.setPriority(NotificationCompat.PRIORITY_HIGH);
        break;
      case "max":
        notification.setPriority(NotificationCompat.PRIORITY_MAX);
        break;
      default:
        notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }
  }

  private Bitmap getBitmapFromURL(String strURL) {
    try {
      URL url = new URL(strURL);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      InputStream input = connection.getInputStream();
      return BitmapFactory.decodeStream(input);
    } catch (IOException e) {
      Log.e(TAG, "error getBitmapFromURL()", e);
      return null;
    }
  }

  private Class getMainActivityClass() {
    String packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "error on getMainActivityClass()", e);
      return null;
    }
  }

  private AlarmManager getAlarmManager() {
    return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  private void broadcastForLocalMessaging(Bundle bundle) {
    Log.d(TAG, "broadcast intent before showing");

    Intent i = new Intent(Constants.INTENT_RECEIVE_LOCAL_NOTIFICATION);
    i.putExtras(bundle);
    context.sendOrderedBroadcast(i, null);
  }

}
