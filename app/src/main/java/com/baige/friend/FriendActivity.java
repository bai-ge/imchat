package com.baige.friend;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;


import com.baige.BaseActivity;
import com.baige.data.entity.FriendView;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;


/**
 * Created by baige on 2017/12/22.
 */

public class FriendActivity extends BaseActivity {

    private Toolbar mToolbar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("好友信息");
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FriendFragment loginFragment = (FriendFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(loginFragment == null){
            loginFragment = FriendFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), loginFragment, R.id.content_frame);
        }
        Repository repository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        FriendPresenter friendPresenter = new FriendPresenter(repository, loginFragment);

        if(getIntent().getExtras().containsKey("friend")){
            FriendView friendView = getIntent().getExtras().getParcelable("friend");
            if(friendView != null){
                friendPresenter.setFriendView(friendView);
            }
        }
    }
}
