package com.baige.search;


import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;


/**
 * Created by baige on 2017/12/26.
 */

public class SearchActivity extends BaseActivity {

    private Toolbar mToolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("添加好友");
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

        SearchFragment searchFragment  = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(searchFragment == null){
            searchFragment = SearchFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), searchFragment, R.id.content_frame);
        }
        SearchPresenter searchPresenter = new SearchPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), searchFragment);
    }
}
