package com.baige.pushcore;

import android.content.Intent;
import android.util.Log;

import com.baige.BaseApplication;
import com.baige.util.Tools;


/**
 * Created by baige on 2018/3/27.
 */

public class SendMessageBroadcast {
    private final static String TAG = SendMessageBroadcast.class.getCanonicalName();

    private static SendMessageBroadcast INSTANCE = null;

    private final static String PACKAGE_NAME = "com.carefor.mainui";

    /**
     *
     */
    public final static String ACTION_SEND_MSG = PACKAGE_NAME + ".SEND_MSG";
    public final static String ACTION_CONNECT_SERVER = PACKAGE_NAME + ".CONNECT_SERVER";
    public final static String ACTION_DISCONNECT_SERVER = PACKAGE_NAME + ".DISCONNECT_SERVER";

    public final static String ACTION_RECEIVE_MSG = PACKAGE_NAME+".RECEIVE_MSG";

    public final static String ACTION_CONNECT_STATE = PACKAGE_NAME+".CONNECT_STATE";

    public final static String ACTION_SEND_MSG_FIAL = PACKAGE_NAME+".SEND_MSG_FIAL";


    public final static String KEY_SEND_MSG ="send_msg";
    public final static String KEY_RECEIVE_MSG = "receive_msg";
    public final static String KEY_SERVER_IP = "server_ip";
    public final static String KEY_SERVER_PORT = "server_port";
    public final static String KEY_CONNECT_STATE = "connect_state";

    private SendMessageBroadcast() {

    }


    public static SendMessageBroadcast getInstance() {
        if (INSTANCE == null) {
            synchronized (SendMessageBroadcast.class) {
                if (INSTANCE == null) {
                    INSTANCE = new SendMessageBroadcast();
                }
            }
        }
        return INSTANCE;
    }

    public void sendMessage(String msg) {

        if (!Tools.isEmpty(msg)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_SEND_MSG);
            intent.putExtra(KEY_SEND_MSG, msg);
            BaseApplication.getAppContext().sendBroadcast(intent);
            Log.d(TAG, "发送广播："+Tools.printBundle(intent.getExtras()));
        }
    }

    public void connectServer(String ip, String port) {

        if (!Tools.isEmpty(ip) && !Tools.isEmpty(port)) {
            Intent intent = new Intent();
            intent.setAction(ACTION_CONNECT_SERVER);
            intent.putExtra(KEY_SERVER_IP, ip);
            intent.putExtra(KEY_SERVER_PORT, port);
            BaseApplication.getAppContext().sendBroadcast(intent);
            Log.d(TAG, "发送广播："+Tools.printBundle(intent.getExtras()));
        }
    }

    public void disconnectServer() {
        Intent intent = new Intent();
        intent.setAction(ACTION_DISCONNECT_SERVER);
        BaseApplication.getAppContext().sendBroadcast(intent);
        Log.d(TAG, "发送广播："+Tools.printBundle(intent.getExtras()));

    }


}
