package com.baige.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import android.support.v4.content.PermissionChecker;
import android.util.Base64;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by baige on 2017/12/27.
 */

public class Tools {

    public final static String DEFAULT_ENCODE = "UTF-8";

    public static final long SIZE_KB = 1024L;
    public static final long SIZE_MB = 1024 * 1024L;
    public static final long SIZE_GB = 1024L * 1024L * 1024L;

    public static final long TIME_SIZE_SECOND = 1000;
    public static final long TIME_SIZE_MIN =  60 * 1000;
    public static final long TIME_SIZE_HOUR = 60 * 60 * 1000;
    public static final long TIME_SIZE_DAY = 24 * TIME_SIZE_HOUR;

    public static String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String ramdom() {
        int number = (int) (Math.random() * 900 + 100);
        return System.currentTimeMillis() + "_" + number;
    }

    public static String toHex(byte[] bytes) {

        final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static byte[] toByte(long data) {
        byte[] buf = new byte[Long.BYTES];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) ((data >> (i * 8)) & 0xff);
        }
        return buf;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static byte[] toByte(int data) {
        byte[] buf = new byte[Integer.BYTES];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) ((data >> (i * 8)) & 0xff);
        }
        return buf;
    }

    public static long toLong(byte buf[]) {
        long data = 0x00;
        for (int i = buf.length - 1; i >= 0; i--) {
            data <<= 8;
            data |= (buf[i] & 0xff);
        }
        return data;
    }

    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    public static boolean isEquals(Object a, Object b){
        if(a == null || b == null){
            return false; //注意 都为null时还是不相等
        }
        return a.equals(b);
    }

    public static String formatTime(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date(time));
    }

    public static String getSizeSting(long size) {

        if (size < SIZE_KB) {
            return size + "B";
        }

        if (size < SIZE_MB) {
            return Math.round(size * 100.0 / SIZE_KB) / 100.0 + "KB";
        }

        if (size < SIZE_GB) {
            return Math.round(size * 100.0 / SIZE_MB) / 100.0 + "MB";
        }

        return Math.round(size * 100.0 / SIZE_GB) / 100.0 + "G";

    }

    public static String getDurationToString(long time) {
        String duration = null;

        if (time < TIME_SIZE_MIN) {
            return Math.round(time * 100.0 / TIME_SIZE_SECOND) / 100 + "秒";
        }

        if (time < TIME_SIZE_HOUR) {
            duration = time / TIME_SIZE_MIN + "分 " + time % TIME_SIZE_MIN / TIME_SIZE_SECOND + "秒";
            return duration;
        }
        duration = time / TIME_SIZE_HOUR + "小时 " + time % TIME_SIZE_HOUR + "分 " + time % TIME_SIZE_MIN / TIME_SIZE_SECOND + "秒";
        return duration;
    }

    public static boolean isSameDay(long time1, long time2) {
       return isSameDay(new Date(time1), new Date(time2));
    }
    public static boolean isSameDay(Date date1, Date date2) {
        if(date1 != null && date2 != null) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);
            return isSameDay(cal1, cal2);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if(cal1 != null && cal2 != null) {
            return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            throw new IllegalArgumentException("The date must not be null");
        }
    }

    //TODO 未完善
    public static String getSuitableTimeFormat(long time){
        long d = System.currentTimeMillis() - time;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
        if(isSameDay(time, System.currentTimeMillis())){
            return dateFormat.format(new Date(time));
        }else if(d <= 0){
            return dateFormat.format(new Date(time));
        }else if(d < TIME_SIZE_DAY){
            return "昨天 "+dateFormat.format(new Date(time));
        }else {
            dateFormat = new SimpleDateFormat("MM-dd", Locale.CHINA);
            return dateFormat.format(new Date(time));
        }
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isAvailable();
        }
        return false;
    }


    // 打印所有的 intent extra 数据
    public static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append("\nkey:" + key + ", value:" + bundle.get(key));
        }
        return sb.toString();
    }

    public static byte[] toByteArray(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();

        parcelable.writeToParcel(parcel, 0);

        byte[] result = parcel.marshall();

        parcel.recycle();

        return (result);
    }

    /* Public Methods */
    public static byte[] stringToData(String string, String charsetName) {
        if (string != null) {
            try {
                return string.getBytes(charsetName);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String dataToString(byte[] data, String charsetName) {
        if (data != null) {
            try {
                return new String(data, charsetName);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getServerDeviceId() {
        byte[] buf = toByte(System.currentTimeMillis());
        String timeString = Base64.encodeToString(buf, Base64.NO_PADDING);//会多出一个换行符
        return "0"+timeString.substring(0, timeString.length() - 1)+String.format("%03d", Integer.valueOf((int) (Math.random()*1000)));
    }


    public static String getMobileDeviceId() {
        byte[] buf = toByte(System.currentTimeMillis());
        String timeString = Base64.encodeToString(buf, Base64.NO_PADDING);
        return "1"+timeString.substring(0, timeString.length() - 1)+String.format("%03d", Integer.valueOf((int) (Math.random()*1000)));
    }



    public static <T> T toParcelable(byte[] bytes,
                                     Parcelable.Creator<T> creator) {
        if (bytes == null) {
            return null;
        }
        Parcel parcel = Parcel.obtain();

        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);

        T result = creator.createFromParcel(parcel);

        parcel.recycle();

        return (result);
    }

    /**
     * 判断SD卡是否存在，并且是否具有读写权限
     *
     * @return
     */
    public static boolean checkPermissionWriteExternalStorage(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) &&
                PermissionChecker.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

}
