package com.baige.callback;


import com.baige.data.entity.User;
import com.baige.data.source.remote.ServerHelper;

import java.util.List;

/**
 * Created by baige on 2018/5/10.
 */

public class HttpBaseCallback extends BaseCallback implements ServerHelper.PrimaryCallback, ServerHelper.CodeCallback, ServerHelper.ComplexCallback {

    private AbstractResponseBinder mResponseBinder;


    public void setResponseBinder(AbstractResponseBinder responseBinder) {
        this.mResponseBinder = responseBinder;
    }

    @Override
    public void timeout() {

    }

    @Override
    public void response(String json) {
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
}
