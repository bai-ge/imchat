package com.baige.chat;


import android.support.annotation.NonNull;
import android.util.Log;

import com.baige.callback.HttpBaseCallback;
import com.baige.common.Parm;
import com.baige.common.State;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.PullService;
import com.baige.util.Tools;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by 百戈 on 2017/2/19.
 */

public class ChatPresenter implements ChatContract.Presenter {

    private final static String TAG = ChatPresenter.class.getSimpleName();

    private final Repository mRepository;

    private final ChatFragment mChatFragment;

    private FriendView mFriendView;

    private PullService pullService;


    public ChatPresenter(@NonNull Repository dateRepository, @NonNull ChatFragment chatWithMeFragment) {
        this.mRepository = checkNotNull(dateRepository);
        this.mChatFragment = checkNotNull(chatWithMeFragment);
        this.pullService = new PullService(dateRepository);
        mChatFragment.setPresenter(this);
    }

    @Override
    public void start() {
        if (mFriendView != null) {
            mChatFragment.showFriendName(mFriendView.getSuitableName());
        }
        Log.d(TAG, "friend =" + mFriendView);
        List<ChatMsgInfo> chats = filter(CacheRepository.getInstance().getChatMessageObservable().loadCache());
        mChatFragment.showMsg(chats);
        CacheRepository.getInstance().getChatMessageObservable().addObserver(chatMsgObserver);
        if (chats.size() > 0) {
            Collections.sort(chats);
            loadMsgAfterTime(chats.get(chats.size() - 1).getSendTime());
        } else {
            loadMsg();
        }
        readBeforeTime();
        pullService.onStart();
    }

    @Override
    public void stop() {
        readBeforeTime();
        pullService.onDestroy();
        CacheRepository.getInstance().getChatMessageObservable().remote(chatMsgObserver);
    }

    public FriendView getFriendView() {
        return mFriendView;
    }

    public void setFriendView(FriendView friendView) {
        this.mFriendView = friendView;
    }

    public void initChatInfo(List<ChatMsgInfo> chatMsgInfos) {
        for (ChatMsgInfo ch : chatMsgInfos) {
            initChatInfo(ch);
        }
    }

    public void initChatInfo(ChatMsgInfo chatMsgInfo) {
        if (chatMsgInfo != null) {
            int sendId = chatMsgInfo.getSenderId();
            User user = CacheRepository.getInstance().who();
            if (sendId == user.getId()) {
                chatMsgInfo.setShowType(Parm.MSG_IS_SEND);
                chatMsgInfo.setUserName("我");
                chatMsgInfo.setUserImgName(user.getImgName());
            } else {
                chatMsgInfo.setShowType(Parm.MSG_IS_RECEIVE);
                chatMsgInfo.setUserName(mFriendView.getSuitableName());
                chatMsgInfo.setUserImgName(mFriendView.getFriendImgName());
            }
        }
    }

    @Override
    public void sendMsg(String msg) {
        if (!Tools.isEmpty(msg)) {
            User user = CacheRepository.getInstance().who();
            final ChatMsgInfo chatMsgInfo = new ChatMsgInfo("我", msg, Parm.MSG_TYPE_TEXT);
            chatMsgInfo.setReceiveId(mFriendView.getFriendId());
            chatMsgInfo.setSenderId(user.getId());

            chatMsgInfo.setUserImgName(user.getImgName());
            chatMsgInfo.setShowType(Parm.MSG_IS_SEND);
            chatMsgInfo.setSendState(Parm.MSG_IS_SENDING);
            chatMsgInfo.setRemark(Tools.ramdom());
            CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfo);


            mRepository.sendMsg(chatMsgInfo, user.getVerification(), new HttpBaseCallback() {
                @Override
                public void success() {
                    super.success();
                    chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                    mChatFragment.notifyChange();
                }

                @Override
                public void loadMsg(ChatMsgInfo responseChat) {
                    super.loadMsg(chatMsgInfo);
                    chatMsgInfo.setSendTime(responseChat.getSendTime());
                    chatMsgInfo.setId(responseChat.getId());
                    chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                    mChatFragment.notifyChange();
                }
            });
        }
    }

    @Override
    public void reSendMsg(final ChatMsgInfo chatMsgInfo) {
        User user = CacheRepository.getInstance().who();
        chatMsgInfo.setSendState(Parm.MSG_IS_SENDING);
        chatMsgInfo.setSendTime(System.currentTimeMillis());
        CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfo);
        mRepository.sendMsg(chatMsgInfo, user.getVerification(), new HttpBaseCallback() {
            @Override
            public void success() {
                super.success();
                chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                mChatFragment.notifyChange();
            }

            @Override
            public void loadMsg(ChatMsgInfo responseChat) {
                super.loadMsg(chatMsgInfo);
                chatMsgInfo.setSendTime(responseChat.getSendTime());
                chatMsgInfo.setId(responseChat.getId());
                chatMsgInfo.setSendState(Parm.MSG_IS_SEND_SUCESS);
                mChatFragment.notifyChange();
            }
        });
    }

    @Override
    public void loadMsg() {
        User user = CacheRepository.getInstance().who();
        if (mFriendView != null && user != null) {
            mRepository.findMsg(user.getId(), user.getVerification(),  new HttpBaseCallback() {
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    Collections.sort(chatMsgInfos);
                    CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfos);
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
        if ( user != null) {
            mRepository.findMsgAfterTime(user.getId(), user.getVerification(), time, new HttpBaseCallback() {
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    Collections.sort(chatMsgInfos);
                    CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfos);
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
        if (mFriendView != null && user != null) {
            mRepository.findMsgBeforeTime(user.getId(), user.getVerification(), time, new HttpBaseCallback() {
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    Collections.sort(chatMsgInfos);
                    CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfos);
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
    public void readBeforeTime() {
        User user = CacheRepository.getInstance().who();
        List<ChatMsgInfo> chatMsgInfos = CacheRepository.getInstance().getChatMessageObservable().loadCache();
        long time = System.currentTimeMillis();
        if(chatMsgInfos != null &&chatMsgInfos.size() > 0){
            Collections.sort(chatMsgInfos);
            time = chatMsgInfos.get(chatMsgInfos.size() - 1).getSendTime();
        }
        for (ChatMsgInfo chat : chatMsgInfos){
            if(chat.isReceive() && chat.getReceiveId() == mFriendView.getFriendId() && chat.getSendTime() <= time){
                chat.setContextState(State.MSG_STATE_READED);
            }
        }
        CacheRepository.getInstance().getChatMessageObservable().notifyObservers();

        mRepository.readMsgBeforeTime(user.getId(), user.getVerification(), mFriendView.getFriendId(), time, new HttpBaseCallback(){
            @Override
            public void fail() {
                super.fail();
                mChatFragment.showTip("未知错误");
            }
        });
    }

    private boolean isNeed(ChatMsgInfo chatMsgInfo) {
        if (mFriendView != null && chatMsgInfo != null) {
            return (mFriendView.getUid() == chatMsgInfo.getSenderId()
                    && mFriendView.getFriendId() == chatMsgInfo.getReceiveId())
                    || (mFriendView.getUid() == chatMsgInfo.getReceiveId()
                    && mFriendView.getFriendId() == chatMsgInfo.getSenderId());
        }
        return false;
    }

    private List<ChatMsgInfo> filter(List<ChatMsgInfo> list) {
        ArrayList<ChatMsgInfo> chats = new ArrayList<>();
        for (ChatMsgInfo c : list) {
            if (isNeed(c)) {
                chats.add(c);
            }
        }
        return chats;
    }

    private Observer chatMsgObserver = new Observer() {
        @Override
        public void update(Observable o, Object arg) {
            Log.d(TAG, "update" + arg);
            if (o instanceof ChatMessageObservable) {
                ChatMessageObservable chatMessageObservable = (ChatMessageObservable) o;
                if (arg == null) {
                    List<ChatMsgInfo> chatMsgInfos = filter(chatMessageObservable.loadCache());
                    initChatInfo(chatMsgInfos);
                    mChatFragment.showMsg(chatMsgInfos);
                } else {
                    Log.d(TAG, "2update");
                    if (arg instanceof List) { //arg.getClass().isArray() 或arg instanceof List
                        List<ChatMsgInfo> chatMsgInfos = filter((List<ChatMsgInfo>) arg);
                        initChatInfo(chatMsgInfos);
                        Log.d(TAG, "3update");
                        mChatFragment.addMsg(chatMsgInfos);
                        Log.d(TAG, "4update");
                    } else {
                        Log.d(TAG, "5update");
                        ChatMsgInfo chatMsgInfo = (ChatMsgInfo) arg;
                        if (isNeed(chatMsgInfo)) {
                            Log.d(TAG, "6update");
                            mChatFragment.showMsg(chatMsgInfo);
                        }
                    }
                }
            }
        }
    };

}
