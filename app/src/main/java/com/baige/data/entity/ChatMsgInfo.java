package com.baige.data.entity;

import android.support.annotation.NonNull;

import com.baige.common.Parm;
import com.baige.util.JsonTools;

import org.json.JSONObject;

/**
 * Created by baige on 2018/5/8.
 */

public class ChatMsgInfo implements Comparable<ChatMsgInfo> {
    private int id;
    private int senderId;
    private int receiveId;
    private long sendTime;
    private String context;
    private Integer contextType;
    private Integer contextState;
    private String remark;

    private int showType; //发送、接收、通知
    private int sendState; //发送中、发送成功、发送失败
    private String userImgName;
    private String userName;



    public ChatMsgInfo(){
        sendTime = System.currentTimeMillis();
        contextType = Parm.MSG_TYPE_TEXT;
        showType = Parm.MSG_IS_SEND;
        sendState = Parm.MSG_IS_SEND_SUCESS;
    }
    public ChatMsgInfo(String msg){
        this();
        this.context = msg;
    }

    public ChatMsgInfo(String name, String msg, int showType){
        this();
        this.userName = name;
        this.context = msg;
        this.showType = showType;
    }

    @Override
    public int compareTo(@NonNull ChatMsgInfo o) {
        if(o != null){
            return (int) (getSendTime() - o.getSendTime());
        }
        return 1;
    }

    public boolean isText(){
        return Parm.MSG_TYPE_TEXT == contextType;
    }

    public boolean isFile(){
        return Parm.MSG_TYPE_FILE == contextType;
    }
    public boolean isImg(){
        return Parm.MSG_TYPE_IMG == contextType;
    }

    public boolean isSend(){
        return Parm.MSG_IS_SEND == showType;
    }
    public boolean isReceive(){
        return Parm.MSG_IS_RECEIVE == showType;
    }
    public boolean isInfrom(){
        return Parm.MSG_IS_RECEIVE == showType;
    }

    public boolean isSending(){
        return sendState == Parm.MSG_IS_SENDING;
    }
    public boolean isWarning(){
        return sendState == Parm.MSG_IS_SEND_FAIL;
    }
    public boolean isSendSuccess(){
        return sendState == Parm.MSG_IS_SEND_SUCESS;
    }

    /*get and set*/
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiveId() {
        return receiveId;
    }

    public void setReceiveId(int receiveId) {
        this.receiveId = receiveId;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public Integer getContextType() {
        return contextType;
    }

    public void setContextType(Integer contextType) {
        this.contextType = contextType;
    }

    public Integer getContextState() {
        return contextState;
    }

    public void setContextState(Integer contextState) {
        this.contextState = contextState;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserImgName() {
        return userImgName;
    }

    public void setUserImgName(String userImgName) {
        this.userImgName = userImgName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getShowType() {
        return showType;
    }

    public void setShowType(int showType) {
        this.showType = showType;
    }

    public int getSendState() {
        return sendState;
    }

    public void setSendState(int sendState) {
        this.sendState = sendState;
    }

    @Override
    public String toString() {
        return "ChatMsgInfo{" +
                "id=" + id +
                ", senderId=" + senderId +
                ", receiveId=" + receiveId +
                ", sendTime=" + sendTime +
                ", context='" + context + '\'' +
                ", contextType=" + contextType +
                ", contextState=" + contextState +
                ", remark='" + remark + '\'' +
                ", showType=" + showType +
                ", sendState=" + sendState +
                ", userImgName='" + userImgName + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    public static ChatMsgInfo createByJson(JSONObject chatJson) {
        ChatMsgInfo chatmsg = (ChatMsgInfo) JsonTools.toJavaBean(ChatMsgInfo.class, chatJson);
        if(chatmsg != null){
        //TODO 初始化

        }
        return chatmsg;
    }
}
