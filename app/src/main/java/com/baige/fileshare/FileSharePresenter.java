package com.baige.fileshare;

import android.os.Environment;
import android.util.Log;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.data.entity.FileView;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.observer.BaseObserver;
import com.baige.data.observer.FileViewObservable;
import com.baige.data.observer.FriendViewObservable;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.FileUtils;
import com.baige.util.Tools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/5.
 */

public class FileSharePresenter implements FileShareContract.Presenter {
    private final static String TAG = FileSharePresenter.class.getSimpleName();

    private Repository mRepository;

    private FileShareContract.View mFragment;


    private Stack<String> mLoadPathHistory = new Stack<>();

    public FileSharePresenter(Repository instance, FileShareFragment fileListFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(fileListFragment);
        mFragment.setPresenter(this);
    }

    @Override
    public void start() {
        List<FileView> list = CacheRepository.getInstance().getFileViewObservable().loadCache();
        initFileView(list);
        mFragment.showFileViews(list);
        CacheRepository.getInstance().registerDataChange(observer);
        searchFiles();
    }

    @Override
    public void searchFiles() {
        mRepository.searchAllFile(new HttpBaseCallback(){
            @Override
            public void meaning(String text) {
                super.meaning(text);
                mFragment.showTip(text);
            }

            @Override
            public void fail() {
                super.fail();
                mFragment.showTip("加载失败");
            }

            @Override
            public void loadFiles(List<FileView> fileViews) {
                super.loadFiles(fileViews);
                Log.d(TAG, "文件个数："+fileViews.size());
                CacheRepository.getInstance().getFileViewObservable().put(fileViews);
            }

            @Override
            public void loadFile(FileView fileView) {
                super.loadFile(fileView);
                CacheRepository.getInstance().getFileViewObservable().put(fileView);
            }
        });
    }

    private void initFileView(List<FileView> list) {
        for (FileView fileView : list) {
            initFileView(fileView);
        }
    }

    private void initFileView(FileView fileView) {
        User user = CacheRepository.getInstance().who();
        FriendViewObservable friendViewObservable = CacheRepository.getInstance().getFriendViewObservable();
        if (fileView != null) {
            if (fileView.getUserId() == user.getId()) {
                fileView.setUserName("我");
            } else {
                FriendView friendView = friendViewObservable.get(fileView.getId());
                if (friendView != null) {
                    fileView.setUserName(friendView.getSuitableName());
                }
            }
        }
    }

    @Override
    public void stop() {
        CacheRepository.getInstance().unRegisterDataChange(observer);
    }

    private BaseObserver observer = new BaseObserver() {
        @Override
        public void update(FileViewObservable observable, Object arg) {
            super.update(observable, arg);
            List<FileView> list = observable.loadCache();
            initFileView(list);
            mFragment.showFileViews(list);
        }
    };
}
