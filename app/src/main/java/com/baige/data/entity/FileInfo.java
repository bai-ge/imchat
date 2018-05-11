package com.baige.data.entity;

import android.support.annotation.NonNull;

import com.baige.data.source.cache.CacheRepository;
import com.baige.exception.IllegalFilePath;


import java.util.Locale;

/**
 * 简单的文件描述
 * 不能通过文件路径path来判断文件类型，因为可能不在本地文件系统中创建
 *
 * @author Lennon
 * @time 2014-08-03
 */
public class FileInfo implements Comparable<FileInfo> {

    private static final long serialVersionUID = 6779119731190741814L;
    private String path = null;
    private String name = null;
    private String appName = null;
    private long createTime = 0;
    private boolean isChecked = false;
    private boolean isFavorite = false;
    private boolean isNew = false;
    private long fileSize = 0; // 字节
    private int fileType = FileType.TYPE_UNKNOWN;
    private int categoryType = FileType.CATEGORY_UNKNOWN;

    /**如果非文件夹的可以这样创建实例，文件夹会出现严重的逻辑问题
     * @param path
     */
    public FileInfo(String path) {
        this.path = path;
        analysisFileInfo();
    }

    public FileInfo(String path, int fileType) {
        this.path = path;
        this.fileType = fileType;
        analysisFileInfo();
    }

    public FileInfo(String path, long createTime, long size) {
        this.path = path;
        this.createTime = createTime;
        this.fileSize = size;
        analysisFileInfo();
    }

    public FileInfo(String path, long size) {
        this.path = path;
        this.fileSize = size;
        analysisFileInfo();
    }

    public void analysisFileInfo() {
        initName();
        ensureType();
    }

    // 确定当前文件名字
    private void initName() {
        if (path.equals("/")) {
            name = "/";
        } else if (path.endsWith(CacheRepository.ExternalStoragePath)) {
            name = CacheRepository.ExternalStoragePath;
        }

        int index = 0;
        index = path.lastIndexOf("/");
        if (index == -1 || !path.startsWith("/")) {
            try {
                throw new IllegalFilePath("非法的标准路径：" + path);
            } catch (IllegalFilePath e) {
                e.printStackTrace();
            }
        }
        name = path.substring(index + 1);
    }

    private void ensureType() {
        if (fileType == -1) {
            return;
        }
        //之所以不用File 来初始化文件类型，是因为需要File 来判断，只能在本地被执行

        fileType = FileType.getFileType(path);
        categoryType = FileType.getFileCategory(path);
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取父路径
     *
     * @return
     */
    public String getParentPath() {

        if (path.equals("/")) {
            return "/";
        } else if (path.endsWith(CacheRepository.ExternalStoragePath)) {
            return CacheRepository.ExternalStoragePath;
        }

        int index = 0;
        index = path.lastIndexOf("/");
        if (index == -1 || !path.startsWith("/")) {
            try {
                throw new IllegalFilePath("非法的标准路径：" + path);
            } catch (IllegalFilePath e) {
                e.printStackTrace();
            }
        }
        return path.substring(0, index);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof FileInfo)) {
            return false;
        }

        FileInfo info = (FileInfo) obj;
        if (path.equals(info.path)) {
            return true;
        }
        return false;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(int categoryType) {
        this.categoryType = categoryType;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    @Override
    public String toString() {
        return "SimpleFileInfo [path=" + path + ", name=" + name + ", isChecked=" + isChecked + ", isFavorite=" + isFavorite + ", isNew=" + isNew
                + ", fileSize=" + fileSize + ", fileType=" + fileType + ", categoryType=" + categoryType + "]";
    }

    @Override
    public int compareTo(@NonNull FileInfo fileInfo) {
        if (fileType < fileInfo.fileType) {
            return -1;
        } else if (fileType > fileInfo.fileType) {
            return 1;
        }
        return name.toLowerCase(Locale.CHINA).compareTo(fileInfo.name.toLowerCase(Locale.CHINA));
    }
}
