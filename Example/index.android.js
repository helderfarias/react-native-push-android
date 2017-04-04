/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from "react";
import { AppRegistry, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import PushNotificationAndroid from 'react-native-push-android';

export default class Example extends Component {
  state = {
    token: null
  };

  componentWillMount() {
    PushNotificationAndroid.getInitialNotification().then(initial => {
      console.log('getInitialNotification => ', initial);
    });

    PushNotificationAndroid.getToken().then(token => {
      this.setState({ token: token });
    });
    
    PushNotificationAndroid.addEventListener('localNotification', (details) => {
      console.log('localNotification => ', details);
    });

    PushNotificationAndroid.addEventListener('notification', (details) => {
      console.log('remoteNotification => ', details);
    });

    PushNotificationAndroid.addEventListener('refreshToken', (token) => {
      console.log('remoteRefreshToken => ', token);
    });
  }

  componentWillUnMount() {
    PushNotificationAndroid.removeEventListener('localNotification');
    PushNotificationAndroid.removeEventListener('notification');
    PushNotificationAndroid.removeEventListener('refreshToken'); 
  }

  sendLocalNotificationFake = () => {    
    require('RCTDeviceEventEmitter').emit('FCMLocalNotificationReceived', {
      "title": "title",
      "body": "body",
      "priority": "high",
      "click_action": "fcm.ACTION.HELLO"
    });
  }

  sendRemoteNotificationFake = () => {
    require('RCTDeviceEventEmitter').emit('FCMNotificationReceived', {
      "title": "title",
      "body": "notif.bod",
      "priority": "high",
      "click_action": "fcm.ACTION.HELLO"
    })
  }  

  sendLocalNotificationNormal = () => {    
    PushNotificationAndroid.notify({
      "title": "title",
      "body": "body",
      "priority": "high",
      "click_action": "fcm.ACTION.HELLO"
    });
  }  

  render() {
    let { token } = this.state;

    console.log(token);

    return (
      <View style={styles.container}>
        <Text style={styles.welcome}>
          Welcome!
        </Text>

        <Text style={styles.instructions}>
          Token: {this.state.token}
        </Text>

        <TouchableOpacity 
          onPress={this.sendLocalNotificationFake}
          style={styles.button}>
          <Text style={styles.buttonText}>Local Notification Fake</Text>
        </TouchableOpacity>

        <TouchableOpacity
          onPress={this.sendRemoteNotificationFake}
          style={styles.button}>
          <Text style={styles.buttonText}>Remote Notification Fake</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          onPress={this.sendLocalNotificationNormal}
          style={styles.button}>
          <Text style={styles.buttonText}>Local Notification Normal</Text>
        </TouchableOpacity>        
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  },
  welcome: {
    fontSize: 20,
    textAlign: "center",
    margin: 10
  },
  instructions: {
    textAlign: "center",
    color: "#333333",
    marginBottom: 5
  },
  button: {
    backgroundColor: "teal",
    paddingHorizontal: 20,
    paddingVertical: 10,
    marginVertical: 15,
    borderRadius: 10
  },
  buttonText: {
    color: "white",
    backgroundColor: "transparent"
  }
});

AppRegistry.registerComponent("Example", () => Example);
