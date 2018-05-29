package com.baige.common;

public class State {

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

    public static final int RELATETION_STRANGE = 0;     //陌生人
    public static final int RELATETION_WAITING = 1;     //等待对方回应
    public static final int RELATETION_BEADDED = 2;     //被添加
    public static final int RELATETION_FRIEND = 3;      //好友关系
    public static final int RELATETION_DEFRIEND = 4;    //主动拉黑
    public static final int RELATETION_BEDEFRIEND = 5;  //被拉黑

    public static final int READ_STATE = 2000;
    public static final int UNREAD_STATE = 2001;

    public static final int REMOTE = 2100;      //远端
    public static final int LOCAL = 2101;       //本地

}
