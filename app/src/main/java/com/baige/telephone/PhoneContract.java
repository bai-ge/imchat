package com.baige.telephone;


import com.baige.BasePresenter;
import com.baige.BaseView;

/**
 * Created by baige on 2017/10/29.
 */

public interface PhoneContract {
    interface Presenter extends BasePresenter {
        void onHangUp();

        void onPickUp();
    }

    interface View extends BaseView<Presenter> {
//        void showUser(User user);
//      void showAddress(User user);
        void showTip(String text);

    }
}
