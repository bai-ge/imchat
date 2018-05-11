package com.baige.data.source;

import android.support.annotation.NonNull;

import com.baige.callback.HttpBaseCallback;
import com.baige.callback.SimpleResponseBinder;
import com.baige.callback.UserResponseBinder;
import com.baige.data.entity.User;
import com.baige.data.source.local.LocalRepository;
import com.baige.data.source.remote.RemoteRepository;
import com.baige.data.source.remote.ServerHelper;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/4.
 */

public class Repository implements DataSource, ServerHelper{

    private final static String TAG = Repository.class.getCanonicalName();

    private static Repository INSTANCE;

    //本地仓库（数据库）
    private LocalRepository mLocalRepository;

    //远程数据仓库（连接远程服务器）
    private RemoteRepository mRemoteRepository;

    private static ExecutorService fixedThreadPool = null;

    private SimpleResponseBinder mSimpleResponseBinder;

    private UserResponseBinder mUserResponseBinder;

    private Repository(LocalRepository localRepository) {
        mLocalRepository =  checkNotNull(localRepository);
        mRemoteRepository = RemoteRepository.getInstance(localRepository);
        mSimpleResponseBinder = new SimpleResponseBinder();
        mUserResponseBinder = new UserResponseBinder();
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
    }

    public static Repository getInstance(@NonNull LocalRepository localRepository) {
        if (INSTANCE == null) {
            synchronized (Repository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new Repository(localRepository);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void login(final User user, final HttpBaseCallback callback) {
        checkNotNull(user);
        checkNotNull(callback);
        callback.setResponseBinder(mUserResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.login(user, callback);
                }
            });
        }else{
            callback.fail();
        }
    }

    @Override
    public void register(final User user, final HttpBaseCallback callback) {
        checkNotNull(user);
        checkNotNull(callback);
        callback.setResponseBinder(mUserResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.register(user, callback);
                }
            });
        }else{
            callback.fail();
        }
    }

    @Override
    public void updateAlias(final int id, final String verification, final String alias, final HttpBaseCallback callback) {
        checkNotNull(verification);
        checkNotNull(callback);
        callback.setResponseBinder(mSimpleResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.updateAlias(id, verification, alias, callback);
                }
            });
        }else{
            callback.fail();
        }
    }

    @Override
    public void uploadFile(final User user, final String file, final HttpBaseCallback callback) {
        checkNotNull(user);
        checkNotNull(file);
        checkNotNull(callback);
        callback.setResponseBinder(mSimpleResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.uploadFile(user, file, callback);
                }
            });
        }else{
            callback.fail();
        }
    }

    @Override
    public void changeHeadImg(final int id, final String verification, final File headImg, final HttpBaseCallback callback) {
        checkNotNull(verification);
        checkNotNull(headImg);
        checkNotNull(callback);
        callback.setResponseBinder(mSimpleResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.changeHeadImg(id, verification, headImg, callback);
                }
            });
        }else{
            callback.fail();
        }
    }

    @Override
    public void downloadFile(final String url, final String path, final String fileName, final HttpBaseCallback callback) {
        checkNotNull(url);
        checkNotNull(path);
        checkNotNull(fileName);
        checkNotNull(callback);
        callback.setResponseBinder(mSimpleResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.downloadFile(url, path, fileName, callback);
                }
            });
        }else{
            callback.fail(fileName);
        }
    }

    @Override
    public void downloadImg(final String imgName, final HttpBaseCallback callback) {
        checkNotNull(imgName);
        checkNotNull(callback);
        callback.setResponseBinder(mSimpleResponseBinder);
        if (fixedThreadPool != null) {
            fixedThreadPool.submit(new Runnable() {
                @Override
                public void run() {
                    mRemoteRepository.downloadImg(imgName, callback);
                }
            });
        }else{
            callback.fail(imgName);
        }
    }
}
