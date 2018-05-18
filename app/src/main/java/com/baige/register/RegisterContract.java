package com.baige.register;


import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.User;

/**
 * Created by baige on 2017/12/22.
 */

public interface RegisterContract {

    interface Presenter extends BasePresenter {
        void register(User user, String code);
        void register(String name, String psw);
    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void delayBack(long time);
    }
}
