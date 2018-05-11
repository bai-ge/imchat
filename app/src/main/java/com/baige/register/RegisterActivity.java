package com.baige.register;

import android.os.Bundle;
import android.view.KeyEvent;

import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;


public class RegisterActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);


        RegisterFragment registerFragment  = (RegisterFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(registerFragment == null){
            registerFragment = RegisterFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), registerFragment, R.id.content_frame);
        }
        RegisterPresenter registerPresenter = new RegisterPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), registerFragment);

    }

    private long clickBackTime = 0;
    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode== KeyEvent.KEYCODE_BACK){
            long now = System.currentTimeMillis();
            if(now - clickBackTime < 800){
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
