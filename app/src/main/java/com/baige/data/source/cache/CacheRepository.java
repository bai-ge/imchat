package com.baige.data.source.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.baige.AppConfigure;
import com.baige.BaseApplication;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.observer.LastChatMessageObservable;
import com.baige.util.Tools;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


/**
 * Created by baige on 2018/3/20.
 */

public class CacheRepository {

    public static String ExternalStoragePath = Environment.getExternalStorageDirectory().getPath();

    private final static String TAG = CacheRepository.class.getCanonicalName();

    private static CacheRepository INSTANCE = null;


    private ChatMessageObservable chatMessageObservable;

    private FriendViewObservable friendViewObservable;

    private LastChatMessageObservable lastChatMessageObservable;

    private int fileSortType;

    private String currentPath = ExternalStoragePath;

    /* 文件名 App名*/
    public static Map<String, String> mAppNameMap = new LinkedHashMap<>();

    private User me;

    private boolean isLogin;

    private String serverIp;

    public String getServerIp(){
        if(Tools.isEmpty(serverIp)){
            readConfig(BaseApplication.getAppContext());
        }
        return serverIp;
    }

   static {
       //文件名，应用名称
       mAppNameMap.put("bluetooth","蓝牙接收文件");
       mAppNameMap.put("browser","浏览器");
       mAppNameMap.put("DCIM","相册");
       mAppNameMap.put("Download","系统下载");
       mAppNameMap.put("downloaded_rom","系统更新包");
       mAppNameMap.put("miliao","米聊");
       mAppNameMap.put("MiMarket","应用商店");
       mAppNameMap.put("MIUI","米柚");
       mAppNameMap.put("Music","音乐");
       mAppNameMap.put("mishop","小米商城");
       mAppNameMap.put("tmp","临时文件");
       mAppNameMap.put("Recordings","录音");
       mAppNameMap.put("huawei","华为");
       mAppNameMap.put("Ringtones","铃声");
       mAppNameMap.put("backups","ES浏览器备份");
       mAppNameMap.put("baidu","百度");
       mAppNameMap.put("BaiduMap","百度地图");
       mAppNameMap.put("QQBrowser","QQ浏览器");
       mAppNameMap.put("sogou","搜狗输入法");
       mAppNameMap.put("tencent","腾讯");
       mAppNameMap.put("360","360");
       mAppNameMap.put("360Browser","360浏览器");
       mAppNameMap.put("360Download","360助手");
       mAppNameMap.put("alipay","支付宝钱包");
       mAppNameMap.put("AirDroid","AirDroid");
       mAppNameMap.put("autonavi","高德地图");
       mAppNameMap.put("BaiduNetdisk","百度云");
       mAppNameMap.put("baofeng","暴风影音");
       mAppNameMap.put("CamScanner","CamScanner");
       mAppNameMap.put("com.taobao.taobao","淘宝");
       mAppNameMap.put("com.UCMobile","手机UC");
       mAppNameMap.put("dianxin","授权管理");
       mAppNameMap.put("egame","爱游戏");
       mAppNameMap.put("GitHub","GitHub");
       mAppNameMap.put("ickeck","腾讯视频");
       mAppNameMap.put("KuGou","酷狗音乐");
       mAppNameMap.put("msc","掌阅iReader");
       mAppNameMap.put("netease","网易");
       mAppNameMap.put("pptv_video_sdk","百度视频");
       mAppNameMap.put("QIYIVideo","奇艺视频");
       mAppNameMap.put("qqmusic","QQ音乐");
       mAppNameMap.put("sina","新浪");
       mAppNameMap.put("suning.ebuy","苏宁易购");
       mAppNameMap.put("tieba","百度贴吧");
       mAppNameMap.put("xtuome","超级课程表");
       mAppNameMap.put("Youdao","有道词典");
       mAppNameMap.put("youku","优酷");
       mAppNameMap.put("powerword","金山词霸");
       mAppNameMap.put("Musiclrc","华为音乐歌词");
       mAppNameMap.put("wandoujia","豌豆荚");
   }

    private CacheRepository() {
        readConfig(BaseApplication.getAppContext());
        chatMessageObservable = new ChatMessageObservable();
        friendViewObservable = new FriendViewObservable();
        lastChatMessageObservable = new LastChatMessageObservable();
    }

    public static CacheRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (CacheRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CacheRepository();
                }
            }
        }
        return INSTANCE;
    }

    public void registerDataChange(Observer observer){
        chatMessageObservable.addObserver(observer);
        friendViewObservable.addObserver(observer);
        lastChatMessageObservable.addObserver(observer);
    }
    public void unRegisterDataChange(Observer observer){
        chatMessageObservable.deleteObserver(observer);
        friendViewObservable.deleteObserver(observer);
        lastChatMessageObservable.deleteObserver(observer);
    }

    public ChatMessageObservable getChatMessageObservable() {
        return chatMessageObservable;
    }

    public FriendViewObservable getFriendViewObservable() {
        return friendViewObservable;
    }

    public LastChatMessageObservable getLastChatMessageObservable() {
        return lastChatMessageObservable;
    }

    public int getFileSortType() {
        return fileSortType;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String mCurrentPath) {
        this.currentPath = mCurrentPath;
    }

    public void setFileSortType(int fileSortType) {
        this.fileSortType = fileSortType;
    }

    /**
     * 登录成功之后不能覆盖该对象，只能单独设置某些属性
     *
     * @param user
     */
    public void setYouself(User user) {
        this.me = user;
    }

    public User who(){
        return me;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public void readConfig(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setLogin(preferences.getBoolean(AppConfigure.IS_LOGIN, false));

        if(me == null){
            me = new User();
        }
        me.setId(preferences.getInt(AppConfigure.KEY_USER_ID, 0));
        me.setName(preferences.getString(AppConfigure.KEY_USER_NAME, ""));
        me.setPassword(preferences.getString(AppConfigure.KEY_PASSWORD, ""));
        me.setAlias(preferences.getString(AppConfigure.KEY_USER_ALIAS, ""));
        me.setVerification(preferences.getString(AppConfigure.KEY_VERIFICATION, ""));
        me.setImgName(preferences.getString(AppConfigure.KEY_USER_IMG, ""));
        setLogin(preferences.getBoolean(AppConfigure.IS_LOGIN, false));
        serverIp = preferences.getString(AppConfigure.KEY_PHONE_SERVER_IP, AppConfigure.DEFAULT_PHONE_SERVER_IP);
    }

    public void saveConfig(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if(me != null){
            editor.putInt(AppConfigure.KEY_USER_ID, me.getid());
            editor.putString(AppConfigure.KEY_USER_NAME, me.getName());
            editor.putString(AppConfigure.KEY_USER_ALIAS, me.getAlias());
            editor.putString(AppConfigure.KEY_PASSWORD, me.getPassword());
            editor.putString(AppConfigure.KEY_VERIFICATION, me.getVerification());
            editor.putString(AppConfigure.KEY_USER_IMG, me.getImgName());
        }
        editor.putBoolean(AppConfigure.KEY_IS_LOGIN, isLogin());
        editor.putString(AppConfigure.KEY_PHONE_SERVER_IP, serverIp);
        editor.apply();
    }
}
