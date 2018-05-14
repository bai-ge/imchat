package com.baige.chat;


import android.support.annotation.NonNull;
import android.widget.ImageButton;
import android.widget.ListView;

import com.baige.callback.HttpBaseCallback;
import com.baige.common.Parm;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;


import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by 百戈 on 2017/2/19.
 */

public class ChatPresenter implements ChatContract.Presenter {

    private final Repository mRepository;

    private final ChatFragment mChatFragment;

    private FriendView mFriendView;


    public ChatPresenter( @NonNull Repository dateRepository, @NonNull ChatFragment chatWithMeFragment) {
        this.mRepository = checkNotNull(dateRepository);
        this.mChatFragment = checkNotNull(chatWithMeFragment);
        mChatFragment.setPresenter(this);
    }

    @Override
    public void start() {
        if(mFriendView != null){
            mChatFragment.showFriendName(mFriendView.getSuitableName());
        }
        loadMsg();
    }

    public FriendView getFriendView() {
        return mFriendView;
    }

    public void setFriendView(FriendView friendView) {
        this.mFriendView = friendView;
    }

    public void initChatInfo(List<ChatMsgInfo> chatMsgInfos){
        for (ChatMsgInfo ch: chatMsgInfos) {
            initChatInfo(ch);
        }
    }
    public void initChatInfo(ChatMsgInfo chatMsgInfo){
        if(chatMsgInfo != null ){
            int sendId = chatMsgInfo.getSenderId();
            User user = CacheRepository.getInstance().who();
            chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
            if(sendId == user.getId()){
                chatMsgInfo.setShowType(Parm.MSG_IS_SEND);
                chatMsgInfo.setUserName("我");
                chatMsgInfo.setUserImgName(user.getImgName());
            }else{
                chatMsgInfo.setShowType(Parm.MSG_IS_RECEIVE);
                chatMsgInfo.setUserName(mFriendView.getSuitableName());
                chatMsgInfo.setUserImgName(mFriendView.getFriendImgName());
            }
        }
    }
    @Override
    public void sendMsg(String msg) {
        if(!Tools.isEmpty(msg)){
            User user = CacheRepository.getInstance().who();
            final ChatMsgInfo chatMsgInfo = new ChatMsgInfo("我", msg, Parm.MSG_TYPE_TEXT);
            chatMsgInfo.setUserImgName(user.getImgName());
            chatMsgInfo.setShowType(Parm.MSG_IS_SEND);
            chatMsgInfo.setSendState(Parm.MSG_IS_SENDING);
            mChatFragment.showMsg(chatMsgInfo);

            if(user != null && mFriendView != null){
                mRepository.sendMsg(user.getId(),
                        user.getVerification(),
                        mFriendView.getFriendId(),
                        msg,
                        Parm.MSG_TYPE_TEXT,
                        new HttpBaseCallback(){
                            @Override
                            public void success() {
                                super.success();
                                chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                                mChatFragment.notifyChange();
                            }

                            @Override
                            public void loadMsg(ChatMsgInfo reponseChat) {
                                super.loadMsg(chatMsgInfo);
                                chatMsgInfo.setSendTime(reponseChat.getSendTime());
                                chatMsgInfo.setId(reponseChat.getId());
                                chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                                mChatFragment.notifyChange();
                            }
                        });
            }
        }
    }

    @Override
    public void loadMsg() {
        User user = CacheRepository.getInstance().who();
        if(mFriendView != null && user != null){
            mRepository.findMsgRelate(user.getId(), user.getVerification(), mFriendView.getFriendId(), new HttpBaseCallback(){
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    mChatFragment.clearMsg();
                    initChatInfo(chatMsgInfos);
                    mChatFragment.showMsg(chatMsgInfos);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mChatFragment.showTip(text);
                }
            });
        }
    }

    @Override
    public void loadMsgAfterTime(long time) {
        User user = CacheRepository.getInstance().who();
        if(mFriendView != null && user != null){
            mRepository.findMsgRelateAfterTime(user.getId(), user.getVerification(), mFriendView.getFriendId(), time, new HttpBaseCallback(){
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    mChatFragment.clearMsg();
                    initChatInfo(chatMsgInfos);
                    mChatFragment.showMsg(chatMsgInfos);
                }
                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mChatFragment.showTip(text);
                }
            });
        }
    }

    @Override
    public void loadMsgBeforeTime(long time) {
        User user = CacheRepository.getInstance().who();
        if(mFriendView != null && user != null){
            mRepository.findMsgRelateBeforeTime(user.getId(), user.getVerification(), mFriendView.getFriendId(), time, new HttpBaseCallback(){
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    mChatFragment.clearMsg();
                    initChatInfo(chatMsgInfos);
                    mChatFragment.showMsg(chatMsgInfos);
                }
                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mChatFragment.showTip(text);
                }
            });
        }
    }
}
