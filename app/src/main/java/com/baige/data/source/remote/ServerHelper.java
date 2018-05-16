package com.baige.data.source.remote;



import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;

import java.io.File;
import java.util.List;


/**
 * Created by baige on 2017/12/22.
 */

public interface ServerHelper {

    /*基本接口部分*/
    interface PrimaryCallback {
        void timeout(); //连不上网，或迟迟得不到响应

        void response(String json);

        void error(Exception e); //运行出错

        void onFinish();
    }

    /*根据json解析后，调用的返回码接口*/
    interface CodeCallback {

        void success();

        void fail();

        void unknown();

        void notFind();//未找到资源

        void typeConvert();//输入参数类型错误

        void exist();//资源已经存在

        void isBlank();//参数为空

        void timeout();

        void invalid(); //无效
    }

    /*复杂的接口部分*/
    interface ComplexCallback {

        void meaning(String text); //返回的中文解释

        void onResponse();//服务器有响应，进度条应该停止

        void loadAUser(User user);

        void loadUsers(List<User> list);

        void loadFriendView(FriendView friendView);

        void loadFriendViews(List<FriendView> list);

        void loadMsg(ChatMsgInfo chatMsgInfo);

        void loadMsgList(List<ChatMsgInfo> chatMsgInfos);

    }
    interface FileCallback{
        void progress(String fileName, long finishSize, long totalSize);
        void uploadFinish(String fileName);
        void downloadFinish(String fileName);
        void error(String fileName, Exception e);
        void fail(String fileName);
    }
    

    void login(User user, HttpBaseCallback callback);

    void register(User user,  HttpBaseCallback callback);

    void updateAlias(int id, String verification, String alias, HttpBaseCallback callback);

    void uploadFile(User user, String file, HttpBaseCallback callback);

    void changeHeadImg(int id, String verification, File headImg, HttpBaseCallback callback);

    void downloadFile(String url, String path, String fileName, HttpBaseCallback callback);

    void downloadImg(String imgName, HttpBaseCallback callback);

    void searchUserBykeyword(int id, String verification, String key, HttpBaseCallback callback);

    void searchFriend(int id, String verification, HttpBaseCallback callback);

    void changeFriendAlias(int id, int uid, String verification, String alias, HttpBaseCallback callback);

    void relateUser(int uid, String verification, int friendId, HttpBaseCallback callback);

    void operationFriend(int id, int uid, String verification, int friendId, String operation, HttpBaseCallback callback);

    //void sendMsg(int uid, String verification, int friendId, String msg, int type,  HttpBaseCallback callback);

    void sendMsg(ChatMsgInfo chatMsgInfo, String verification, HttpBaseCallback callback);

    void findMsgRelate(int uid, String verification, int friendId, HttpBaseCallback callback);

    void findMsgRelateAfterTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback);

    void findMsgRelateBeforeTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback);

    void findMsg(int uid, String verification, HttpBaseCallback callback);

    void findMsgAfterTime(int uid, String verification, long time, HttpBaseCallback callback);

    void findMsgBeforeTime(int uid, String verification, long time, HttpBaseCallback callback);

    void readMsgBeforeTime(int uid, String verification, long time, HttpBaseCallback callback);

    void readMsgBeforeTime(int uid, String verification, int friendId, long time, HttpBaseCallback callback);
}
