package com.baige.login;


import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;

import java.io.File;

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
        if (user != null) {
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
        user.setDeviceId(CacheRepository.getInstance().getDeviceId());
        login(user, psw);
    }

    @Override
    public void login(User user, final String psw) {
        mRepository.login(user, new HttpBaseCallback() {
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
                final User me = CacheRepository.getInstance().who();
                //同一名用户
                if (me != null && Tools.isEquals(me.getName(), user.getName())) {
                    //上传本地图片
                    if (Tools.isEmpty(user.getImgName()) && !Tools.isEmpty(me.getImgName())) {
                        user.setImgName(me.getImgName());
                        File imgFile = new File(BaseApplication.headImgPath, me.getImgName());
                        if (imgFile.exists()) {
                            mRepository.changeHeadImg(me.getId(), user.getVerification(), imgFile, new HttpBaseCallback() {
                                @Override
                                public void uploadFinish(String fileName) {
                                    super.uploadFinish(fileName);
                                }
                            });
                        }
                    }
                }
                CacheRepository.getInstance().setYouself(user);
                CacheRepository.getInstance().setLogin(true);
                CacheRepository.getInstance().saveConfig(BaseApplication.getAppContext());
                mFragment.onBack();
            }
        });
    }

    @Override
    public void stop() {

    }
}
