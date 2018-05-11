package com.baige.login;




import android.content.Intent;
import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/12/22.
 */

public class LoginPresenter implements LoginContract.Presenter {
    private final static String TAG = LoginPresenter.class.getCanonicalName();

    private Repository mRepository;

    private LoginFragment mFragment;

    public LoginPresenter(Repository repository, LoginFragment loginFragment) {
        mRepository = checkNotNull(repository);
        mFragment = checkNotNull(loginFragment);
        mFragment.setPresenter(this);
    }


    @Override
    public void start() {
        User user = CacheRepository.getInstance().who();
        if(user != null){
            mFragment.showName(user.getName());
            mFragment.setPsw(user.getPassword());
        }
    }

    @Override
    public void login(final String name, String psw) {
        //TODO
        User user = new User();
        user.setName(name);
        user.setPassword(Tools.MD5(psw));
        user.setDeviceId(Tools.ramdom());
        login(user, psw);
    }

    @Override
    public void login(User user, final String psw) {
        mRepository.login(user, new HttpBaseCallback(){
            @Override
            public void meaning(String text) {
                super.meaning(text);
                mFragment.showTip(text);
            }

            @Override
            public void success() {
                super.success();
            }

            @Override
            public void fail() {
                super.fail();
                mFragment.showTip("登录失败");
            }

            @Override
            public void loadAUser(User user) {
                super.loadAUser(user);
                //TODO 保存用户数据
                Log.d(TAG, user.toString());
                user.setPassword(psw);
                CacheRepository.getInstance().setYouself(user);
                CacheRepository.getInstance().setLogin(true);
                CacheRepository.getInstance().saveConfig(BaseApplication.getAppContext());
                mFragment.onBack();
            }
        });
    }
}
