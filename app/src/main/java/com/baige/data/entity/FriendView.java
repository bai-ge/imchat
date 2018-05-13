package com.baige.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.baige.util.JsonTools;

import org.json.JSONObject;

public class FriendView implements Parcelable{
    private int id; //Friend 表中的ID
    private int uid; //用户ID
    private String friendName; //用户名
    private int friendId;
    private String alias; //用户别名
    private String friendAlias;//好友设置的用户备注
    private long relateTime;
    private int state;
    private int readState; //自己是否已读
    private String remake;
    private String friendImgName;
    private String friendDeviceId;

    public FriendView(){};

    private FriendView(Parcel in) {
        id = in.readInt();
        uid = in.readInt();
        friendName = in.readString();
        friendId = in.readInt();
        alias = in.readString();
        friendAlias = in.readString();
//        in.readList(getMedicineList(), Medicine.class.getClassLoader());
        relateTime = in.readLong();
        state = in.readInt();
        readState = in.readInt();
        remake = in.readString();
        friendImgName = in.readString();
        friendDeviceId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(uid);
        dest.writeString(friendName);
        dest.writeInt(friendId);
        dest.writeString(alias);
        dest.writeString(friendAlias);
        dest.writeLong(relateTime);
        dest.writeInt(state);
        dest.writeInt(readState);
        dest.writeString(remake);
        dest.writeString(friendImgName);
        dest.writeString(friendDeviceId);

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
        this.friendName = name;
        this.alias = alias;
    }
    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
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

    public String getFriendImgName() {
        return friendImgName;
    }

    public void setFriendImgName(String friendImgName) {
        this.friendImgName = friendImgName;
    }

    public String getFriendDeviceId() {
        return friendDeviceId;
    }

    public void setFriendDeviceId(String friendDeviceId) {
        this.friendDeviceId = friendDeviceId;
    }

    public static FriendView createByJson(JSONObject friendJson) {
        return (FriendView) JsonTools.toJavaBean(FriendView.class, friendJson);
    }

    @Override
    public String toString() {
        return "FriendView{" +
                "id=" + id +
                ", uid=" + uid +
                ", friendName='" + friendName + '\'' +
                ", friendId=" + friendId +
                ", alias='" + alias + '\'' +
                ", friendAlias='" + friendAlias + '\'' +
                ", relateTime=" + relateTime +
                ", state=" + state +
                ", readState=" + readState +
                ", remake='" + remake + '\'' +
                ", friendImgName='" + friendImgName + '\'' +
                ", friendDeviceId='" + friendDeviceId + '\'' +
                '}';
    }
}
