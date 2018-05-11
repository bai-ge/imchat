package com.baige.filelist;

import android.Manifest;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.baige.BaseActivity;
import com.baige.adapter.FileInfoAdapter;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;

import java.util.List;

/**
 * Created by baige on 2018/5/5.
 */

public class FileListActivity extends BaseActivity {

    private Toolbar mToolbar;

    private FileListPresenter mFileListPresenter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_toolbar_commmon);

        String [] permissison = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestRunTimePermission(permissison, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onGranted(List<String> grantedPermission) {//同意

            }

            @Override
            public void onDenied(List<String> deniedPermission) {//拒绝
                StringBuffer buffer = new StringBuffer();
                buffer.append("您已拒绝了以下权限:");
                for (int i = 0; i < deniedPermission.size(); i++) {
                    buffer.append("\n"+deniedPermission);
                }
                showTip(buffer.toString());
            }
        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("文件浏览");
        //为activity窗口设置活动栏
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        //设置返回图标
        actionBar.setHomeAsUpIndicator(0);
        actionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileListActivity.super.onBackPressed();
            }
        });

        FileListFragment fileListFragment  = (FileListFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(fileListFragment == null){
            fileListFragment = FileListFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), fileListFragment, R.id.contentFrame);
        }
        mFileListPresenter = new FileListPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), fileListFragment);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        mFileListPresenter.rebackPack();
    }
}
