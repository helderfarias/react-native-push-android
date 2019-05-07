# react-native-push-android

## Getting started

`$ npm install react-native-push-android --save`

### Mostly automatic installation

`$ react-native link react-native-push-android`

### Manual installation

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`

- Add `import br.com.helderfarias.pushandroid.RNPushAndroidPackage;` to the imports at the top of the file
- Add `new RNPushAndroidPackage()` to the list returned by the `getPackages()` method

2. Append the following lines to `android/settings.gradle`:
   ```
   include ':react-native-push-android'
   project(':react-native-push-android').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-push-android/android')
   ```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
   ```
     compile project(':react-native-push-android')
   ```

## Configuration

- Step 1 - Edit \$PROJECT_NAME/android/build.gradle

```diff
 dependencies {
    ...
    classpath 'com.android.tools.build:gradle:2.0.0'
+   classpath 'com.google.gms:google-services:3.0.0'
    ...
```

- Step 2 - Edit \$PROJECT_NAME/android/app/build.gradle

```diff
    ...
    apply plugin: "com.android.application"
+   apply plugin: 'com.google.gms.google-services'
    ...
```

- Step 3 - Edit android/app/src/main/AndroidManifest.xml

```diff
    ...
    <application
      android:name=".MainApplication"
      android:allowBackup="true"
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher"
      android:theme="@style/AppTheme">
+       <receiver android:name="br.com.helderfarias.pushandroid.LocalMessagingReceiver" />

+       <receiver android:enabled="true" android:exported="true"
+           android:name="br.com.helderfarias.pushandroid.SystemBootEventReceiver">
+           <intent-filter>
+               <action android:name="android.intent.action.BOOT_COMPLETED"/>
+               <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
+               <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
+               <category android:name="android.intent.category.DEFAULT" />
+           </intent-filter>
+       </receiver>

+       <service android:name="br.com.helderfarias.pushandroid.MessagingService">
+           <intent-filter>
+               <action android:name="com.google.firebase.MESSAGING_EVENT"/>
+           </intent-filter>
+       </service>

        <activity
            android:name=".MainActivity"
            android:label="@string/app_name"
            android:configChanges="keyboard|keyboardHidden|orientation|screenSize"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
+           <intent-filter>
+               <action android:name="fcm.ACTION.HELLO" />
+               <category android:name="android.intent.category.DEFAULT" />
+           </intent-filter>
        </activity>

        <activity android:name="com.facebook.react.devsupport.DevSettingsActivity" />
    </application>
    ...
```

## Methods

- static getInitialNotification()

```
  This method returns a promise that resolves to either
  the notification object if the app was launched by a push notification, or null otherwise.
```

- static getToken()

```
  This method returns a promise with refresh token fcm, or null otherwise.
```

- static addEventListener(event)

```
  Attaches a listener to remote or local notification events for app.

  Valid events are:
    - localNotification: Fired when a local notification is received.
    - notification: Fired when a remote notification is received by fcm.
    - refreshToken: Fired when a remote token refreshed.
```

- static removeEventListener(event, callback)

```
  Removes the event listener. Do this in componentWillUnmount to prevent memory leaks.
```

- static notify(details)

```
  Create localNotification.

  details is an object containing:
    - title
    - body
    - ticket
    - auto_cancel
    - number
    - sub_text
    - group
    - data
    - lights
    - priority
    - color
    - sound
    - big_text
    - icon
    - large_icon
```

## Usage

```javascript
import React, { Component } from "react";
import { AppRegistry, Text, TouchableOpacity, View } from "react-native";
import PushNotificationAndroid from "react-native-push-android";

export default class Example extends Component {
  _localNotificationEvent = null;
  _notificationEvent = null;
  _refreshTokenEvent = null;

  state = {
    token: null
  };

  componentWillMount() {
    PushNotificationAndroid.getInitialNotification().then(initial => {
      console.log("getInitialNotification => ", initial);
    });

    PushNotificationAndroid.getToken().then(token => {
      this.setState({ token: token });
    });

    this._localNotificationEvent = PushNotificationAndroid.addEventListener(
      "localNotification",
      details => {
        console.log("localNotification => ", details);
      }
    );

    this._notificationEvent = PushNotificationAndroid.addEventListener(
      "notification",
      details => {
        console.log("remoteNotification => ", details);
      }
    );

    this._refreshTokenEvent = PushNotificationAndroid.addEventListener(
      "refreshToken",
      token => {
        console.log("remoteRefreshToken => ", token);
      }
    );
  }

  componentWillUnMount() {
    this._localNotificationEvent.remove();
    this._notificationEvent.remove();
    this._refreshTokenEvent.remove();

    // You can remove all listener events
    PushNotificationAndroid.removeAllListeners("localNotification");
    PushNotificationAndroid.removeAllListeners("notification");
    PushNotificationAndroid.removeAllListeners("refreshToken");
  }

  sendLocalNotificationNormal = () => {
    PushNotificationAndroid.notify({
      title: "title",
      body: "body",
      click_action: "fcm.ACTION.HELLO"
    });
  };

  render() {
    let { token } = this.state;

    return (
      <View>
        <Text>Token: {this.state.token}</Text>

        <TouchableOpacity onPress={this.sendLocalNotificationNormal}>
          <Text>Local Notification</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

AppRegistry.registerComponent("Example", () => Example);
```
