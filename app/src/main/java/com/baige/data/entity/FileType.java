package com.baige.data.entity;


import android.util.Log;

import com.baige.imchat.R;
import com.baige.util.Tools;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileType {

    private final static String TAG = FileType.class.getSimpleName();
    //public static final String[] fileExtensions = {"3gp", "aac", "amr", "apk", "avi", "dng", "doc", "docx", "exe", "flac", "gif", "gz", "jpeg", "jpg", "m4a", "mkv", "mov", "mp3", "mp4", "pdf", "pic", "png", "rar", "txt", "wav", "wma", "zip"};
    public static final int TYPE_FOLDER = -1;
    public static final int TYPE_UNKNOWN = 100;

    public static final int TYPE_3GP = 1;
    public static final int TYPE_AAC = 2;
    public static final int TYPE_AMR = 3;
    public static final int TYPE_APK = 4;
    public static final int TYPE_AVI = 5;
    public static final int TYPE_DNG = 6;
    public static final int TYPE_DOC = 7;
    public static final int TYPE_DOCX = 8;
    public static final int TYPE_EXE = 9;
    public static final int TYPE_FLAC = 10;
    public static final int TYPE_GIF = 11;
    public static final int TYPE_GZ = 12;
    public static final int TYPE_JPEG = 13;
    public static final int TYPE_JPG = 14;
    public static final int TYPE_M4A = 15;
    public static final int TYPE_MKV = 16;
    public static final int TYPE_MOV = 17;
    public static final int TYPE_MP3 = 18;
    public static final int TYPE_MP4 = 19;
    public static final int TYPE_PDF = 20;
    public static final int TYPE_PIC = 21;
    public static final int TYPE_PNG = 22;
    public static final int TYPE_RAR = 23;
    public static final int TYPE_TXT = 24;
    public static final int TYPE_WAV = 25;
    public static final int TYPE_WMA = 26;
    public static final int TYPE_ZIP = 27;
    public static final int TYPE_XLS = 28;
    public static final int TYPE_PPT = 29;
    public static final int TYPE_XML = 30;
    public static final int TYPE_HTML = 31;
    public static final int TYPE_TAR = 32;
    public static final int TYPE_RMVB = 33;
    public static final int TYPE_MUSIC = 34;
    public static final int TYPE_OPUS = 35;

    public static Map<String, Integer> mExtensionTypeMap = new LinkedHashMap<>();
    static{
        mExtensionTypeMap.put(".3gp", TYPE_3GP);
        mExtensionTypeMap.put(".aac", TYPE_AAC);
        mExtensionTypeMap.put(".amr", TYPE_AMR);
        mExtensionTypeMap.put(".apk", TYPE_APK);
        mExtensionTypeMap.put(".avi", TYPE_AVI);
        mExtensionTypeMap.put(".dng", TYPE_DNG);
        mExtensionTypeMap.put(".doc", TYPE_DOC);
        mExtensionTypeMap.put(".docx", TYPE_DOCX);
        mExtensionTypeMap.put(".exe", TYPE_EXE);
        mExtensionTypeMap.put(".flac", TYPE_FLAC);
        mExtensionTypeMap.put(".gif", TYPE_GIF);
        mExtensionTypeMap.put(".gz", TYPE_GZ);
        mExtensionTypeMap.put(".jpeg", TYPE_JPEG);
        mExtensionTypeMap.put(".jpg", TYPE_JPG);
        mExtensionTypeMap.put(".m4a", TYPE_M4A);
        mExtensionTypeMap.put(".mkv", TYPE_MKV);
        mExtensionTypeMap.put(".mov", TYPE_MOV);
        mExtensionTypeMap.put(".mp3", TYPE_MP3);
        mExtensionTypeMap.put(".mp4", TYPE_MP4);
        mExtensionTypeMap.put(".pdf", TYPE_PDF);
        mExtensionTypeMap.put(".pic", TYPE_PIC);
        mExtensionTypeMap.put(".png", TYPE_PNG);
        mExtensionTypeMap.put(".rar", TYPE_RAR);
        mExtensionTypeMap.put(".txt", TYPE_TXT);
        mExtensionTypeMap.put(".wav", TYPE_WAV);
        mExtensionTypeMap.put(".wma", TYPE_WMA);
        mExtensionTypeMap.put(".zip", TYPE_ZIP);
        mExtensionTypeMap.put(".xls", TYPE_XLS);
        mExtensionTypeMap.put(".ppt", TYPE_PPT);
        mExtensionTypeMap.put(".xml", TYPE_XML);
        mExtensionTypeMap.put(".html", TYPE_HTML);
        mExtensionTypeMap.put(".tar", TYPE_TAR);
        mExtensionTypeMap.put(".rmvb", TYPE_RMVB);
        mExtensionTypeMap.put(".opus", TYPE_OPUS);
    }

    public static final int CATEGORY_UNKNOWN = 0; // 图片
    public static final int CATEGORY_PICTURE = 1; // 图片
    public static final int CATEGORY_DOCUMENT = 2; // 文档

    /**根据扩展名获取类型
     * @param canonicalPath
     * @return
     */
    public static int getFileType(String canonicalPath) {

        Log.d(TAG, "getFileType"+canonicalPath);

        if (Tools.isEmpty(canonicalPath)) {
            return TYPE_UNKNOWN;
        }

        int index = canonicalPath.lastIndexOf(".");
        if (index == -1) {
            return TYPE_UNKNOWN;
        }

        String extension = canonicalPath.substring(index).trim();
        Log.d(TAG, "getFileType"+extension);
        Integer value = mExtensionTypeMap.get(extension);
        if (value == null) {
            return TYPE_UNKNOWN;
        }
        return value;
    }

    /**
     * TODO 未区分
     * 根据扩展名获取种类
     *
     * @param canonicalPath
     * @return
     */
    public static int getFileCategory(String canonicalPath) {

        if (Tools.isEmpty(canonicalPath)) {
            return CATEGORY_UNKNOWN;
        }

        int index = canonicalPath.lastIndexOf(".");
        if (index == -1) {
            return CATEGORY_UNKNOWN;
        }

        String extension = canonicalPath.substring(index).trim();
        Integer value = mExtensionTypeMap.get(extension);
        if (value == null) {
            return CATEGORY_UNKNOWN;
        }
        return value;
    }

//    /**
//     * 返回扩展名所能包含的全部信息
//     *
//     * @param canonicalPath
//     * @return
//     */
//    public static int[] getFileTypeAndCategory(String canonicalPath) {
//
//        if (Tools.isEmpty(canonicalPath)) {
//            return mTypeCategoryInfo;
//        }
//
//        int index = canonicalPath.lastIndexOf(".");
//        if (index == -1) {
//            return mTypeCategoryInfo;
//        }
//
//        String extension = canonicalPath.substring(index).trim().toLowerCase(Locale.CHINA);
//        int[] value = DeploymentOperation.mExtensionTypeMap.get(extension);
//        if (value == null) {
//            return mTypeCategoryInfo;
//        }
//        return value;
//    }

    public static int getResourceIdByType(int type) {

        int resourceId;
        switch (type) {
            case TYPE_FOLDER:
                resourceId = R.drawable.icon_fm_folder;
                break;
            case TYPE_3GP:
                resourceId = R.drawable.icon_fm_3gp;
                break;
            case TYPE_AAC:
                resourceId = R.drawable.icon_fm_aac;
                break;
            case TYPE_AMR:
                resourceId = R.drawable.icon_fm_amr;
                break;
            case TYPE_APK:
                resourceId = R.drawable.icon_fm_apk;
                break;
            case TYPE_AVI:
                resourceId = R.drawable.icon_fm_avi;
                break;
            case TYPE_DNG:
                resourceId = R.drawable.icon_fm_dng;
                break;
            case TYPE_DOC:
                resourceId = R.drawable.icon_fm_doc;
                break;
            case TYPE_DOCX:
                resourceId = R.drawable.icon_fm_docx;
                break;
            case TYPE_EXE:
                resourceId = R.drawable.icon_fm_exe;
                break;
            case TYPE_FLAC:
                resourceId = R.drawable.icon_fm_flac;
                break;
            case TYPE_GIF:
                resourceId = R.drawable.icon_fm_gif;
                break;
            case TYPE_GZ:
                resourceId = R.drawable.icon_fm_gz;
                break;
            case TYPE_JPEG:
                resourceId = R.drawable.icon_fm_jpeg;
                break;
            case TYPE_JPG:
                resourceId = R.drawable.icon_fm_jpg;
                break;
            case TYPE_M4A:
                resourceId = R.drawable.icon_fm_m4a;
                break;
            case TYPE_MKV:
                resourceId = R.drawable.icon_fm_mkv;
                break;
            case TYPE_MOV:
                resourceId = R.drawable.icon_fm_mov;
                break;
            case TYPE_MP3:
//                resourceId = R.drawable.icon_fm_mp3;
                resourceId = R.drawable.icon_fm_music;
                break;
            case TYPE_MP4:
                resourceId = R.drawable.icon_fm_mp4;
                break;
            case TYPE_PDF:
                resourceId = R.drawable.icon_fm_pdf;
                break;
            case TYPE_PIC:
                resourceId = R.drawable.icon_fm_pic;
                break;
            case TYPE_PNG:
                resourceId = R.drawable.icon_fm_png;
                break;
            case TYPE_RAR:
                resourceId = R.drawable.icon_fm_rar;
                break;
            case TYPE_TXT:
                resourceId = R.drawable.icon_fm_txt;
                break;
            case TYPE_WAV:
                resourceId = R.drawable.icon_fm_wav;
                break;
            case TYPE_WMA:
                resourceId = R.drawable.icon_fm_wma;
                break;
            case TYPE_ZIP:
                resourceId = R.drawable.icon_fm_zip;
                break;
            case TYPE_XLS:
                resourceId = R.drawable.icon_fm_file;
                break;
            case TYPE_PPT:
                resourceId = R.drawable.icon_fm_file;
                break;
            case TYPE_XML:
                resourceId = R.drawable.icon_fm_file;
                break;
            case TYPE_HTML:
                resourceId = R.drawable.icon_fm_file;
                break;
            case TYPE_TAR:
                resourceId = R.drawable.icon_fm_tar;
                break;
            case TYPE_RMVB:
                resourceId = R.drawable.icon_fm_video;
                break;
            case TYPE_MUSIC:
                resourceId = R.drawable.icon_fm_music;
                break;
            case TYPE_OPUS:
                resourceId = R.drawable.icon_fm_opus;
                break;
            default:
                resourceId = R.drawable.icon_fm_unknow;
                break;
        }
        return resourceId;
    }

    public static boolean isZip(String path) {
        path = path.toLowerCase();
        boolean isZip = false;
        if (path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".tar") || path.endsWith(".gz")) {
            return true;
        }
        return isZip;
    }

    public static boolean isDocument(String path) {
        path = path.toLowerCase();
        boolean flag = false;
        if (path.endsWith(".txt") || path.endsWith(".doc") || path.endsWith(".docx") || path.endsWith(".xls") || path.endsWith(".xlsx")
                || path.endsWith(".ppt") || path.endsWith(".pptx") || path.endsWith(".xml") || path.endsWith(".html") || path.endsWith(".htm")) {
            return true;
        }
        return flag;
    }

    public static boolean isApk(String path) {
        path = path.toLowerCase();
        boolean flag = false;
        if (path.endsWith(".apk")) {
            return true;
        }
        return flag;
    }

    public static int getResuorceIdByPath(String path) {
        int type = getFileType(path.toLowerCase());
        return getResourceIdByType(type);

    }

}
