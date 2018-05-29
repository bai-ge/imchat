package com.baige.telephone;


import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.FriendView;

/**
 * Created by baige on 2017/10/29.
 */

public interface PhoneContract {
    interface Presenter extends BasePresenter {
        void onHangUp();

        void onPickUp();
    }

    interface View extends BaseView<Presenter> {

        void showFriend(FriendView friendView);

        void showDelayTime(long delay);

        void showFriendImg(String imgName);

        void showName(String name);

        void showAddress(String address);

        void showStatus(String text);

        void showLog(String text);

        void showLog(TelePhone.LogBean logBean);

        void showProgress(boolean isShow);

        void clearLog();

        void hidePickUpBtn();

        void showTip(String text);

        void setSpeakerphoneOn(boolean on);

        void close();

    }
}
