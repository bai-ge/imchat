package com.baige;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.baige.data.entity.AppPackgeInfo;
import com.baige.data.entity.FileInfo;
import com.baige.service.DaemonService;
import com.baige.service.PullService;
import com.baige.util.Loggerx;
import com.baige.util.Tools;

import java.io.File;
import java.util.Map;


public class BaseApplication extends Application {

    private final static String TAG = BaseApplication.class.getCanonicalName();

    private static BaseApplication self;

    public static String PACKAGE_NAME = "";

    public static String headImgPath = "/head";

    public static String tmpPath = "/tmp";

    public static String downloadPath = Environment.getExternalStorageDirectory()
            + File.separator + "IMchat"+ File.separator + "download";

    public String getDiskCacheDir(Context context) {
        String cachePath = null;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return cachePath;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        PACKAGE_NAME = getPackageName();
        headImgPath = getDiskCacheDir(getAppContext()) + File.separator + "head";
        tmpPath =  getDiskCacheDir(getAppContext()) + File.separator + "tmp";

        if(Tools.checkPermissionWriteExternalStorage(getApplicationContext())){
            Loggerx.bWriteToFile = true;
        }
        //日志管理
        Loggerx.d(TAG, "打开应用");

        //守护服务
//        startService(new Intent(this, DaemonService.class));
    }

    public static AppPackgeInfo getApkPackgageInfo(String archiveFilePath) {

        PackageManager pm = self.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);

        AppPackgeInfo pkgInfo = null;
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName; // 得到安装包名称
            String version = info.versionName; // 得到版本信息
            Drawable icon = pm.getApplicationIcon(appInfo);// 得到图标信息
            pkgInfo = new AppPackgeInfo(icon, appName, version, packageName);
        }
        return pkgInfo;
    }

    public static Context getAppContext() {
        if (self != null) {
            return self.getApplicationContext();
        }
        return null;
    }


}
