package com.baige.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;


/**
 * Created by baige on 2018/6/4.
 */

public class SystemOpenType {

    public static void systemOpen(FileInfo fileInfo, Context context) {
        if (fileInfo == null || context == null) {
            return;
        }
        String uriFile = "file://" + fileInfo.getPath();
        Uri uri = Uri.parse(uriFile);
        switch (fileInfo.getFileType()) {
            case FileType.TYPE_FOLDER:
                break;
            case FileType.TYPE_3GP:
                openVideo(uri, context);
                break;
            case FileType.TYPE_AAC:
                openAudio(uri, context);
                break;
            case FileType.TYPE_AMR:
                openAudio(uri, context);
                break;
            case FileType.TYPE_APK:
                openApk(uri, context);
                break;
            case FileType.TYPE_AVI:
                openVideo(uri, context);
                break;
            case FileType.TYPE_DNG:
                openImage(uri, context);
                break;
            case FileType.TYPE_DOC:
                openWord(uri, context);
                break;
            case FileType.TYPE_DOCX:
                openWord(uri, context);
                break;
            case FileType.TYPE_EXE:
                openAll(uri, context);
                break;
            case FileType.TYPE_FLAC:
                openAudio(uri, context);
                break;
            case FileType.TYPE_GIF:
                openImage(uri, context);
                break;
            case FileType.TYPE_GZ:
                openAll(uri, context);
                break;
            case FileType.TYPE_JPEG:
                openImage(uri, context);
                break;
            case FileType.TYPE_JPG:
                openImage(uri, context);
                break;
            case FileType.TYPE_M4A:
                openAudio(uri, context);
                break;
            case FileType.TYPE_MKV:
                openVideo(uri, context);
                break;
            case FileType.TYPE_MOV:
                openVideo(uri, context);
                break;
            case FileType.TYPE_MP3:
                openAudio(uri, context);
                break;
            case FileType.TYPE_MP4:
                openVideo(uri, context);
                break;
            case FileType.TYPE_PDF:
                openPDF(uri, context);
                break;
            case FileType.TYPE_PIC:
                openAll(uri, context);
                break;
            case FileType.TYPE_PNG:
                openImage(uri, context);
                break;
            case FileType.TYPE_RAR:
                openAll(uri, context);
                break;
            case FileType.TYPE_TXT:
                openText(uri, context);
                break;
            case FileType.TYPE_WAV:
                openAudio(uri, context);
                break;
            case FileType.TYPE_WMA:
                openAudio(uri, context);
                break;
            case FileType.TYPE_ZIP:
                openAll(uri, context);
                break;
            case FileType.TYPE_XLS:
                openExcel(uri, context);
                break;
            case FileType.TYPE_PPT:
                openPPT(uri, context);
                break;
            case FileType.TYPE_XML:
                openText(uri, context);
                break;
            case FileType.TYPE_HTML:
                openHtml(uri, context);
                break;
            case FileType.TYPE_TAR:
                openAll(uri, context);
                break;
            case FileType.TYPE_RMVB:
                openVideo(uri, context);
                break;
            case FileType.TYPE_MUSIC:
                openAudio(uri, context);
                break;
            case FileType.TYPE_OPUS:
                openAudio(uri, context);
                break;
            default:
                openAll(uri, context);
                break;
        }

    }

    public static void openAll(Uri uri, Context context){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "*/*");
        context.startActivity(intent);
    }
    public static void openApk(Uri uri, Context context){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void openVideo(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(uri, "video/*");
        context.startActivity(intent);
    }

    public static void openAudio(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setDataAndType(uri, "audio/*");
        context.startActivity(intent);
    }
    public static void openHtml(Uri uri, Context context){
//        Uri uri2 = Uri.parse(uri).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.setDataAndType(uri2, "text/html");
//        context.startActivity(intent);
    }
    public static void openImage(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }
    public static void openPPT(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        context.startActivity(intent);
    }
    public static void openExcel(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        context.startActivity(intent);
    }
    public static void openWord(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);intent.setDataAndType(uri, "application/msword");
        context.startActivity(intent);
    }
    public static void openText(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "text/plain");
        context.startActivity(intent);
    }
    public static void openPDF(Uri uri, Context context){
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/pdf");
        context.startActivity(intent);
    }
}
