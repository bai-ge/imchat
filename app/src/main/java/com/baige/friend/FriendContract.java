package com.baige.friend;


import android.graphics.Bitmap;

import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;

/**
 * Created by baige on 2017/12/22.
 */

public interface FriendContract {

    interface Presenter extends BasePresenter {
        void updateFriendAlias(String text);

        void downloadImg(String imgName);

        void addFriend();

        void agree();

        void reject();

        void deleteFriend();

        void defriend();

        void cancelDefriend();

        FriendView getFriend();

    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);

        void showFriendView(FriendView friendView);

        void showFriendImg(String imgName);

        void showFriendImg(Bitmap img);

        void showFriendName(String name);

        void showUserAlias(String alias);

        void showFriendAlias(String friendAlias);

        void showBtnLayoutByState(int state);

    }
}
