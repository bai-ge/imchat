package com.baige.chat;

import android.graphics.Bitmap;


import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.ChatMsgInfo;

import java.util.List;

/**
 * Created by 百戈 on 2017/2/19.
 */

public interface ChatContract {

    interface Presenter extends BasePresenter {
        void sendMsg(String msg);
        void loadMsg();
        void loadMsgAfterTime(long time);
        void loadMsgBeforeTime(long time);
    }

    interface View extends BaseView<Presenter> {
        void clearMsg();
        void showFriendName(String name);
        void showFriendNetwork(String network);
        void showMsg(List<ChatMsgInfo> msgInfoList);
        void showMsg(ChatMsgInfo chatMsgInfo);
        void notifyChange();
    }

}
