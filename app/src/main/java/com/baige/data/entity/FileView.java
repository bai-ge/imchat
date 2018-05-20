package com.baige.data.entity;

import android.util.Log;

import com.baige.util.JsonTools;

import org.json.JSONObject;

/**
 * Created by baige on 2018/5/21.
 */

public class FileView {
    private int id;
    private int userId;
    private String fileName;
    private String filePath;
    private int fileType;
    private long fileSize;
    private String fileDescribe;
    private long uploadTime;
    private int downloadCount;
    private String remark;

    String userName;
    boolean isCheck;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileDescribe() {
        return fileDescribe;
    }

    public void setFileDescribe(String fileDescribe) {
        this.fileDescribe = fileDescribe;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }

    public static FileView createByJson(JSONObject fileJson) {
        Log.e("FileView", "开始");
        FileView fileView = (FileView) JsonTools.toJavaBean(FileView.class, fileJson);
        Log.e("FileView", fileView.toString());
        return fileView;
    }

    @Override
    public String toString() {
        return "FileView{" +
                "id=" + id +
                ", userId=" + userId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType=" + fileType +
                ", fileSize=" + fileSize +
                ", fileDescribe='" + fileDescribe + '\'' +
                ", uploadTime=" + uploadTime +
                ", downloadCount=" + downloadCount +
                ", remark='" + remark + '\'' +
                ", userName='" + userName + '\'' +
                ", isCheck=" + isCheck +
                '}';
    }
}
