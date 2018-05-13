package com.baige.common;

public class State {
    public static final int MSG_STATE_UNREAD = 1000;
    public static final int MSG_STATE_READED = 1001;

    public static final String BEAN_CHATMSG_SENDER_ID = "senderId";
    public static final String BEAN_CHATMSG_RECEIVE_ID = "receiveId";
    public static final String BEAN_CHATMSG_SEND_TIME = "sendTime";
    public static final String BEAN_CHATMSG_RECEIVE_TIME = "receiveTime";
    public static final String BEAN_CHATMSG_CONTEXT = "context";
    public static final String BEAN_CHATMSG_CONTEXT_TYPE = "contextType";
    public static final String BEAN_CHATMSG_CONTEXT_STATE = "contextState";

    public static final int CHATMSG_METHOD_FIND_ALL = 1100;
    public static final int CHATMSG_METHOD_FIND_HISTORY_MSG = 1101;
    public static final int CHATMSG_METHOD_FIND_UNREAD_BY_ID = 1102;
    public static final int CHATMSG_METHOD_FIND_UNREAD_BY_NAME = 1103;
    public static final int CHATMSG_METHOD_FIND_BY_SEND_ID = 1104;
    public static final int CHATMSG_METHOD_FIND_BY_SEND_NAME = 1105;

    public static final int RELATETION_ADD = 2;         // 添加
    public static final int RELATETION_REJECT = 4;      // 拒绝
    public static final int RELATETION_AGREE = 8;       // 同意
    public static final int RELATETION_DEFRIEND = 16;   // 拉黑 | 0 | 1（0表示用户，1表示好友）
    public static final int RELATETION_DELETE = 32;     // 删除

    public static final int READ_STATE = 2000;
    public static final int UNREAD_STATE = 2001;



}
