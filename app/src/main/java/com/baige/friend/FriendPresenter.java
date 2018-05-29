package com.baige.friend;


import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;

import com.baige.common.Parm;
import com.baige.common.State;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.BaseObserver;
import com.baige.data.observer.ChatMessageObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.observer.LastChatMessageObservable;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;


import java.util.Observable;
import java.util.Observer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class FriendPresenter implements FriendContract.Presenter {
    private final static String TAG = FriendPresenter.class.getCanonicalName();

    private Repository mRepository;

    private FriendFragment mFragment;

    private FriendView mFriendView;

    public FriendPresenter(Repository repository, FriendFragment loginFragment) {
        mRepository = checkNotNull(repository);
        mFragment = checkNotNull(loginFragment);
        mFragment.setPresenter(this);
    }


    public void setFriendView(FriendView friendView){
        this.mFriendView = friendView;
    }
    @Override
    public void start() {
        CacheRepository.getInstance().registerDataChange(dataObserver);
        if(mFriendView != null){
            mFragment.showFriendView(mFriendView);
        }
    }

    @Override
    public FriendView getFriend() {
        return mFriendView;
    }

    @Override
    public void updateFriendAlias(final String text) {
        if(!Tools.isEmpty(text) && !Tools.isEquals(text, mFriendView.getFriendAlias())){
            User user = CacheRepository.getInstance().who();
            if(user != null && !Tools.isEmpty(user.getVerification())){
                mRepository.changeFriendAlias(mFriendView.getId(), user.getId(), user.getVerification(), text, new HttpBaseCallback(){
                    @Override
                    public void success() {
                        super.success();
                        mFriendView.setFriendAlias(text);
                    }

                    @Override
                    public void fail() {
                        super.fail();
                        mFragment.showTip("更新好友备注失败！");
                    }

                    @Override
                    public void meaning(String text) {
                        super.meaning(text);
                        mFragment.showTip(text);
                    }
                });
            }
        }
    }

    @Override
    public void downloadImg(String imgName) {
        if(!Tools.isEmpty(imgName)){
            mRepository.downloadImg(imgName, new HttpBaseCallback(){
                @Override
                public void downloadFinish(String remark, String fileName) {
                    super.downloadFinish(remark, fileName);
                    mFragment.showFriendImg(fileName);
                }
            });
        }
    }

    @Override
    public void addFriend() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.relateUser(user.getId(),  user.getVerification(), mFriendView.getFriendId(), new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_WAITING);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("添加失败");
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
    public void agree() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.operationFriend(mFriendView.getId(), user.getId(),  user.getVerification(), mFriendView.getFriendId(), "agree", new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_FRIEND);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("操作失败");
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
    public void reject() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.operationFriend(mFriendView.getId(), user.getId(),  user.getVerification(), mFriendView.getFriendId(), "reject", new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_STRANGE);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("操作失败");
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
    public void deleteFriend() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.operationFriend(mFriendView.getId(), user.getId(),  user.getVerification(), mFriendView.getFriendId(), "delete", new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_STRANGE);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("操作失败");
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
    public void defriend() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.operationFriend(mFriendView.getId(), user.getId(),  user.getVerification(), mFriendView.getFriendId(), "defriend", new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_DEFRIEND);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("操作失败");
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
    public void cancelDefriend() {
        User user = CacheRepository.getInstance().who();
        if(user != null && !Tools.isEmpty(user.getVerification()) && mFriendView != null){
            mRepository.operationFriend(mFriendView.getId(), user.getId(),  user.getVerification(), mFriendView.getFriendId(), "cancel_defriend", new HttpBaseCallback(){
                @Override
                public void success() {
                    super.success();
                    mFriendView.setState(State.RELATETION_STRANGE);
                    CacheRepository.getInstance().getFriendViewObservable().put(mFriendView);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("操作失败");
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
        CacheRepository.getInstance().unRegisterDataChange(dataObserver);
    }
    private BaseObserver dataObserver = new BaseObserver() {
        @Override
        public void update(FriendViewObservable observable, Object arg) {
            mFriendView = observable.get(mFriendView.getId());
            mFragment.showFriendView(mFriendView);
            super.update(observable, arg);
        }
    };
}
