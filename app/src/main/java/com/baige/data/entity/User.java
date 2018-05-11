package com.baige.data.entity;

import android.util.Log;

import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONObject;

/**
 * Created by baige on 2018/5/6.
 */

public class User extends BaseEntity{

    private int id; //数据库中的ID

    private String name; // 手机号码或邮箱

    private String alias; //别名

    private String deviceId; //设备Id

    private String password; // 密码

    private String verification; //验证字段

    private String imgName; //头像，仅有文件名

    private Long loginTime;

    private String loginIp;

    private Long registerTime;

    public User(){};

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public int getid() {
        return id;
    }

    public void setid(int uid) {
        this.id = id;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerification() {
        return verification;
    }

    public void setVerification(String verification) {
        this.verification = verification;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public void setLoginIp(String loginIp) {
        this.loginIp = loginIp;
    }

    public Long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Long registerTime) {
        this.registerTime = registerTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null){
            return false;
        }
        if(o instanceof String){
            return Tools.isEquals(o, this.getName());
        }else if(o instanceof User){
            User user = (User) o;
            if(user.getid() == getid()){
                return true;
            }else if(Tools.isEquals(user.getName(), getName())){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", alias='" + alias + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", password='" + password + '\'' +
                ", verification='" + verification + '\'' +
                ", imgName='" + imgName + '\'' +
                '}';
    }

    public static User createByJson(JSONObject userJson) {
        Log.d("user", userJson.toString());
        User user = (User) JsonTools.toJavaBean(User.class, userJson );
        Log.d("user" , ""+ user);
        return user;
    }
}
