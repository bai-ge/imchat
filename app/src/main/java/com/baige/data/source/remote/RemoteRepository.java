package com.baige.data.source.remote;

import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.common.Parm;
import com.baige.data.dao.ChatMessageDAO;
import com.baige.data.dao.FileDAO;
import com.baige.data.dao.FriendDAO;
import com.baige.data.dao.UserDAO;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.source.DataSource;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;
import com.baige.util.Tools;
import com.baige.util.UploadTools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/4.
 */

public class RemoteRepository implements DataSource, ServerHelper {

    private final static String TAG = RemoteRepository.class.getName();

    private static RemoteRepository INSTANCE = null;

    private static final int TIME_OUT = 10 * 1000;// 超时时间

    private static final String CHARSET = "utf-8";// 设置编码

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

    public String getServerAddress() {
        return "http://"+CacheRepository.getInstance().getServerIp()+":12060";
    }

    private void HttpURLPost(String url, String json, PrimaryCallback callBack) {
        checkNotNull(url);
        checkNotNull(json);
        checkNotNull(callBack);
        HttpURLConnection connection;
        OutputStreamWriter out = null;
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        Log.d(TAG, url + " " + json);
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
        } finally {
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

    /**
     * Android上传单个文件并带参数到服务端
     *
     *
     * @param remark     用于提示FileView 信息更新
     * @param requestUrl 请求的url
     * @param params     参数列表
     * @param file       需要上传的文件
     * @param fileKey    文件key
     * @param fileType   文件类型(image/png, image/gif, image/jpeg, image/pjpeg, image/x-png, application/octet-stream)
     * @param callback
     */
    public static void uploadOneFileWithParams(String remark, String requestUrl, Map<String, String> params, File file, String fileKey, String fileType, HttpBaseCallback callback) {
        StringBuffer response = new StringBuffer();
        BufferedReader reader = null;
        String BOUNDARY = UUID.randomUUID().toString().replace("-", "");// 边界标识 随机生成
        String PREFIX = "--";
        String LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";// 内容类型
        Log.d(TAG, requestUrl + " ,fileKey" + fileKey);
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);// 允许输入流
            conn.setDoOutput(true);// 允许输出流
            conn.setUseCaches(false);// 不允许使用缓存
            conn.setRequestMethod("POST");// 请求方式
            conn.setRequestProperty("Charset", CHARSET);// 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + "; boundary=" + BOUNDARY);


            // 首先组拼文本类型的参数
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINE_END);
                sb.append("Content-Type: text/plain; charset=" + CHARSET + LINE_END);
                sb.append("Content-Transfer-Encoding: 8bit" + LINE_END);
                sb.append(LINE_END);
                sb.append(entry.getValue());
                sb.append(LINE_END);
            }
            Log.i(TAG,  "params :\n" + sb.toString());
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(sb.toString().getBytes());
            sb = new StringBuilder();
            // 发送文件数据
            if (file != null) {
                long totalSize = file.length();
                long countSize = 0;
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);
                /**
                 * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                 * filename是文件的名字，包含后缀名的 比如:abc.png
                 */
                sb.append("Content-Disposition: form-data; name=\"" + fileKey + "\"; filename=\"" + file.getName() + "\"" + LINE_END);
                sb.append("Content-Type: " + fileType + LINE_END);
                sb.append(LINE_END);
                Log.i(TAG,  "file :\n" + sb.toString());
                outStream.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    outStream.write(bytes, 0, len);
                    countSize += len;
                    callback.progress(remark, file.getName(), countSize, totalSize);
                }
                is.close();
                outStream.write(LINE_END.getBytes());
            }
            sb = new StringBuilder();
            sb.append(PREFIX + BOUNDARY + PREFIX + LINE_END);
            outStream.write(sb.toString().getBytes());
            outStream.flush();
            /**
             * 获取响应码 200=成功 当响应成功，获取响应的流
             */
            int res = conn.getResponseCode();
            Log.e(TAG, "response code:" + res);
            if(res == 200){
                Log.e(TAG, "request success");
                callback.uploadFinish(remark, file.getName());
            }else{
                Log.e(TAG, "request fail");
                callback.fail(remark, file.getName());
            }

            //读取响应
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            conn.disconnect();
            Log.d(TAG, "服务器反馈信息" + response.toString());
            callback.response(response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.error(remark, file.getName(), e);
        } catch (IOException e) {
            e.printStackTrace();
            callback.error(remark, file.getName(), e);
        } finally {
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //TODO 保留，但不用
    @Override
    public void downloadFile(String remark, String url, String path, String fileName, HttpBaseCallback callback) {
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
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                Log.i(TAG, url + "连接成功!");
                File filePath = new File(path);
                if(!filePath.exists()){
                    filePath.mkdirs();
                }
                File saveFile = new File(filePath, fileName);
                //TODO 做文件缓存
                if(saveFile.exists()){
                    saveFile.delete();
                }
                bos = new BufferedOutputStream(new FileOutputStream(saveFile));
                byte[] buffer = new byte[1024];
                long totalSize = -1;
                long countSize = 0;
                int len = 0;
                while( (len = inputStream.read(buffer)) != -1){
                    bos.write(buffer, 0, len);
                    totalSize += len;
                    callback.progress(remark, fileName, countSize, totalSize); //TODO 未知
                }
                bos.flush();
                Log.i(TAG, "下载文件成功："+fileName);
                callback.downloadFinish(remark, fileName);
            }else{
                callback.fail(remark, fileName);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.error(remark, fileName, e);
        } catch (IOException e) {
            e.printStackTrace();
            callback.error(remark, fileName, e);
        }finally {
            if(conn != null ){
                conn.disconnect();
            }
            if(bos != null){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void login(User user, HttpBaseCallback callBack) {
        Log.d(TAG, "登录：" + user);
        String url = getServerAddress() + "/imchat/user/login.action";
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
        Log.d(TAG, "注册：" + user);
        String url = getServerAddress() + "/imchat/user/register.action";
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
        Log.d(TAG, "修改别名：" + alias);
        String url = getServerAddress() + "/imchat/user/alias.action";
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

    private String doPost(String url, String imagePath) {
        return null;
    }

    @Override
    public void changeHeadImg(int id, String verification, File headImg, HttpBaseCallback callback) {
        String url = getServerAddress() + "/imchat/user/changeImg.action";
        Map<String, String> params = new HashMap<>();
        params.put(UserDAO.ID, String.valueOf(id));
        params.put(UserDAO.VERIFICATION, verification);
        uploadOneFileWithParams(headImg.getName(), url, params, headImg, "headImg", "image/jpeg", callback);
    }

    @Override
    public void downloadImg(String imgName, HttpBaseCallback callback) {
        String url = getServerAddress() + "/imchat/user/downloadImg.action?imgFileName="+imgName;
        LoadingManager.getInstance().downloadFile(url, url, BaseApplication.headImgPath, imgName, callback);
    }

    @Override
    public void uploadFile(String remark, User user, FileInfo file, HttpBaseCallback callback) {
        String url = getServerAddress() + "/imchat/file/upload.action";
        File f = new File(file.getPath());
//        UploadUtil.uploadFile(f, url);
        Map<String, String> params = new HashMap<>();
        params.put(Parm.UID, String.valueOf(user.getId()));
        params.put(UserDAO.VERIFICATION, user.getVerification());
        params.put(FileDAO.FILE_SIZE, String.valueOf(f.length()));
        params.put(FileDAO.FILE_TYPE, String.valueOf(file.getFileType()));
        params.put(FileDAO.REMARK, remark);
        uploadOneFileWithParams(remark, url, params, f, "upload", "application/octet-stream", callback);
//        UploadTools.uploadFile(f, url, "upload");
        // doPost(url, file);
    }

    @Override
    public void searchUserBykeyword(int id, String verification, String key, HttpBaseCallback callback) {
        Log.d(TAG, "查找用户：" + key);
        String url = getServerAddress() + "/imchat/user/search.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(UserDAO.ID, id);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(Parm.KEYWORD, key);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void searchFriend(int id, String verification, HttpBaseCallback callback) {
        Log.d(TAG, "查找好友");
        String url = getServerAddress() + "/imchat/friend/search.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, id);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void changeFriendAlias(int id, int uid, String verification, String alias, HttpBaseCallback callback) {
        Log.d(TAG, "更改好友备注");
        String url = getServerAddress() + "/imchat/friend/changAlias.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(FriendDAO.ID, id);
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(UserDAO.ALIAS, alias);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void relateUser(int uid, String verification, int friendId, HttpBaseCallback callback) {
        Log.d(TAG, "添加好友");
        String url = getServerAddress() + "/imchat/friend/relate.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(FriendDAO.FRIEND_ID, friendId);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void operationFriend(int id, int uid, String verification, int friendId, String operation, HttpBaseCallback callback) {
        Log.d(TAG, "好友操作" + operation);
        String url = getServerAddress() + "/imchat/friend/operation.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(FriendDAO.ID, id);
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(FriendDAO.FRIEND_ID, friendId);
            jsonObject.put(Parm.OPERATION, operation);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }


    @Override
    public void sendMsg(ChatMsgInfo chatMsgInfo, String verification, HttpBaseCallback callback) {
        Log.d(TAG, "发送信息" + chatMsgInfo.getContext());
        String url = getServerAddress() + "/imchat/chat/send.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, chatMsgInfo.getSenderId());
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.RECEIVE_ID, chatMsgInfo.getReceiveId());
            jsonObject.put(ChatMessageDAO.CONTEXT, chatMsgInfo.getContext());
            jsonObject.put(ChatMessageDAO.CONTEXT_TYPE, chatMsgInfo.getContextType());
            jsonObject.put(ChatMessageDAO.REMARK, chatMsgInfo.getRemark());
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsgRelate(int uid, String verification, int friendId, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/find.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.RECEIVE_ID, friendId);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsgRelateAfterTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/findAfterTime.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.RECEIVE_ID, friendId);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsgRelateBeforeTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/findBeforeTime.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.RECEIVE_ID, friendId);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsg(int uid, String verification, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/find.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsgAfterTime(int uid, String verification, long time, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/findAfterTime.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void findMsgBeforeTime(int uid, String verification, long time, HttpBaseCallback callback) {
        Log.d(TAG, "查找消息");
        String url = getServerAddress() + "/imchat/chat/findBeforeTime.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(ChatMessageDAO.SENDER_ID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void readMsgBeforeTime(int uid, String verification, long time, HttpBaseCallback callback) {
        Log.d(TAG, "标记已读");
        String url = getServerAddress() + "/imchat/chat/read.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void readMsgBeforeTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback) {
        Log.d(TAG, "标记已读");
        String url = getServerAddress() + "/imchat/chat/read.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(FriendDAO.FRIEND_ID, friendId);
            jsonObject.put(ChatMessageDAO.SEND_TIME, time);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void searchAllFile(HttpBaseCallback callback) {
        Log.d(TAG, "获取文件列表");
        String url = getServerAddress() + "/imchat/file/search.action";
        HttpURLPost(url, "{}", callback);
    }

    @Override
    public void shareFile(int uid, String verification, FileView fileView, HttpBaseCallback callback) {
        Log.d(TAG, "分享文件");
        String url = getServerAddress() + "/imchat/file/share.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(FileDAO.FILE_NAME, fileView.getFileName());
            jsonObject.put(FileDAO.FILE_PATH, fileView.getFilePath());
            jsonObject.put(FileDAO.FILE_DESCRIBE, fileView.getFileDescribe());
            jsonObject.put(FileDAO.FILE_TYPE, fileView.getFileType());
            jsonObject.put(FileDAO.FILE_SIZE, fileView.getFileSize());
            jsonObject.put(FileDAO.REMARK, fileView.getRemark());
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }

    @Override
    public void downloadFile(String remark, int fid, String fileName, HttpBaseCallback callback) {
        Log.d(TAG, "下载文件"+fileName);
        String url = getServerAddress() + "/imchat/file/download.action?fn="+fid;
        LoadingManager.getInstance().downloadFile(remark, url, BaseApplication.downloadPath, fileName, callback);
    }

    @Override
    public void updateDownloadCount(int fid, HttpBaseCallback callback) {
        Log.d(TAG, "更新下载数量");
        String url = getServerAddress() + "/imchat/file/downloadCount.action?fn="+fid;
        HttpURLGet(url, callback);
    }

    @Override
    public void deleteFile(int fid, int uid, String verification, HttpBaseCallback callback) {
        Log.d(TAG, "删除远程文件");
        String url = getServerAddress() + "/imchat/file/delete.action";
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(UserDAO.VERIFICATION, verification);
            jsonObject.put(Parm.FN, fid);
            HttpURLPost(url, jsonObject.toString(), callback);
        } catch (JSONException e) {
            e.printStackTrace();
            callback.error(e);
        }
    }
}
