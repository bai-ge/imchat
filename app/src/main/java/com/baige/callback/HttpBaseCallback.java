package com.baige.callback;


import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FileView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.source.remote.ServerHelper;

import java.util.List;

/**
 * Created by baige on 2018/5/10.
 */

public class HttpBaseCallback extends BaseCallback implements ServerHelper.PrimaryCallback, ServerHelper.CodeCallback, ServerHelper.ComplexCallback, ServerHelper.FileCallback{

    private AbstractResponseBinder mResponseBinder;


    public void setResponseBinder(AbstractResponseBinder responseBinder) {
        this.mResponseBinder = responseBinder;
    }

    @Override
    public void timeout() {

    }

    @Override
    public final void response(String json) { //不可重写
        /*
        * 通用解析器
        * 需要根据服务器返回的json 数据，调用本身不同的函数，解决持续通信问题
        * */
        if (mResponseBinder != null) {
            mResponseBinder.parse(json, this);
        }
    }

    @Override
    public void error(Exception e) {

    }

    @Override
    public void onFinish() {

    }

    @Override
    public void success() {

    }

    @Override
    public void fail() {

    }

    @Override
    public void unknown() {

    }

    @Override
    public void notFind() {

    }

    @Override
    public void typeConvert() {

    }

    @Override
    public void exist() {

    }

    @Override
    public void isBlank() {

    }

    @Override
    public void invalid() {

    }

    @Override
    public void meaning(String text) {

    }

    @Override
    public void onResponse() {

    }

    @Override
    public void loadAUser(User user) {

    }

    @Override
    public void loadUsers(List<User> list) {

    }

    @Override
    public void loadFriendView(FriendView friendView) {

    }

    @Override
    public void loadFriendViews(List<FriendView> list) {

    }

    @Override
    public void loadMsg(ChatMsgInfo chatMsgInfo) {

    }

    @Override
    public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {

    }

    @Override
    public void loadFile(FileView fileView) {

    }

    @Override
    public void loadFiles(List<FileView> fileViews) {

    }

    @Override
    public void loadObject(Object obj) {

    }

    @Override
    public void loadList(List<Object> list) {

    }

    @Override
    public void loadFail() {

    }

    @Override
    public void progress(String remark, String fileName, long finishSize, long totalSize) {

    }

    @Override
    public void uploadFinish(String remark, String fileName) {

    }

    @Override
    public void downloadFinish(String remark, String fileName) {

    }

    @Override
    public void error(String remark, String fileName, Exception e) {

    }

    @Override
    public void fail(String remark, String fileName) {

    }

}
