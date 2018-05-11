package com.baige.connect.msg;

/**
 * Created by baige on 2018/3/24.
 */

public class Parm {
    /*code 一般作为服务器反馈的数字代码， */
    public final static String CODE = "code";
    public final static String FROM = "from";
    public final static String TO = "to";
    public final static String DATA = "data";
    public final static String DATA_TYPE = "data_type";

    public final static String DEVICE_ID = "device_id";
    public final static String CALLBACK = "callback";

    public final static String LOCAL_IP = "local_ip";
    public final static String REMOTE_IP = "remote_ip";
    public final static String LOCAL_PORT = "local_port";
    public final static String ACCEPT_PORT = "accept_port";
    public final static String REMOTE_PORT = "remote_port";
    public final static String LOCAL_UDP_PORT = "local_udp_port";
    public final static String REMOTE_UDP_PORT = "remote_udp_port";

    private final static int MSG_TYPE = 0x0010;
    public final static int TYPE_LOGIN = MSG_TYPE + 1;
    public final static int TYPE_LOGOUT = MSG_TYPE + 2;

    public final static int CODE_SUCCESS = 200;
    public final static int CODE_FAIL = 500;
    public final static int CODE_UNKNOWN = 999;
    public final static int CODE_NOT_FIND = 404;
    public final static int CODE_INVALID = 1005;
    public final static int CODE_TIMEOUT = 1004;
}
