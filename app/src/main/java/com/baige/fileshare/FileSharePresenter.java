package com.baige.fileshare;

import android.os.Environment;
import android.util.Log;

import com.baige.callback.HttpBaseCallback;
import com.baige.connect.NetServerManager;
import com.baige.connect.msg.MessageManager;
import com.baige.connect.msg.MessageManagerOfFile;
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
import com.baige.p2pcore.ConnectSession;
import com.baige.p2pcore.ConnectorManager;
import com.baige.p2pcore.FileReceiverSession;
import com.baige.p2pcore.FileSenderSession;
import com.baige.pushcore.SendMessageBroadcast;
import com.baige.util.FileUtils;
import com.baige.util.Tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/5.
 */

public class FileSharePresenter implements FileShareContract.Presenter {
    private final static String TAG = FileSharePresenter.class.getSimpleName();

    private Repository mRepository;

    private FileShareContract.View mFragment;

    private boolean isShowOwnFile;


    private Stack<String> mLoadPathHistory = new Stack<>();

    public FileSharePresenter(Repository instance, FileShareFragment fileListFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(fileListFragment);
        mFragment.setPresenter(this);
    }

    @Override
    public void start() {
        List<FileView> list = CacheRepository.getInstance().getFileViewObservable().loadCache();
        List<FileView> showList = filter(list);
        initFileView(showList);
        mFragment.showFileViews(showList);
        CacheRepository.getInstance().registerDataChange(observer);
        searchFiles();
    }

    public boolean isShowOwnFile() {
        return isShowOwnFile;
    }

    public void setShowOwnFile(boolean showOwnFile) {
        isShowOwnFile = showOwnFile;
    }

    public List<FileView> filter(List<FileView> fileViews){
        if(isShowOwnFile){
            User user = CacheRepository.getInstance().who();
            List<FileView> update = new ArrayList<>();
            for (FileView file: fileViews) {
                if(file.getUserId() == user.getId()){
                    update.add(file);
                }
            }
            return update;
        }else{
            return fileViews;
        }

    }

    @Override
    public void searchFiles() {
        mRepository.searchAllFile(new HttpBaseCallback() {
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
                Log.d(TAG, "文件个数：" + fileViews.size());
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
                FriendView friendView = friendViewObservable.get(fileView.getUserId());
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
            List<FileView> showList = filter(list);
            initFileView(showList);
            mFragment.showFileViews(showList);
        }
    };

    @Override
    public void downloadFiles(List<FileView> fileViews) {
        for (FileView f : fileViews) {
            if (f.isRemote()) {
                //TODO 从服务器下载
                downloadFileFromServer(f);
            } else {
                //从用户本地下载
                downloadFile(f);
            }
        }
    }

    public void downloadFileFromServer(final FileView fileView){
        if(fileView != null){
            fileView.setShowProgress(true);
            if(fileView.getProgressPercent() == 1){
                fileView.setProgressPercent(0);
            }
            CacheRepository.getInstance().getFileViewObservable().put(fileView);
            mRepository.downloadFile(fileView.getRemark(), fileView.getId(), fileView.getFileName(), new HttpBaseCallback(){
                @Override
                public void downloadFinish(String remark, String fileName) {
                    super.downloadFinish(remark, fileName);
                    mFragment.showTip("下载完成"+fileName);
                    FileView f = CacheRepository.getInstance().getFileViewObservable().get(remark);
                    if(f != null){
//                        f.setShowProgress(false);
                        f.setProgressPercent(1);
                        CacheRepository.getInstance().getFileViewObservable().put(f);
                        updateDownloadCount(f);
                    }

                }

                @Override
                public void progress(String remark, String fileName, long finishSize, long totalSize) {
                    super.progress(remark, fileName, finishSize, totalSize);
                    FileView f = CacheRepository.getInstance().getFileViewObservable().get(remark);
                    if(f != null){
//                        f.setShowProgress(false);
                        f.setProgressPercent((float) (finishSize * 1.0 / f.getFileSize()));
                        CacheRepository.getInstance().getFileViewObservable().put(f);
                    }
                    Log.d(TAG, finishSize + ":"+f.getFileSize()+", ="+(finishSize * 1.0 / f.getFileSize()));
                }

                @Override
                public void fail(String remark, String fileName) {
                    super.fail(remark, fileName);
                    FileView f = CacheRepository.getInstance().getFileViewObservable().get(remark);
                    if(f != null){
//                        f.setShowProgress(false);
                    }
                    mFragment.showTip( "下载失败"+fileName);
                }

                @Override
                public void fail() {
                    super.fail();
                    mFragment.showTip("下载失败");
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                    mFragment.showTip(text);
                }

                @Override
                public void error(String remark, String fileName, Exception e) {
                    super.error(remark, fileName, e);
                    FileView f = CacheRepository.getInstance().getFileViewObservable().get(remark);
                    if(f != null){
//                        f.setShowProgress(false);
                    }
                    mFragment.showTip( "下载失败"+fileName);
                }
            });
        }
    }


    public void downloadFile(final FileView fileView) {
        final FriendView friendView = CacheRepository.getInstance().getFriendViewObservable().get(fileView.getUserId());
        if (friendView == null) {
            mFragment.showTip("用户拒绝分享文件" + fileView.getFileName());
        } else {
            NetServerManager.tryUdpTest();
            //建立P2P通信
            //                                //新建会话, 这里应该在发送下载文件请求时已经创建
            String uuid = UUID.randomUUID().toString();
            ConnectSession connectSession = new ConnectSession();
            connectSession.setUUID(uuid);
            connectSession.setDestroyRunnable(new Runnable() {
                @Override
                public void run() {
                    mFragment.showTip("下载完成");

                    updateDownloadCount(fileView);
                }
            });
            connectSession.setStartRunnable(new Runnable() {
                @Override
                public void run() {
                    mFragment.showTip("开始下载");
                }
            });
            FileReceiverSession fileReceiverSession = new FileReceiverSession(uuid, fileView.getRemark(), fileView.getFileName(), fileView.getFileSize());
            fileReceiverSession.setSlipWindowCount(CacheRepository.getInstance().getSlipWindowCount());
            connectSession.put(FileReceiverSession.TAG, fileReceiverSession);
            ConnectorManager.getInstance().add(connectSession);
            String from = CacheRepository.getInstance().getDeviceId();
            String to = friendView.getDeviceId();
            String msg = MessageManagerOfFile.askDownloadFile( from, to, fileView, uuid, fileReceiverSession.getSlipWindowCount());
            SendMessageBroadcast.getInstance().sendMessage(msg);
        }
    }

    @Override
    public void updateDownloadCount(FileView fileView) {

        mRepository.updateDownloadCount(fileView.getId(), new HttpBaseCallback(){
            @Override
            public void loadFile(FileView fileView) {
                super.loadFile(fileView);
                FileView f = CacheRepository.getInstance().getFileViewObservable().get(fileView.getRemark());
                if(f != null){
                    f.setDownloadCount(fileView.getDownloadCount());
                    CacheRepository.getInstance().getFileViewObservable().put(f);
                }else{
                    CacheRepository.getInstance().getFileViewObservable().put(fileView);
                }

            }
        });
    }

    @Override
    public void deleteFiles(List<FileView> fileViews) {
        User user = CacheRepository.getInstance().who();
        for (FileView f: fileViews) {
            if(f.getUserId() == user.getId()){
                CacheRepository.getInstance().getFileViewObservable().remote(f.getRemark());
                mRepository.deleteFile(f.getId(), user.getId(), user.getVerification(), new HttpBaseCallback(){
                    @Override
                    public void meaning(String text) {
                        super.meaning(text);
                        mFragment.showTip(text);
                    }
                });
            }else{
                mFragment.showTip("无法删除文件");
            }
        }
    }
}
