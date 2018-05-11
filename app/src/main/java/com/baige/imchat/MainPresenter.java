package com.baige.imchat;

import com.baige.BaseActivity;
import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;
import com.baige.util.UploadUtil;

import java.io.File;

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
        if(user != null){
            mFragment.showUserName(user.getName());
            mFragment.showUserAlias(user.getAlias());
            mFragment.showUserImg(user.getImgName());
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
        User user = CacheRepository.getInstance().who();
        File file = new File(fileName);
        mRepository.changeHeadImg(user.getId(), user.getVerification(), file, new HttpBaseCallback(){
            @Override
            public void success() {
                super.success();
                mFragment.showTip("成功");
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
                public void downloadFinish(String fileName) {
                    super.downloadFinish(fileName);
                    mFragment.showUserImg(fileName);
                }
            });
        }
    }
}
