package com.baige.login;


import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.User;

/**
 * Created by baige on 2017/12/22.
 */

public interface LoginContract {

    interface Presenter extends BasePresenter {
        void login(String name, String psw);
        void login(User user, String psw);
    }

    interface View extends BaseView<Presenter> {
        void showName(String name);
        void setPsw(String psw);
        void showTip(String text);
        void finishActivity();
        void onBack();
    }
}
