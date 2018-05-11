package com.baige.telephone;



import com.baige.data.source.Repository;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/29.
 */

public class PhonePresenter implements PhoneContract.Presenter {

    private final static String TAG = PhonePresenter.class.getCanonicalName();
    private PhoneFragment mPhonefragment;
    private Repository mRepository;


    public PhonePresenter(Repository repository, PhoneFragment phoneFragment) {
        mRepository = checkNotNull(repository);
        mPhonefragment = checkNotNull(phoneFragment);
        mPhonefragment.setPresenter(this);

    }


    @Override
    public void start() {

    }



    @Override
    public void onHangUp() {
    }


    @Override
    public void onPickUp() {
        //TODO 远程服务器执行接听指令，正确连接之后修改状态

    }

}
