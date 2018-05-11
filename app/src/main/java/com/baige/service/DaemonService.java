package com.baige.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;


import com.baige.broadcast.WakeReceiver;
import com.baige.connect.NetServerManager;
import com.baige.connect.SocketClientAddress;
import com.baige.util.Tools;
import com.coolerfall.daemon.Daemon;
import com.baige.AppConfigure;


/**
 * Created by Ryoko on 2018/3/18.
 */

public class DaemonService extends Service {

    public final static String DEFAULT_SERVER_IP = "125.217.52.17";

    public final static String DEFAULT_SERVER_PORT = "12056";

    DaemonServiceRepository mDaemonServiceRepository;
    /**
     * Log tag ：DaemonService
     */
    private static final String TAG = DaemonService.class.getCanonicalName();

    /**
     * 定时唤醒的时间间隔，5分钟
     */
    private final static int ALARM_INTERVAL = 5 * 60 * 1000;
    private final static int WAKE_REQUEST_CODE = 6666;

    private final static int GRAY_SERVICE_ID = -1001;




    //网络连接

    private NetServerManager mNetServerManager;

    private NetworkConnectChangedReceiver networkConnectChangedReceiver;






    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");
        Daemon.run(DaemonService.this, DaemonService.class, Daemon.INTERVAL_ONE_MINUTE);
        mDaemonServiceRepository = DaemonServiceRepository.getInstance();
        readConfig();
        saveConfig();
        checkNetwork();
        mNetServerManager = NetServerManager.getInstance();

        networkConnectChangedReceiver = new NetworkConnectChangedReceiver();
        registerReceiver();
      //  startTask();
        grayGuard();

        // Notification notification = new Notification();
        // startForeground(-1, notification);
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkConnectChangedReceiver, filter);
    }

    private void startTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(mDaemonServiceRepository.isWifiValid() || mDaemonServiceRepository.isNetworkValid()){
//                    mNetServerManager.connectToAddressByTCP(mDaemonServiceRepository.getServerAddress(), new OnConnectedListener() {
//                        @Override
//                        public void onConnected(BaseConnector connector) {
//                            Log.d(TAG, "连接服务器成功");
//                            connector.sendString();
//                        }
//
//                        @Override
//                        public void onDisconnected(BaseConnector connector) {
//                            Log.d(TAG, "连接服务器失败");
//                        }
//
//                        @Override
//                        public void onResponse(BaseConnector connector, @NonNull SocketPacket responsePacket) {
//                            Log.d(TAG, "收到服务器信息"+responsePacket);
//                        }
//                    });
                    mNetServerManager.connectTOServer(mDaemonServiceRepository.getServerAddress().getRemoteIP(), Integer.valueOf(mDaemonServiceRepository.getServerAddress().getRemotePort()) );
                }
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IPush.Stub() {
            @Override
            public int getPid() throws RemoteException {
                Log.d(TAG, Thread.currentThread().getName());
                return Process.myPid();
            }

            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveConfig();
        unregisterReceiver(networkConnectChangedReceiver);
        Log.d(TAG, "onDestroy()");
        startService(new Intent(this, DaemonService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    private void grayGuard() {
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(GRAY_SERVICE_ID, new Notification());//API < 18 ，此方法能有效隐藏Notification上的图标
        } else {
            Intent innerIntent = new Intent(this, DaemonInnerService.class);
            startService(innerIntent);
            startForeground(GRAY_SERVICE_ID, new Notification());
        }

        //发送唤醒广播来促使挂掉的UI进程重新启动起来
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent();
        alarmIntent.setAction(WakeReceiver.GRAY_WAKE_ACTION);
        PendingIntent operation = PendingIntent.getBroadcast(this, WAKE_REQUEST_CODE, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), ALARM_INTERVAL, operation);
    }

    public class NetworkConnectChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action =" + action);
            // 这个监听wifi的打开与关闭，与wifi的连接无关
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.e(TAG, "wifiState" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_DISABLED:
                        mDaemonServiceRepository.setWifiEnable(false);
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:

                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        mDaemonServiceRepository.setWifiEnable(true);
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        break;
                    default:
                        break;


                }
            }
            // 这个监听wifi的连接状态即是否连上了一个有效无线路由，当上边广播的状态是WifiManager
            // .WIFI_STATE_DISABLING，和WIFI_STATE_DISABLED的时候，根本不会接到这个广播。
            // 在上边广播接到广播是WifiManager.WIFI_STATE_ENABLED状态的同时也会接到这个广播，
            // 当然刚打开wifi肯定还没有连接到有效的无线
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    NetworkInfo.State state = networkInfo.getState();
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;// 当然，这边可以更精确的确定状态
                    Log.e(TAG, "isConnected" + isConnected);
                    if (isConnected) {
                        mDaemonServiceRepository.setWifiValid(true);
                    } else {
                        mDaemonServiceRepository.setWifiValid(false);
                    }
                }
            }
            // 这个监听网络连接的设置，包括wifi和移动数据的打开和关闭。.
            // 最好用的还是这个监听。wifi如果打开，关闭，以及连接上可用的连接都会接到监听。见log
            // 这个广播的最大弊端是比上边两个广播的反应要慢，如果只是要监听wifi，我觉得还是用上边两个配合比较合适
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                Log.i(TAG, "CONNECTIVITY_ACTION");

                NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
                if (activeNetwork != null) { // connected to the internet
                    if (activeNetwork.isConnected()) {
                        if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                            // connected to wifi
                            mDaemonServiceRepository.setWifiValid(true);
                            Log.e(TAG, "当前WiFi连接可用 ");
                        } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                            // connected to the mobile provider's data plan
                            mDaemonServiceRepository.setNetworkValid(true);
                            Log.e(TAG, "当前移动网络连接可用 ");
                        }
                        //TODO 尝试连接服务器
                       mNetServerManager.connectTOServer(mDaemonServiceRepository.getServerAddress().getRemoteIP(), Integer.valueOf(mDaemonServiceRepository.getServerAddress().getRemotePort()) );
                    } else {
                        Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                        mDaemonServiceRepository.setWifiValid(false);
                        mDaemonServiceRepository.setNetworkValid(false);
                    }


                    Log.e(TAG, "info.getTypeName()" + activeNetwork.getTypeName());
                    Log.e(TAG, "getSubtypeName()" + activeNetwork.getSubtypeName());
                    Log.e(TAG, "getState()" + activeNetwork.getState());
                    Log.e(TAG, "getDetailedState()"
                            + activeNetwork.getDetailedState().name());
                    Log.e(TAG, "getDetailedState()" + activeNetwork.getExtraInfo());
                    Log.e(TAG, "getType()" + activeNetwork.getType());
                } else {   // not connected to the internet
                    Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                    mDaemonServiceRepository.setWifiValid(false);
                    mDaemonServiceRepository.setNetworkValid(false);

                }
            }
        }
    }

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class DaemonInnerService extends Service {

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            //stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }

    public void checkNetwork(){
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.isConnected()) {
                if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                    // connected to wifi
                    mDaemonServiceRepository.setWifiEnable(true);
                    mDaemonServiceRepository.setWifiValid(true);
                    Log.i(TAG, "当前WiFi连接可用 ");
                } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                    // connected to the mobile provider's data plan
                    mDaemonServiceRepository. setNetworkValid(true);
                    Log.e(TAG, "当前移动网络连接可用 ");
                }
                //TODO 尝试连接服务器

            } else {
                Log.i(TAG, "当前没有网络连接，请确保你已经打开网络 ");
                mDaemonServiceRepository.setWifiValid(false);
                mDaemonServiceRepository.setNetworkValid(false);
            }
            Log.i(TAG, "info.getTypeName()" + activeNetwork.getTypeName());
            Log.i(TAG, "getSubtypeName()" + activeNetwork.getSubtypeName());
            Log.i(TAG, "getState()" + activeNetwork.getState());
            Log.i(TAG, "getDetailedState()" + activeNetwork.getDetailedState().name());
            Log.i(TAG, "getDetailedState()" + activeNetwork.getExtraInfo());
            Log.i(TAG, "getType()" + activeNetwork.getType());
        } else {   // not connected to the internet
            Log.e(TAG, "当前没有网络连接，请确保你已经打开网络 ");
            mDaemonServiceRepository.setWifiValid(false);
            mDaemonServiceRepository.setNetworkValid(false);
        }
    }

    public void readConfig() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String ip = preferences.getString(AppConfigure.KEY_SERVER_IP, DEFAULT_SERVER_IP);
        String port = preferences.getString(AppConfigure.KEY_SERVER_PORT, DEFAULT_SERVER_PORT);
        if(mDaemonServiceRepository.getServerAddress() == null){
            SocketClientAddress serverAddress = new SocketClientAddress(ip, port);
            mDaemonServiceRepository.setServerAddress(serverAddress);
        }else{
            mDaemonServiceRepository.getServerAddress().setRemoteIP(ip);
            mDaemonServiceRepository.getServerAddress().setRemotePort(port);
        }
        String deviceId = preferences.getString(AppConfigure.KEY_DEVICE_ID, Tools.getMobileDeviceId());
        mDaemonServiceRepository.setDeviceId(deviceId);
    }

    public void saveConfig() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        if (mDaemonServiceRepository.getServerAddress() != null) {
            editor.putString(AppConfigure.KEY_SERVER_IP, mDaemonServiceRepository.getServerAddress().getRemoteIP());
            editor.putString(AppConfigure.KEY_SERVER_PORT, mDaemonServiceRepository.getServerAddress().getRemotePort());
        }
        editor.putString(AppConfigure.KEY_DEVICE_ID, mDaemonServiceRepository.getDeviceId());
        editor.commit();
    }


}
