package com.baige.data.source.remote;



import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;

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

    }

    void login(User user, HttpBaseCallback callback);

    void register(User user,  HttpBaseCallback callback);

    void updateAlias(int id, String verification, String alias, HttpBaseCallback callback);

    void uploadFile(User user, String file, HttpBaseCallback callback);

}
