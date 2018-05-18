package com.baige.telephone;



import com.baige.data.entity.FriendView;
import com.baige.data.source.Repository;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/29.
 */

public class PhonePresenter implements PhoneContract.Presenter {

    private final static String TAG = PhonePresenter.class.getCanonicalName();

    private PhoneFragment mPhonefragment;

    private Repository mRepository;

    private FriendView mFriendView;


    public PhonePresenter(Repository repository, PhoneFragment phoneFragment) {
        mRepository = checkNotNull(repository);
        mPhonefragment = checkNotNull(phoneFragment);
        mPhonefragment.setPresenter(this);
    }

    public FriendView getFriendView() {
        return mFriendView;
    }

    public void setFriendView(FriendView friendView) {
        this.mFriendView = friendView;
    }

    @Override
    public void start() {
        if(mFriendView != null){
            mPhonefragment.showFriend(mFriendView);
        }
    }


    @Override
    public void stop() {

    }

    @Override
    public void onHangUp() {
    }


    @Override
    public void onPickUp() {
        //TODO 远程服务器执行接听指令，正确连接之后修改状态

    }

}
