package com.baige.imchat;

import android.util.Log;
import android.widget.ListView;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.BaseObserver;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.observer.LastChatMessageObservable;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/4.
 */

public class MainPresenter implements MainContract.Presenter {

    private final static String TAG = MainPresenter.class.getSimpleName();

    private Repository mRepository;

    private MainContract.View mFragment;

    public MainPresenter(Repository instance, MainFragment mainFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(mainFragment);
        mFragment.setPresenter(this);
    }

    @Override
    public void start() {
        User user = CacheRepository.getInstance().who();
        Log.d(TAG, "开始"+user);
        if(user != null){
            mFragment.showUserName(user.getName());
            mFragment.showUserAlias(user.getAlias());
//            这里显示会引起界面未初始化完成
            mFragment.showUserImg(user.getImgName());

            loadFriends();
            loadMsg();
            CacheRepository.getInstance().registerDataChange(dataObserver);
            List<FriendView> friendViewList = CacheRepository.getInstance().getFriendViewObservable().loadCache();
            List<ChatMsgInfo> chatMsgInfos = CacheRepository.getInstance().getChatMessageObservable().loadCache();
            CacheRepository.getInstance().getLastChatMessageObservable().analyze(chatMsgInfos, user.getId());
            CacheRepository.getInstance().getLastChatMessageObservable().analyze(friendViewList);
        }

    }

    @Override
    public void updateAlias(final String text) {
        final User user = CacheRepository.getInstance().who();
        if(user != null){
            if(Tools.isEquals(user.getAlias(), text)){
                return;
            }
            mRepository.updateAlias(user.getId(), user.getVerification(), text, new HttpBaseCallback(){
                @Override
                public void fail() {
                    super.fail();
                }

                @Override
                public void success() {
                    super.success();
                    user.setAlias(text);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }
            });
        }else{
            mFragment.showTip("您未登录，请先完成登录！");
        }
    }

    @Override
    public void changeImg(String fileName) {
        mFragment.showTip("开始上传文件"+fileName);
        final User user = CacheRepository.getInstance().who();
        final File file = new File(fileName);
        mRepository.changeHeadImg(user.getId(), user.getVerification(), file, new HttpBaseCallback(){
            @Override
            public void success() {
                super.success();
                mFragment.showTip("成功");
                user.setImgName(file.getName());
            }

            @Override
            public void fail() {
                super.fail();
            }

            @Override
            public void meaning(String text) {
                super.meaning(text);
                mFragment.showTip(text);
            }
        });
    }

    @Override
    public void downloadImg(String imgName) {
        if(!Tools.isEmpty(imgName)){
            mRepository.downloadImg(imgName, new HttpBaseCallback(){
                @Override
                public void downloadFinish(String remark, String fileName) {
                    super.downloadFinish(remark, fileName);
                    mFragment.showUserImg(fileName);
                }
            });
        }
    }

    @Override
    public void loadFriends() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification())){
            mRepository.searchFriend(user.getId(), user.getVerification(), new HttpBaseCallback(){
                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("加载好友失败");
                }

                @Override
                public void loadFriendViews(List<FriendView> list) {
                    super.loadFriendViews(list);
                    CacheRepository.getInstance().getFriendViewObservable().put(list);
                }
            });
        }

    }

    @Override
    public void loadMsg() {
        User user = CacheRepository.getInstance().who();
        if (user != null) {
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
                    mFragment.showTip(text);
                }
            });
        }
    }

    @Override
    public void stop() {
        CacheRepository.getInstance().getChatMessageObservable().remote(dataObserver);
    }

    private BaseObserver dataObserver = new BaseObserver() {

        @Override
       public void update(ChatMessageObservable observable, Object arg){
            Log.d(TAG, "消息："+arg);
            User user = CacheRepository.getInstance().who();
            List<ChatMsgInfo> chatMsgInfos = observable.loadCache();
            CacheRepository.getInstance().getLastChatMessageObservable().analyze(chatMsgInfos, user.getId());
        }

        @Override
        public void update(FriendViewObservable observable, Object arg){
            Log.d(TAG, "好友："+arg);
            mFragment.showFriends(observable.loadCache());
            CacheRepository.getInstance().getLastChatMessageObservable().analyze( observable.loadCache());
        }

        @Override
        public void update(LastChatMessageObservable observable, Object arg){
            Log.d(TAG, "最近消息："+arg);
            mFragment.showLastChatMsgs(observable.loadCache());
        }
    };
}
