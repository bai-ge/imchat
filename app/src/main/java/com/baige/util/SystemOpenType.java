package com.baige.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.imchat.BuildConfig;

import java.io.File;


/**
 * Created by baige on 2018/6/4.
 */

public class SystemOpenType {

    public static void systemOpen(FileInfo fileInfo, Context context) {
        if (fileInfo == null || context == null) {
            return;
        }

//        String uriFile = "file://" + fileInfo.getPath();
//        Uri uri = Uri.parse(uriFile);

        Intent intent = new Intent();
        File uriFile = new File(fileInfo.getPath());
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", uriFile);
        } else {
            uri = Uri.fromFile(uriFile);
        }
        switch (fileInfo.getFileType()) {
            case FileType.TYPE_FOLDER:
                break;
            case FileType.TYPE_3GP:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_AAC:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_AMR:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_APK:
                openApk(uri, context, intent);
                break;
            case FileType.TYPE_AVI:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_DNG:
                openImage(uri, context, intent);
                break;
            case FileType.TYPE_DOC:
                openWord(uri, context, intent);
                break;
            case FileType.TYPE_DOCX:
                openWord(uri, context, intent);
                break;
            case FileType.TYPE_EXE:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_FLAC:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_GIF:
                openImage(uri, context, intent);
                break;
            case FileType.TYPE_GZ:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_JPEG:
                openImage(uri, context, intent);
                break;
            case FileType.TYPE_JPG:
                openImage(uri, context, intent);
                break;
            case FileType.TYPE_M4A:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_MKV:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_MOV:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_MP3:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_MP4:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_PDF:
                openPDF(uri, context, intent);
                break;
            case FileType.TYPE_PIC:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_PNG:
                openImage(uri, context, intent);
                break;
            case FileType.TYPE_RAR:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_TXT:
                openText(uri, context, intent);
                break;
            case FileType.TYPE_WAV:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_WMA:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_ZIP:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_XLS:
                openExcel(uri, context, intent);
                break;
            case FileType.TYPE_PPT:
                openPPT(uri, context, intent);
                break;
            case FileType.TYPE_XML:
                openText(uri, context, intent);
                break;
            case FileType.TYPE_HTML:
                openHtml(uri, context, intent);
                break;
            case FileType.TYPE_TAR:
                openAll(uri, context, intent);
                break;
            case FileType.TYPE_RMVB:
                openVideo(uri, context, intent);
                break;
            case FileType.TYPE_MUSIC:
                openAudio(uri, context, intent);
                break;
            case FileType.TYPE_OPUS:
                openAudio(uri, context, intent);
                break;
            default:
                openAll(uri, context, intent);
                break;
        }
    }

    public static void openAll(Uri uri, Context context, Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "*/*");
        context.startActivity(intent);
    }
    public static void openApk(Uri uri, Context context, Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri,"application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static void openVideo(Uri uri, Context context, Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");
        context.startActivity(intent);
    }

    public static void openAudio(Uri uri, Context context, Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "audio/*");
        context.startActivity(intent);
    }
    public static void openHtml(Uri uri, Context context, Intent intent){
//        Uri uri2 = Uri.parse(uri).buildUpon().encodedAuthority("com.android.htmlfileprovider").scheme("content").encodedPath(param ).build();
//        Intent intent = new Intent("android.intent.action.VIEW");
//        intent.setDataAndType(uri2, "text/html");
//        context.startActivity(intent);
    }
    public static void openImage(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/*");
        context.startActivity(intent);
    }
    public static void openPPT(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        context.startActivity(intent);
    }
    public static void openExcel(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.ms-excel");
        context.startActivity(intent);
    }
    public static void openWord(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(uri, "application/msword");
        context.startActivity(intent);
    }
    public static void openText(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/plain");
        context.startActivity(intent);
    }
    public static void openPDF(Uri uri, Context context, Intent intent){
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        context.startActivity(intent);
    }
}
