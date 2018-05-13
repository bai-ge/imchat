package com.baige.login;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;

import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;


/**
 * Created by baige on 2017/12/22.
 */

public class LoginActivity extends BaseActivity {




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);

        LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(loginFragment == null){
            loginFragment = LoginFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), loginFragment, R.id.content_frame);
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        LoginPresenter loginPresenter = new LoginPresenter(repository, loginFragment);
        Log.d("guide_page", "启动登录界面");
    }

    private long clickBackTime = 0;
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            long now = System.currentTimeMillis();
            if(now - clickBackTime < 500){
                finishAll();
            }else{
                clickBackTime = now;
                showTip("再按一次返回键退出");
            }
            return true;//不执行父类点击事件
        }
        return super.onKeyDown(keyCode, event);//继续执行父类其他点击事件
    }
}
