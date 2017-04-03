
# react-native-push-android

## Getting started

`$ npm install react-native-push-android --save`

### Mostly automatic installation

`$ react-native link react-native-push-android`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import br.com.helder.push.RNPushAndroidPackage;` to the imports at the top of the file
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


## Usage
```javascript
import RNPushAndroid from 'react-native-push-android';

// TODO: What to do with the module?
RNPushAndroid;
```
  