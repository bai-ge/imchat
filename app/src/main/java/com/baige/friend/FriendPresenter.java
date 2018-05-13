package com.baige.friend;


import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;

import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;



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
        if(mFriendView != null){
            mFragment.showFriendView(mFriendView);
        }
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
                public void downloadFinish(String fileName) {
                    super.downloadFinish(fileName);
                    mFragment.showFriendImg(fileName);
                }
            });
        }
    }
}
