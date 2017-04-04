
import { 
  NativeModules, 
  NativeAppEventEmitter 
} from 'react-native';

const { RNPushAndroid } = NativeModules;

const scheduleLocalNotification = (details) => {
  RNPushAndroid.scheduleLocalNotification(details);
}

const cancelAllLocalNotifications = () => {
  RNPushAndroid.cancelAllLocalNotifications();
}

const cancelLocalNotifications = (notificationID) => {
  RNPushAndroid.cancelLocalNotifications(notificationID);
}

const getScheduledLocalNotifications = () => {
  return RNPushAndroid.getScheduledLocalNotifications();
}

const getInitialNotification = () => {
  return RNPushAndroid.getInitialNotification();
}

const getToken = () => {
  return RNPushAndroid.getToken();
}

const subscribeToTopic = (topic) => {
  RNPushAndroid.subscribeToTopic(topic);
}

const unsubscribeFromTopic = (topic) => {
  RNPushAndroid.unsubscribeFromTopic(topic);
}

const notify = (details) => {
  RNPushAndroid.sendLocalNotification(details);
}

const addEventListener = (event, callback) => {
  if (event == 'localNotification') {
      NativeAppEventEmitter.addListener('FCMLocalNotificationReceived', callback);
      return;
  }

  if (event == 'notification') {
      NativeAppEventEmitter.addListener('FCMNotificationReceived', callback);
      return;
  }

  if (event == 'refreshToken') {
      NativeAppEventEmitter.addListener('FCMTokenRefreshed', callback);
      return;
  }
}

const removeEventListener = (event) => {
  if (event == 'localNotification') {
      NativeAppEventEmitter.removeEventListener('FCMLocalNotificationReceived');
      return;
  }

  if (event == 'notification') {
      NativeAppEventEmitter.removeEventListener('FCMNotificationReceived');
      return;
  }

  if (event == 'refreshToken') {
      NativeAppEventEmitter.removeEventListener('FCMTokenRefreshed');
      return;
  }  
}

export default {
  scheduleLocalNotification,
  cancelAllLocalNotifications,
  cancelLocalNotifications,
  getScheduledLocalNotifications,
  addEventListener,
  removeEventListener,
  getInitialNotification,
  getToken,
  notify,
};
