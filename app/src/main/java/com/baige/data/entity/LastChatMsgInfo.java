package com.baige.data.entity;

import com.baige.util.Tools;

/**
 * Created by baige on 2018/5/7.
 */

public class LastChatMsgInfo {

    private int uid; //数据库中的ID

    private String name; // 手机号码或邮箱

    private String alias; //别名

    private String friendAlias;//朋友备注

    private String imagName;

    private String lastMessage;

    private int msgType;

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

    public String getSuitableName(){
        String name = getFriendAlias();
        if(Tools.isEmpty(name)){
            name = getAlias();
            if(Tools.isEmpty(name)){
                name = getName();
            }
        }
        return name;
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

    public String getFriendAlias() {
        return friendAlias;
    }

    public void setFriendAlias(String friendAlias) {
        this.friendAlias = friendAlias;
    }

    public String getImagName() {
        return imagName;
    }

    public void setImagName(String imagName) {
        this.imagName = imagName;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    @Override
    public String toString() {
        return "LastChatMsgInfo{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", friendAlias='" + friendAlias + '\'' +
                ", imagName='" + imagName + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", msgType=" + msgType +
                ", lastTime=" + lastTime +
                ", msgCount=" + msgCount +
                '}';
    }
}
