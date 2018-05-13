package com.baige.data.source.remote;


import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.util.FileUtils;
import com.baige.util.Tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by baige on 2018/5/12.
 */

public class LoadingManager {

    private final static String TAG = LoadingManager.class.getSimpleName();

    private static LoadingManager INSTANCE;

    private Map<String, LoadingThread> loadingPools;


    private LoadingManager() {
        loadingPools = Collections.synchronizedMap(new LinkedHashMap<String, LoadingThread>());
    }

    public static LoadingManager getInstance() {
        if (INSTANCE == null) {
            synchronized (LoadingManager.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new LoadingManager();
                }
            }
        }
        return INSTANCE;
    }

    class LoadingThread extends Thread {
        String path;
        String tmpFile;
        String fileName;
        long size;
        long totalSize;
        String url;
        HttpBaseCallback callback;
        LoadingManager.State state;

        public boolean isLoading() {
            return state == LoadingManager.State.Loading;
        }

        public boolean isFailed() {
            return state == LoadingManager.State.Failed;
        }

        public boolean isFinished() {
            return state == LoadingManager.State.Finished;
        }

        public void setState(LoadingManager.State state) {
            this.state = state;
        }

        public LoadingThread(String url, String path, String fileName, HttpBaseCallback callback) {
            this.url = url;
            this.path = path;
            this.fileName = fileName;
            this.callback = callback;
            tmpFile = Tools.ramdom() + ".tmp";
        }

        @Override
        public void run() {
            super.run();
            setState(LoadingManager.State.Loading);
            File tmpFilePath = new File(BaseApplication.tmpPath);
            if (!tmpFilePath.exists()) {
                tmpFilePath.mkdirs();
            }

            HttpURLConnection conn = null;
            InputStream inputStream = null;
            BufferedOutputStream bos = null;
            Log.d(TAG, url + " ,fileName" + fileName);
            try {
                URL httpUrl = new URL(url);
                conn = (HttpURLConnection) httpUrl.openConnection();
                conn.setConnectTimeout(8 * 1000);
                conn.setRequestMethod("GET");
                inputStream = conn.getInputStream();
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, url + "连接成功!");

                    File saveFile = new File(tmpFilePath, tmpFile);
                    //TODO 做文件缓存
                    if (saveFile.exists()) {
                        saveFile.delete();
                    }
                    bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                    byte[] buffer = new byte[1024];
                    long totalSize = -1;
                    long countSize = 0;
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                        totalSize += len;
                        callback.progress(fileName, countSize, totalSize); //TODO 未知
                    }
                    bos.flush();
                    bos.close();
                    bos = null;
                    //转移文件
                    File toFilePath = new File(path);
                    if (!toFilePath.exists()) {
                        toFilePath.mkdirs();
                    }
                    File toFile = new File(toFilePath, fileName);
                    if (toFile.exists()) {
                        toFile.delete();
                    }
                    FileUtils.moveTo(saveFile, toFile);
                    setState(LoadingManager.State.Finished);
                    Log.i(TAG, "下载文件成功：" + fileName);
                    callback.downloadFinish(fileName);

                } else {
                    setState(LoadingManager.State.Failed);
                    callback.fail(fileName);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                setState(LoadingManager.State.Failed);
                callback.error(fileName, e);
            } catch (IOException e) {
                e.printStackTrace();
                setState(LoadingManager.State.Failed);
                callback.error(fileName, e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                //移除该下载线程
                synchronized (LoadingManager.class) {
                    Object obj = loadingPools.get(url);
                    if (obj != null && obj.equals(this)) {
                        loadingPools.remove(path + File.separator + fileName);
                    }
                }
            }

        }
    }

    public enum State {
        Loading, Failed, Finished
    }


    public void downloadFile(String url, String path, String fileName, HttpBaseCallback callback) {
        LoadingThread thread = loadingPools.get(url);
        LoadingThread loadingThread;
        if (thread == null) {
            loadingThread = new LoadingThread(url, path, fileName, callback);
            loadingPools.put(url, loadingThread);
            loadingThread.start();
        } else if (thread.isLoading()) {
            return;
        } else if (thread.isFailed()) {
            thread.interrupt();
            loadingPools.remove(url);
            loadingThread = new LoadingThread(url, path, fileName, callback);
            loadingPools.put(url, loadingThread);
            loadingThread.start();
        }
    }
}
