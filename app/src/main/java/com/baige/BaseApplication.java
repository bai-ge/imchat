package com.baige;


import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baige.common.Parm;
import com.baige.data.entity.AppPackgeInfo;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.BaseObserver;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.observer.FileViewObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.observer.LastChatMessageObservable;
import com.baige.data.source.cache.CacheRepository;
import com.baige.imchat.MainActivity;
import com.baige.imchat.R;
import com.baige.service.DaemonService;
import com.baige.service.PullService;
import com.baige.util.BitmapTools;
import com.baige.util.ImageLoader;
import com.baige.util.Loggerx;
import com.baige.util.Tools;

import java.io.File;
import java.util.List;
import java.util.Map;


public class BaseApplication extends Application {

    private final static String TAG = BaseApplication.class.getCanonicalName();

    private static BaseApplication self;

    public static String PACKAGE_NAME = "";

    public static String headImgPath = "/head";

    public static String tmpPath = "/tmp";

    private Handler mHandler;

    private int mActivityResumedCount = 0;

    private static final long CHECK_DELAY = 500;

    private NotificationManager mNotificationManager; //通知服务

    private final static int FRIEND_NOTIFIED = 101;

    private final static int MSG_NOTIFIED = 102;

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

        mHandler = new Handler();
        this.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

    public static BaseApplication getInstance() {
        return self;
    }

    public static Context getAppContext() {
        if (self != null) {
            return self.getApplicationContext();
        }
        return null;
    }

    public static void showTip(String text){
        Toast.makeText(getAppContext(), text, Toast.LENGTH_LONG).show();
    }

    private ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivityCreated()");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivityStarted()");
        }

        @Override
        public void onActivityResumed(Activity activity) {

            Log.v(TAG, activity.getLocalClassName() + ".onActivityResumed()");
            mActivityResumedCount++;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //发送通知
//                    if (isScreenOn() && !isBackground()) {
//                        mApplicationBroadcastSender.onBecameForeground();
//                    }
                }
            }, CHECK_DELAY);

        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivityPaused()");
            mActivityResumedCount--;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //发送通知
//                    if (isScreenOn() && isBackground()) {
//                        mApplicationBroadcastSender.onBecameBackground();
//                    }
                }
            }, CHECK_DELAY);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivityStopped()");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivitySaveInstanceState()");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.v(TAG, activity.getLocalClassName() + ".onActivityDestroyed()");
        }
    };

    public boolean isBackground() {
        if (mActivityResumedCount > 0) {
            Log.i("IMChatApp", "前台运行");
            return false;
        } else {
            Log.i("IMChatApp", "后台运行");
            return true;
        }
    }


    public void initChatInfo(ChatMsgInfo chatMsgInfo) {
        if (chatMsgInfo != null) {
            int sendId = chatMsgInfo.getSenderId();
            User user = CacheRepository.getInstance().who();
            if (sendId == user.getId()) {
                chatMsgInfo.setShowType(Parm.MSG_IS_SEND);
                chatMsgInfo.setUserName("我");
                chatMsgInfo.setUserImgName(user.getImgName());
            } else {
                FriendView friendView = CacheRepository.getInstance().getFriendViewObservable().get(chatMsgInfo.getSenderId());
                chatMsgInfo.setShowType(Parm.MSG_IS_RECEIVE);
                if(friendView != null){
                    chatMsgInfo.setUserName(friendView.getSuitableName());
                    chatMsgInfo.setUserImgName(friendView.getImgName());
                }
            }
        }
    }
    /*
小图标，通过 setSmallIcon() 方法设置
标题，通过 setContentTitle() 方法设置
内容，通过 setContentText() 方法设置
*/
    public void showChatInform(ChatMsgInfo chatMsgInfo){
        if(isBackground()){
            initChatInfo(chatMsgInfo);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getAppContext());
            builder.setSmallIcon(R.mipmap.ic_launcher)//通知图标
                    .setOngoing(true)//true，设置他为一个正在进行的通知
                    .setAutoCancel(true)//用户点击就自动消失
                    .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))//Notification.FLAG_AUTO_CANCEL
                    .setContent(getRemoteViewForChat(chatMsgInfo)); //根据当前版本返回一个合适的视图
            mNotificationManager.notify(MSG_NOTIFIED, builder.build());
        }

    }

    public void showFriendInform(FriendView friendView){
        if(isBackground()){
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getAppContext());
            builder.setSmallIcon(R.mipmap.ic_launcher)//通知图标
                    .setOngoing(true)//true，设置他为一个正在进行的通知
                    .setAutoCancel(true)//用户点击就自动消失
                    .setContentIntent(getDefalutIntent(Notification.FLAG_AUTO_CANCEL))//Notification.FLAG_AUTO_CANCEL
                    .setContent(getRemoteViewForFriend(friendView)); //根据当前版本返回一个合适的视图
            mNotificationManager.notify(FRIEND_NOTIFIED, builder.build());
        }
    }

    protected void cancelNotification() {
        mNotificationManager.cancel(MSG_NOTIFIED);
        mNotificationManager.cancel(FRIEND_NOTIFIED);
    }


    private RemoteViews getRemoteViewForChat(ChatMsgInfo chatMsgInfo){
        RemoteViews remoteViews = new RemoteViews(getAppContext().getPackageName(), R.layout.notify_normal);
        remoteViews.setTextViewText(R.id.notify_title, chatMsgInfo.getUserName());
        remoteViews.setTextViewText(R.id.notify_time, Tools.getSuitableTimeFormat(System.currentTimeMillis()));
        remoteViews.setTextViewText(R.id.notify_content, chatMsgInfo.getContext());
        //设置头像
        String url = BaseApplication.headImgPath + File.separator + chatMsgInfo.getUserImgName();
        Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url+"notify");
        if(bitmap != null){
            remoteViews.setImageViewBitmap(R.id.notify_img, bitmap);
        }else{
            bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
            if(bitmap == null){
                bitmap = ImageLoader.decodeSampledBitmapFromResource(url, BitmapTools.dp2px(getAppContext(), 120));
            }
            if(bitmap != null){
                Bitmap bm =  BitmapTools.drawCircleView(bitmap, BitmapTools.dp2px(getAppContext(), 45), BitmapTools.dp2px(getAppContext(), 45));
                ImageLoader.getInstance().addBitmapToMemoryCache(url+"notify", bm);
                remoteViews.setImageViewBitmap(R.id.notify_img, bm);
            }
        }
        return remoteViews;
    }

    private RemoteViews getRemoteViewForFriend(FriendView friendView){
        RemoteViews remoteViews = new RemoteViews(getAppContext().getPackageName(), R.layout.notify_normal);
        remoteViews.setTextViewText(R.id.notify_title, "好友请求");
        remoteViews.setTextViewText(R.id.notify_time, Tools.getSuitableTimeFormat(System.currentTimeMillis()));
        remoteViews.setTextViewText(R.id.notify_content, friendView.getSuitableName() + "请求添加您为好友");
        //设置头像
        String url = BaseApplication.headImgPath + File.separator + friendView.getImgName();
        Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url+"notify");
        if(bitmap != null){
            remoteViews.setImageViewBitmap(R.id.notify_img, bitmap);
        }else{
            bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
            if(bitmap == null){
                bitmap = ImageLoader.decodeSampledBitmapFromResource(url, BitmapTools.dp2px(getAppContext(), 120));
            }
            if(bitmap != null){
                Bitmap bm =  BitmapTools.drawCircleView(bitmap, BitmapTools.dp2px(getAppContext(), 45), BitmapTools.dp2px(getAppContext(), 45));
                ImageLoader.getInstance().addBitmapToMemoryCache(url+"notify", bm);
                remoteViews.setImageViewBitmap(R.id.notify_img, bm);
            }
        }
        return remoteViews;
    }

    private PendingIntent getDefalutIntent(int flags) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getAppContext(), 0, new Intent(getAppContext(), MainActivity.class), flags);
        return pendingIntent;
    }



}
