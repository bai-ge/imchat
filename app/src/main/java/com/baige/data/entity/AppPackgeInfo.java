package com.baige.data.entity;

import android.graphics.drawable.Drawable;

public class AppPackgeInfo {
    private Drawable icon;
    private String appName;
    private String version;
    private String packageName;

    public AppPackgeInfo(Drawable icon, String appName, String version, String packageName) {
        this.icon = icon;
        this.appName = appName;
        this.version = version;
        this.packageName = packageName;
    }

    public AppPackgeInfo() {
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return appName + version;
    }

}
