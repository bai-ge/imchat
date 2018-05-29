package com.baige.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.baige.common.State;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONObject;

public class FriendView implements Parcelable{
    private int id; //Friend 表中的ID
    private int userId; //用户ID
    private int friendId;
    private String name; //用户名
    private String alias; //用户别名
    private String friendAlias;//好友设置的用户备注
    private long relateTime;
    private int state;
    private int readState; //自己是否已读
    private String remake;
    private String imgName;  //好友的头像名称
    private String deviceId; //好友的设备ID

    public FriendView(){};

    private FriendView(Parcel in) {
        id = in.readInt();
        userId = in.readInt();
        name = in.readString();
        friendId = in.readInt();
        alias = in.readString();
        friendAlias = in.readString();
//        in.readList(getMedicineList(), Medicine.class.getClassLoader());
        relateTime = in.readLong();
        state = in.readInt();
        readState = in.readInt();
        remake = in.readString();
        imgName = in.readString();
        deviceId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(userId);
        dest.writeString(name);
        dest.writeInt(friendId);
        dest.writeString(alias);
        dest.writeString(friendAlias);
        dest.writeLong(relateTime);
        dest.writeInt(state);
        dest.writeInt(readState);
        dest.writeString(remake);
        dest.writeString(imgName);
        dest.writeString(deviceId);

    }
    @Override
    public int describeContents() {
        return 0;
    }



    public static final Creator<FriendView> CREATOR = new Creator<FriendView>() {

        @Override
        public FriendView createFromParcel(Parcel in) {
            return new FriendView(in);
        }

        @Override
        public FriendView[] newArray(int size) {

            return new FriendView[size];
        }
    };

    public FriendView(String friendAlias, String name, String alias){
        this.friendAlias = friendAlias;
        this.name = name;
        this.alias = alias;
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

    public boolean isFriend(){
        return state == State.RELATETION_FRIEND;
    }


    public String getName() {
        return name;
    }

    public void setName(String friendName) {
        this.name = friendName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int uid) {
        this.userId = uid;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFriendAlias() {
        return friendAlias;
    }

    public void setFriendAlias(String friendAlias) {
        this.friendAlias = friendAlias;
    }

    public long getRelateTime() {
        return relateTime;
    }

    public void setRelateTime(long relateTime) {
        this.relateTime = relateTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getReadState() {
        return readState;
    }

    public void setReadState(int readState) {
        this.readState = readState;
    }

    public String getRemake() {
        return remake;
    }

    public void setRemake(String remake) {
        this.remake = remake;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String friendImgName) {
        this.imgName = friendImgName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String friendDeviceId) {
        this.deviceId = friendDeviceId;
    }

    public static FriendView createByJson(JSONObject friendJson) {
        return (FriendView) JsonTools.toJavaBean(FriendView.class, friendJson);
    }

    @Override
    public String toString() {
        return "FriendView{" +
                "id=" + id +
                ", uid=" + userId +
                ", friendName='" + name + '\'' +
                ", friendId=" + friendId +
                ", alias='" + alias + '\'' +
                ", friendAlias='" + friendAlias + '\'' +
                ", relateTime=" + relateTime +
                ", state=" + state +
                ", readState=" + readState +
                ", remake='" + remake + '\'' +
                ", friendImgName='" + imgName + '\'' +
                ", friendDeviceId='" + deviceId + '\'' +
                '}';
    }
}
