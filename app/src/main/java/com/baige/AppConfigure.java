package com.baige;

import android.os.Environment;

import java.io.File;

/**
 * Created by baige on 2018/3/24.
 */

public class AppConfigure {

    public final static String APP_HOME = Environment.getExternalStorageDirectory()+ File.separator + "imchat";
    public final static String HEAD_IMG_PATH = APP_HOME + File.separator + "head";
    public final static String LOG_PATH = APP_HOME + File.separator + "log";


    public final static String KEY_IS_LOGIN = "is_login";
    public final static String KEY_USER_ID = "user_id";
    public final static String KEY_USER_NAME = "user_name";
    public final static String KEY_USER_ALIAS = "user_alias";
    public final static String KEY_PASSWORD = "user_password";
    public final static String KEY_VERIFICATION = "user_verification";

    public final static String KEY_SERVER_IP = "server_ip";
    public final static String KEY_SERVER_PORT = "server_port";
    public final static String KEY_DEVICE_ID = "device_id";

    public final static String IS_LOGIN = "is_login";

}
