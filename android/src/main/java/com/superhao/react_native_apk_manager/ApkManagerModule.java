package com.superhao.react_native_apk_manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.File;

public class ApkManagerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    final BroadcastReceiver apkInstallListener;
    String apkFile;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == 10086) {
                installApk(apkFile);
            }
        }
    };

    public ApkManagerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(mActivityEventListener);
        final ReactApplicationContext ctx = reactContext;

        apkInstallListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //接收替换广播
                if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
                    //TODO
                    Log.e("test", "替换成功");
                    if (ctx.hasActiveCatalystInstance()) {
                        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("apkManagerListener", "替换成功");
                    }
                } else if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
                    //TODO
                    //接收安装广播
                    Log.e("test", "安装成功");
                    if (ctx.hasActiveCatalystInstance()) {
                        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("apkManagerListener", "安装成功");
                    }
                } else if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                    //TODO
                    //接收卸载广播
                    Log.e("test", "卸载成功");
                    if (ctx.hasActiveCatalystInstance()) {
                        ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("apkManagerListener", "卸载成功");
                    }
                }
            }
        };
        ctx.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "ApkManagerModule";
    }

    @ReactMethod
    public void uninstallApk(String packageName) {
        Uri uri = Uri.parse("package:" + packageName);
        Intent intent = new Intent(Intent.ACTION_DELETE, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getReactApplicationContext().startActivity(intent);
    }

    @ReactMethod
    public void installApk(String filePath) {
        apkFile = filePath;
        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            boolean b = getReactApplicationContext().getPackageManager().canRequestPackageInstalls();

            if (!b) {
                //没有权限
                startInstallPermissionSettingActivity();
            } else {
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri contentUri = FileProvider.getUriForFile(
                        getCurrentActivity()
                        , this.getReactApplicationContext().getPackageName() + ".provider"
                        , apkFile);
                intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
                // Validate that the device can open the file
                PackageManager pm = getCurrentActivity().getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    this.getReactApplicationContext().startActivity(intent);
                }
            }
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            getReactApplicationContext().startActivity(intent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        Uri packageURI = Uri.parse("package:" + this.getReactApplicationContext().getPackageName());
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        getReactApplicationContext().startActivityForResult(intent, 10086, null);
    }

    @ReactMethod
    public void isExpChannel(String packageName, String metaDataName, String channel, final Promise promise) {

        PackageManager manager = getReactApplicationContext().getPackageManager();
        boolean installed = false;
        try {
            // 设置PackageManager.GET_META_DATA标识a位是必须的
            installed = isAppInstalled(packageName);
            if (installed) {
                PackageInfo info = manager.getPackageInfo(packageName,
                        PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA);
                String myChannel = info.applicationInfo.metaData.getString(metaDataName);
                if (myChannel == null) {
                    installed = false;
                } else if (!myChannel.equals(channel)) {
                    installed = false;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
            e.printStackTrace();
        }
        promise.resolve(installed);
    }

    @ReactMethod
    public void isExpChannels(ReadableArray packageNames, ReadableArray metaDataNames, ReadableArray channels, final Promise promise) {

        PackageManager manager = getReactApplicationContext().getPackageManager();
        WritableArray isInstallArray = new WritableNativeArray();

        for (int i = 0; i < packageNames.size(); i ++) {
            boolean installed = false;
            try {
                // 设置PackageManager.GET_META_DATA标识a位是必须的
                String packageName = packageNames.getString(i);
                String metaDataName = metaDataNames.getString(i);
                String channel = channels.getString(i);
                installed = isAppInstalled(packageName);
                if (!installed) {
                    PackageInfo info = manager.getPackageInfo(packageName,
                            PackageManager.GET_CONFIGURATIONS | PackageManager.GET_META_DATA);
                    String myChannel = info.applicationInfo.metaData.getString(metaDataName);
                    if (myChannel == null) {
                        installed = false;
                    } else if (!myChannel.equals(channel)) {
                        installed = false;
                    }
                    isInstallArray.pushBoolean(installed);
                }
            } catch (PackageManager.NameNotFoundException e) {
                installed = false;
                isInstallArray.pushBoolean(installed);
                e.printStackTrace();
            }
        }

        promise.resolve(isInstallArray);
    }

    @ReactMethod
    public void openApk(String packageName) {
        String package_name = packageName;
        PackageManager packageManager = getReactApplicationContext().getPackageManager();
        Intent it = packageManager.getLaunchIntentForPackage(package_name);
        getReactApplicationContext().startActivity(it);
    }

    @ReactMethod
    private boolean isAppInstalled(String packageName, final Promise promise) {
        PackageManager pm = getReactApplicationContext().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        promise.resolve(installed);
        return installed;
    }

    @ReactMethod
    private void isAppsInstalled(ReadableArray packageNames, final Promise promise) {
        PackageManager pm = getReactApplicationContext().getPackageManager();
        WritableArray isInstallArray = new WritableNativeArray();
        for (int i = 0; i < packageNames.size(); i ++) {
            boolean installed = false;
            try {
                String packageName = packageNames.getString(i);
                pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                installed = true;
                isInstallArray.pushBoolean(installed);
            } catch (PackageManager.NameNotFoundException e) {
                installed = false;
                isInstallArray.pushBoolean(installed);
            }
        }
        promise.resolve(isInstallArray);
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getReactApplicationContext().getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    // 注册监听  
    private void registerSDCardListener(Activity activity) {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        activity.registerReceiver(apkInstallListener, intentFilter);
    }

    @Override
    public void onHostResume() {
        final Activity activity = getCurrentActivity();
        if (activity == null) {
            FLog.e(ReactConstants.TAG, "no activity to register receiver");
            return;
        }
        registerSDCardListener(activity);
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        final Activity activity = getCurrentActivity();
        if (activity == null) return;
        try
        {
            activity.unregisterReceiver(apkInstallListener);
        }
        catch (java.lang.IllegalArgumentException e) {
            FLog.e(ReactConstants.TAG, "receiver already unregistered", e);
        }
    }
}
