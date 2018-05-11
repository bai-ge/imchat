package com.baige.data.source.remote;

import android.util.Log;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.dao.UserDAO;
import com.baige.data.entity.User;
import com.baige.data.source.DataSource;
import com.baige.data.source.local.LocalRepository;
import com.baige.util.UploadTools;
import com.baige.util.UploadUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/4.
 */

public class RemoteRepository implements DataSource, ServerHelper{

    private final static String TAG = RemoteRepository.class.getName();

    private static RemoteRepository INSTANCE = null;

    private String serverAddress = "http://192.168.1.101:8080";

    //这里本地数据库只用来获取本地用户信息
    private LocalRepository mLocalRepository;

    private RemoteRepository(LocalRepository localRepository) {
        mLocalRepository = localRepository;
    }

    public static RemoteRepository getInstance(LocalRepository localRepository) {
        if (INSTANCE == null) {
            synchronized (RemoteRepository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new RemoteRepository(localRepository);
                }
            }
        }
        return INSTANCE;
    }


    private void HttpURLPost(String url, String json, PrimaryCallback callBack) {
        checkNotNull(url);
        checkNotNull(json);
        checkNotNull(callBack);
        HttpURLConnection connection;
        OutputStreamWriter out = null;
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        Log.d(TAG, url+" "+json);
        try {
            URL httpUrl = new URL(url);
            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setConnectTimeout(8 * 1000);
            connection.setReadTimeout(8 * 1000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);//设置不要缓存
            connection.setInstanceFollowRedirects(true);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            //POST请求
            out = new OutputStreamWriter(connection.getOutputStream());
            out.write(json);
            out.flush();

            //读取相应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            connection.disconnect();
            Log.d(TAG, "服务器反馈信息" + response.toString());
            callBack.response(response.toString());
        } catch (IOException e) {
            callBack.error(e);
        }  finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void HttpURLGet(String url, PrimaryCallback callBack) {
        checkNotNull(url);
        checkNotNull(callBack);
        HttpURLConnection connection;
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        Log.d(TAG, url);
        try {
            URL httpUrl = new URL(url);
            connection = (HttpURLConnection) httpUrl.openConnection();
            connection.setConnectTimeout(8 * 1000);
            connection.setReadTimeout(8 * 1000);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setUseCaches(false);//设置不要缓存
            connection.setInstanceFollowRedirects(true);//设置本次连接是否自动处理重定向
            //connection.setDoInput(true);设置这句话会变成POST
            //connection.setDoOutput(true);
            connection.connect();
            //读取相应
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            connection.disconnect();
            Log.d(TAG, "服务器反馈信息" + response.toString());
            callBack.response(response.toString());
        } catch (IOException e) {
            callBack.error(e);

        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void login(User user, HttpBaseCallback callBack) {
        Log.d(TAG, "登录："+user);
        String url = serverAddress + "/imchat/user/login.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UserDAO.NAME, user.getName());
            jsonObject.put(UserDAO.PASSWORD, user.getPassword());
            jsonObject.put(UserDAO.DEVICE_ID, user.getDeviceId());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void register(User user, HttpBaseCallback callBack) {
        Log.d(TAG, "注册："+user);
        String url = serverAddress + "/imchat/user/register.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UserDAO.NAME, user.getName());
            jsonObject.put(UserDAO.PASSWORD, user.getPassword());
            HttpURLPost(url, jsonObject.toString(), callBack);
        } catch (JSONException e) {
            e.printStackTrace();
            callBack.error(e);
        }
    }

    @Override
    public void updateAlias(int id, String verification, String alias, HttpBaseCallback callback) {
        Log.d(TAG, "修改别名："+alias);
        String url = serverAddress + "/imchat/user/alias.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UserDAO.ID, id);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(UserDAO.ALIAS, alias);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void uploadFile(User user, String file, HttpBaseCallback callback) {
        String url = serverAddress + "/imchat/file/upload.action";
        File f = new File(file);
//        UploadUtil.uploadFile(f, url);
        UploadTools.uploadFile(f, url);
    }
}
