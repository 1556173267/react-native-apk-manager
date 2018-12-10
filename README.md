# react-native-apk-manager
React Native bridging library for android to manager apk.(install apk, uninstall apk, open apk, check app is installed or not, check app is installed or not with channel)

## TOC

* [Installation](#installation)
* [Linking](#linking)
* [Usage](#usage)
* [API](#api)

## Installation

Using npm:

```shell
npm install --save react-native-apk-manager
```

or using yarn:

```shell
yarn add react-native-apk-manager
```

## Linking

### Automatic

```shell
react-native link react-native-apk-manager
```

(or using [`rnpm`](https://github.com/rnpm/rnpm) for versions of React Native < 0.27)

```shell
rnpm link react-native-apk-manager
```

### Manual

<details>
    <summary>Android</summary>

* **_optional_** in `android/build.gradle`:

```gradle
...
  ext {
    // dependency versions
    compileSdkVersion = "<Your compile SDK version>" // default: 27
    targetSdkVersion = "<Your target SDK version>" // default: 27
  }
...
```

* in `android/app/build.gradle`:

```diff
dependencies {
    ...
    compile "com.facebook.react:react-native:+"  // From node_modules
+   compile project(':react-native-apk-manager')
}
```

* in `android/settings.gradle`:

```diff
...
include ':app'
+ include ':react-native-apk-manager'
+ project(':react-native-apk-manager').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-apk-manager/android')
```

#### With React Native 0.29+

* in `MainApplication.java`:

```diff
+ import com.superhao.react_native_apk_manager.ApkManagerPackage;

  public class MainApplication extends Application implements ReactApplication {
    ......

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
+         new ApkManagerPackage(),
          new MainReactPackage()
      );
    }

    ......
  }
```
</details>

## Usage

```js
import * as ApkManager from 'react-native-apk-manager';
```

## API

| Method | 	Params | Return Type |
| :----- | :------ | :---------- |
| [isAppInstalled()](#isAppInstalled()) | `pageName<string>` | `Promise<boolean>` |
| [isAppsInstalled()](#isAppsInstalled) | `packageNames<Array<string>>` | `Promise<Array<boolean>>` |
| [installApk()](#installApk) | `filePath<string>` | `void` |
| [uninstallApk()](#uninstallApk) | `packageName<string>` | `void` |
| [openApk()](#openApk) | `packageName<string>` | `void` |
| [isExpChannel()](#isExpChannel) | `packageName<string>, metaDataName<string>, channel<string>` | `Promise<boolean>` |
| [isExpChannels()](#isExpChannel) | `packageNames<Array<string>>, metaDataNames<Array<string>>, channels<Array<string>>` | `Promise<Array<boolean>>` |

### isAppInstalled(`pageName<string>`): `Promise<boolean>`

Check app isInstalled with packageName.

**Examples**

```js
ApkManager.isAppInstalled('com.lengjing.ktyaokongc').then((data)=> {
  // true or false
});
```

---

### isAppsInstalled(`packageNames<Array<string>>`):`Promise<Array<boolean>>`

Check apps isInstalled with packageName List.

**Examples**

```js
ApkManager.isAppInstalled(['com.lengjing.ktyaokongc', 'test.tets', 'xxxx', 'xxxx']).then((data)=> {
  // [true, false, false, true]
});
```

---

### installApk(`filePath<string>`)

Install apk with apkFilePath.

**Examples**

```js
ApkManager.installApk('storage/emulated/0/renlaifeng_download/test.apk');
```
---

### uninstallApk(`packageName<string>`)

Uninstall apk with packageName.

**Examples**

```js
ApkManager.uninstallApk('com.lengjing.ktyaokongc');
```

---

### openApk(`packageName<string>`)

Open other app with packageName.

**Examples**

```js
ApkManager.openApk('com.lengjing.ktyaokongc');
```
---

### isExpChannel(`packageName<string>, metaDataName<string>, channel<string>`):`Promise<boolean>`

Check special channel app is installed with packageName, metaName, special channel.

* Before use, you should know app's metaName and channel in AndroidManifest.xml.
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.ownmoduletest"> => // here is the packageName 'com.ownmoduletest'
    ...
    <application
      ...
      <meta-data android:name="JPUSH_APPKEY" android:value="1232444"/> => // here is the metaDataName 'JPUSH_APPKEY'
      ...
    </application>
</manifest>
```

**Examples**

```js
ApkManager.isExpChannel('com.ownmoduletest', 'JPUSH_APPKEY', '1232444').then((data)=> {
  // true
});

ApkManager.isExpChannel('com.ownmoduletest', 'JPUSH_APPKEY', '11111').then((data)=> {
  // false
});

ApkManager.isExpChannel('com.ownmoduletest', 'otherkeyname', '1232444').then((data)=> {
  // false
});
```

---

### isExpChannels(`packageNames<Array<string>>, metaDataNames<Array<string>>, channels<Array<string>>`):`Promise<Array<boolean>>`

Check special channel apps is installed with packageNames, metaNames, special channels.

* Before use, you should know app's metaName and channel in AndroidManifest.xml.
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.ownmoduletest"> => // here is the packageName 'com.ownmoduletest'
    ...
    <application
      ...
      <meta-data android:name="JPUSH_APPKEY" android:value="1232444"/> => // here is the metaDataName 'JPUSH_APPKEY'
      ...
    </application>
</manifest>
```

**Examples**

```js
ApkManager.isExpChannels(['com.ownmoduletest', 'com.test', 'xxxxxx'], ['JPUSH_APPKEY', 'test_key', 'xxxxxxx'], ['1232444', 'wanted channel', 'wanted channel']).then((data)=> {
  // [true, true, true]
});

ApkManager.isExpChannels(['com.ownmoduletest', 'com.test', 'xxxxxx'], ['JPUSH_APPKEY', 'test_key', 'xxxxxxx'], ['11111', 'wanted channel', 'wanted channel']).then((data)=> {
  // [false, true, true]
});

ApkManager.isExpChannels(['com.ownmoduletest', 'com.test', 'xxxxxx'], ['other_key', 'test_key', 'xxxxxxx'], ['1232444', 'wanted channel', 'wanted channel']).then((data)=> {
  // [false, true, true]
});
```
---
