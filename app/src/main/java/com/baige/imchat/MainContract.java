package com.baige.imchat;

import android.graphics.Bitmap;

import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.LastChatMsgInfo;

import java.util.List;

/**
 * Created by baige on 2018/5/4.
 */

public interface MainContract {

    interface Presenter extends BasePresenter {
        void updateAlias(String text);

        void changeImg(String file);

        void downloadImg(String imgName);

        void loadFriends();

        void loadMsg();

    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);

        void showNetwork(int state);

        void showInform(String text);

        void showInform(String text, long time);

        void hideInform();

        void showDrawer();

        void showUserImg(String imgName);

        void showUserImg(Bitmap img);

        void showUserName(String name);

        void showUserAlias(String alias);

        void showFriends(List<FriendView> friendViewList);

        void showLastChatMsgs(List<LastChatMsgInfo> lastChatMsgInfos);
    }
}
