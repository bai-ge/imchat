package com.baige.register;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.util.Tools;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class RegisterPresenter implements RegisterContract.Presenter {
    private final static String TAG = RegisterPresenter.class.getCanonicalName();

    private RegisterFragment mFragment;

    private Repository mRepository;

    public RegisterPresenter(Repository instance, RegisterFragment registerFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(registerFragment);
        registerFragment.setPresenter(this);
    }



    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void register(final User user, String code) {
        mRepository.register(user, new HttpBaseCallback(){
            @Override
            public void meaning(String text) {
                super.meaning(text);
                mFragment.showTip(text);
            }

            @Override
            public void success() {
                super.success();
                mFragment.delayBack(2000);
            }

            @Override
            public void fail() {
                super.fail();
            }

            @Override
            public void loadAUser(User user) {
                super.loadAUser(user);
                //TODO 保存用户数据
            }
        });
    }

    @Override
    public void register(String name, String psw) {
        User user = new User();
        user.setName(name);
        user.setPassword(Tools.MD5(psw));
        register(user, "");
    }
}
