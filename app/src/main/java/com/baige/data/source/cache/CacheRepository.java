package com.baige.data.source.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.baige.AppConfigure;
import com.baige.BaseApplication;
import com.baige.data.entity.User;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.observer.FileViewObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.observer.LastChatMessageObservable;
import com.baige.pushcore.SendMessageBroadcast;
import com.baige.util.Tools;

import java.util.LinkedHashMap;
import java.util.Map;
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

    private FileViewObservable fileViewObservable;

    private int fileSortType;

    private String currentPath = ExternalStoragePath;

    /* 文件名 App名*/
    public static Map<String, String> mAppNameMap = new LinkedHashMap<>();

    private User me;

    private boolean isLogin;

    //铃声设置
    private boolean isSilence; //是否静音

    private String ringUri;

    private boolean phoneVibrate;


    //网络相关

    private boolean isP2PConnectSuccess = false;

    private String deviceId; //极光推送的设备ID

    private int serverPort = 12056;

    private int serverUdpPort = 12059;

    private String serverIp;


    private String localIp = null;

    private int localPort = 0;

    private String remoteIp = null;

    private int remotePort = 0;

    private int remoteUdpPort = 0;

    private int localUdpPort = 0;

    //文件传输
    private int slipWindowCount;
    private boolean isShareFile;



    public String getServerIp() {
        if (Tools.isEmpty(serverIp)) {
            readConfig(BaseApplication.getAppContext());
        }
        return serverIp;
    }

    public String getDeviceId() {
        if (Tools.isEmpty(deviceId)) {
            readConfig(BaseApplication.getAppContext());
        }
        return deviceId;
    }

    public boolean isP2PConnectSuccess() {
        return isP2PConnectSuccess;
    }

    public void setP2PConnectSuccess(boolean p2PConnectSuccess) {
        isP2PConnectSuccess = p2PConnectSuccess;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerUdpPort() {
        return serverUdpPort;
    }

    public void setServerUdpPort(int serverUdpPort) {
        this.serverUdpPort = serverUdpPort;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemoteUdpPort() {
        return remoteUdpPort;
    }

    public void setRemoteUdpPort(int remoteUdpPort) {
        this.remoteUdpPort = remoteUdpPort;
    }

    public int getLocalUdpPort() {
        return localUdpPort;
    }

    public void setLocalUdpPort(int localUdpPort) {
        this.localUdpPort = localUdpPort;
    }



    static {
        //文件名，应用名称
        mAppNameMap.put("bluetooth", "蓝牙接收文件");
        mAppNameMap.put("browser", "浏览器");
        mAppNameMap.put("DCIM", "相册");
        mAppNameMap.put("Download", "系统下载");
        mAppNameMap.put("downloaded_rom", "系统更新包");
        mAppNameMap.put("miliao", "米聊");
        mAppNameMap.put("MiMarket", "应用商店");
        mAppNameMap.put("MIUI", "米柚");
        mAppNameMap.put("Music", "音乐");
        mAppNameMap.put("mishop", "小米商城");
        mAppNameMap.put("tmp", "临时文件");
        mAppNameMap.put("Recordings", "录音");
        mAppNameMap.put("huawei", "华为");
        mAppNameMap.put("Ringtones", "铃声");
        mAppNameMap.put("backups", "ES浏览器备份");
        mAppNameMap.put("baidu", "百度");
        mAppNameMap.put("BaiduMap", "百度地图");
        mAppNameMap.put("QQBrowser", "QQ浏览器");
        mAppNameMap.put("sogou", "搜狗输入法");
        mAppNameMap.put("tencent", "腾讯");
        mAppNameMap.put("360", "360");
        mAppNameMap.put("360Browser", "360浏览器");
        mAppNameMap.put("360Download", "360助手");
        mAppNameMap.put("alipay", "支付宝钱包");
        mAppNameMap.put("AirDroid", "AirDroid");
        mAppNameMap.put("autonavi", "高德地图");
        mAppNameMap.put("BaiduNetdisk", "百度云");
        mAppNameMap.put("baofeng", "暴风影音");
        mAppNameMap.put("CamScanner", "CamScanner");
        mAppNameMap.put("com.taobao.taobao", "淘宝");
        mAppNameMap.put("com.UCMobile", "手机UC");
        mAppNameMap.put("dianxin", "授权管理");
        mAppNameMap.put("egame", "爱游戏");
        mAppNameMap.put("GitHub", "GitHub");
        mAppNameMap.put("ickeck", "腾讯视频");
        mAppNameMap.put("KuGou", "酷狗音乐");
        mAppNameMap.put("msc", "掌阅iReader");
        mAppNameMap.put("netease", "网易");
        mAppNameMap.put("pptv_video_sdk", "百度视频");
        mAppNameMap.put("QIYIVideo", "奇艺视频");
        mAppNameMap.put("qqmusic", "QQ音乐");
        mAppNameMap.put("sina", "新浪");
        mAppNameMap.put("suning.ebuy", "苏宁易购");
        mAppNameMap.put("tieba", "百度贴吧");
        mAppNameMap.put("xtuome", "超级课程表");
        mAppNameMap.put("Youdao", "有道词典");
        mAppNameMap.put("youku", "优酷");
        mAppNameMap.put("powerword", "金山词霸");
        mAppNameMap.put("Musiclrc", "华为音乐歌词");
        mAppNameMap.put("wandoujia", "豌豆荚");
    }

    private CacheRepository() {
        readConfig(BaseApplication.getAppContext());
        chatMessageObservable = new ChatMessageObservable();
        friendViewObservable = new FriendViewObservable();
        lastChatMessageObservable = new LastChatMessageObservable();
        fileViewObservable = new FileViewObservable();
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

    public void registerDataChange(Observer observer) {
        chatMessageObservable.addObserver(observer);
        friendViewObservable.addObserver(observer);
        lastChatMessageObservable.addObserver(observer);
        fileViewObservable.addObserver(observer);
    }

    public void unRegisterDataChange(Observer observer) {
        chatMessageObservable.deleteObserver(observer);
        friendViewObservable.deleteObserver(observer);
        lastChatMessageObservable.deleteObserver(observer);
        fileViewObservable.deleteObserver(observer);
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

    public FileViewObservable getFileViewObservable() {
        return fileViewObservable;
    }

    public void setFileViewObservable(FileViewObservable fileViewObservable) {
        this.fileViewObservable = fileViewObservable;
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

    public User who() {
        return me;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }


    public String getRingUri() {
        return ringUri;
    }

    public boolean isPhoneVibrate() {
        return phoneVibrate;
    }

    public boolean isSilence() {
        return isSilence;
    }

    public int getSlipWindowCount() {
        return slipWindowCount;
    }

    public void setSlipWindowCount(int slipWindowCount) {
        this.slipWindowCount = slipWindowCount;
    }

    public boolean isShareFile() {
        return isShareFile;
    }

    public void setShareFile(boolean shareFile) {
        isShareFile = shareFile;
    }

    public void readConfig(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setLogin(preferences.getBoolean(AppConfigure.IS_LOGIN, false));

        if (me == null) {
            me = new User();
        }
        me.setId(preferences.getInt(AppConfigure.KEY_USER_ID, 0));
        me.setName(preferences.getString(AppConfigure.KEY_USER_NAME, ""));
        me.setPassword(preferences.getString(AppConfigure.KEY_PASSWORD, ""));
        me.setAlias(preferences.getString(AppConfigure.KEY_USER_ALIAS, ""));
        me.setVerification(preferences.getString(AppConfigure.KEY_VERIFICATION, ""));
        me.setImgName(preferences.getString(AppConfigure.KEY_USER_IMG, ""));
        me.setDeviceId(preferences.getString(AppConfigure.KEY_DEVICE_ID, Tools.getMobileDeviceId()));

        setLogin(preferences.getBoolean(AppConfigure.IS_LOGIN, false));
        String ip = preferences.getString(AppConfigure.KEY_PHONE_SERVER_IP, AppConfigure.DEFAULT_PHONE_SERVER_IP);
        deviceId = preferences.getString(AppConfigure.KEY_DEVICE_ID, Tools.getMobileDeviceId());

        if (!Tools.isEmpty(ip) && !Tools.isEquals(ip, serverIp)) {
            serverIp = ip;
            SendMessageBroadcast.getInstance().connectServer(serverIp, "" + getServerPort());
        }

        ringUri = preferences.getString(AppConfigure.KEY_PHONE_RING, "");
        phoneVibrate = preferences.getBoolean(AppConfigure.KEY_PHONE_VIBRATE, false);
        isSilence = preferences.getBoolean(AppConfigure.KEY_PHONE_SILENCE, false);

        isShareFile = preferences.getBoolean(AppConfigure.KEY_FILE_SHARE, true);
        slipWindowCount = Integer.valueOf(preferences.getString(AppConfigure.KEY_SLIP_WINDOW_COUNT, "5"));
        saveConfig(context);
    }

    public void saveConfig(Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        if (me != null) {
            editor.putInt(AppConfigure.KEY_USER_ID, me.getid());
            editor.putString(AppConfigure.KEY_USER_NAME, me.getName());
            editor.putString(AppConfigure.KEY_USER_ALIAS, me.getAlias());
            editor.putString(AppConfigure.KEY_PASSWORD, me.getPassword());
            editor.putString(AppConfigure.KEY_VERIFICATION, me.getVerification());
            editor.putString(AppConfigure.KEY_USER_IMG, me.getImgName());
            editor.putString(AppConfigure.KEY_DEVICE_ID, me.getDeviceId());
        }
        editor.putBoolean(AppConfigure.KEY_IS_LOGIN, isLogin());
        editor.putString(AppConfigure.KEY_PHONE_SERVER_IP, serverIp);
        editor.putBoolean(AppConfigure.KEY_FILE_SHARE, isShareFile);
        editor.putString(AppConfigure.KEY_SLIP_WINDOW_COUNT, String.valueOf(slipWindowCount));
        editor.commit();
    }
}
