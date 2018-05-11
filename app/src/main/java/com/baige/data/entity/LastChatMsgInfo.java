package com.baige.data.entity;

/**
 * Created by baige on 2018/5/7.
 */

public class LastChatMsgInfo {

    private int uid; //数据库中的ID

    private String name; // 手机号码或邮箱

    private String alias; //别名

    private String lastMessage;

    private long lastTime;

    private int msgCount;

    public LastChatMsgInfo() {}

    public LastChatMsgInfo(String name, String alias, String lastMessage, long lastTime, int msgCount) {
        this.name = name;
        this.alias = alias;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
        this.msgCount = msgCount;
    }

    public LastChatMsgInfo(int uid, String name, String alias, String lastMessage, long lastTime, int msgCount) {
        this.uid = uid;
        this.name = name;
        this.alias = alias;
        this.lastMessage = lastMessage;
        this.lastTime = lastTime;
        this.msgCount = msgCount;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getMsgCount() {
        return msgCount;
    }

    public void setMsgCount(int msgCount) {
        this.msgCount = msgCount;
    }
}
