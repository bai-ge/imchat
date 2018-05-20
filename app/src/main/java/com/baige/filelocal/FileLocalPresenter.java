package com.baige.filelocal;

import android.os.Environment;
import android.util.Log;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.data.entity.User;
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

public class FileLocalPresenter implements FileLocalContract.Presenter {
    private final static String TAG = FileLocalPresenter.class.getSimpleName();

    private Repository mRepository;

    private FileLocalContract.View mFragment;

    private LoadFileInfosThread mLoadFileInfosThread;

    private Stack<String> mLoadPathHistory = new Stack<>();

    public FileLocalPresenter(Repository instance, FileLocalFragment fileListFragment) {
        mRepository = checkNotNull(instance);
        mFragment = checkNotNull(fileListFragment);
        mFragment.setPresenter(this);
    }

    @Override
    public void start() {
        String path = CacheRepository.getInstance().getCurrentPath();
        if(Tools.isEmpty(path) || path.length() < CacheRepository.ExternalStoragePath.length()){
            loadFileInfo(CacheRepository.ExternalStoragePath);
        }else{
            loadFileInfo(path);
        }
    }

    @Override
    public void loadFileInfo(String path) {
        if (mLoadFileInfosThread != null && !mLoadFileInfosThread.isInterrupted()) {
            mLoadFileInfosThread.interrupt();
        }
        mFragment.clearFileInfos();
        mFragment.showNavigationPath(path);
        mLoadPathHistory.push(path);
        CacheRepository.getInstance().setCurrentPath(path);
        mLoadFileInfosThread = new LoadFileInfosThread(path);
        mLoadFileInfosThread.start();
    }

    @Override
    public void rebackPack() {
        if(!mLoadPathHistory.isEmpty()){
            String path = mLoadPathHistory.pop();
            if (Tools.isEmpty(path)) {
                loadFileInfo(CacheRepository.ExternalStoragePath);
            }else if(Tools.isEquals(CacheRepository.getInstance().getCurrentPath(), path)){
               rebackPack();
            } else{
                if (mLoadFileInfosThread != null && !mLoadFileInfosThread.isInterrupted()) {
                    mLoadFileInfosThread.interrupt();
                }
                mFragment.clearFileInfos();
                mFragment.showNavigationPath(path);
                CacheRepository.getInstance().setCurrentPath(path);
                mLoadFileInfosThread = new LoadFileInfosThread(path);
                mLoadFileInfosThread.start();
            }
        }else{
            loadFileInfo(CacheRepository.ExternalStoragePath);
        }
    }

    /**
     * 扫描当前文件夹下的异步任务
     *
     * @author Administrator
     */
    class LoadFileInfosThread extends Thread {

        private static final int PROGRESS_SHOW_MIN = 200;
        private String path = Environment.getExternalStorageState();
        private String mTargetPath = null;
        private boolean mIsNeedSetAppName = true;

        public LoadFileInfosThread(String path) {
            if (FileUtils.isLegalPath(path)) {
                this.path = path;
            } else {
                Log.e(TAG, "非法的文件路径！" + path);
            }
            Log.e(TAG, "文件路径" + path);
        }

        public LoadFileInfosThread(String path, String targetPath) {
            if (FileUtils.isLegalPath(path)) {
                this.path = path;
            } else {
                Log.e(TAG, "非法的文件路径！" + path);
            }
            mTargetPath = targetPath;
            Log.e(TAG, "文件路径" + path + ", targe " + mTargetPath);
        }

        @Override
        public void run() {
            boolean showHideFile = true;
            File file = new File(path);
            if (!file.exists() || !file.isDirectory()) {
                Log.e(TAG, "文件路径 return");
                return;
            }
            File[] files = new File(path).listFiles();
            Log.e(TAG, "子文件" + files);
            FileInfo fileInfo = null;
            if (files != null) {
                // for
                if (files.length > PROGRESS_SHOW_MIN) {

                }

                for (int i = 0; i < files.length; i++) {
                    try {
                        if (!showHideFile && FileUtils.isHideFile(files[i].getName())) {
                            continue;
                        }

                        String canonicalPath = files[i].getCanonicalPath();
                        if (files[i].isDirectory()) {
                            // 文件夹
                            fileInfo = new FileInfo(canonicalPath, FileType.TYPE_FOLDER);
                            fileInfo.setAppName(FileUtils.getAppNameFromMap(fileInfo.getName()));
                        } else if (files[i].isFile()) {
                            // 填充文件属性
                            fileInfo = new FileInfo(canonicalPath, files[i].length());
                        }
                        fileInfo.setCreateTime(files[i].lastModified());
                        fileInfo.setFileSize(FileUtils.getChildCount(files[i]));
                        mFragment.addFileInfo(fileInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }// end for
            }
        }

    }

    @Override
    public void stop() {

    }

    @Override
    public void uploadFile(List<FileInfo> fileInfos) {
        User user = CacheRepository.getInstance().who();
        HttpBaseCallback callback = new HttpBaseCallback(){

            @Override
            public void progress(String fileName, long finishSize, long totalSize) {
                Log.d(TAG, fileName + ", size ="+finishSize + ", totalSize" + totalSize);
            }

            @Override
            public void uploadFinish(String fileName) {
                Log.d(TAG, fileName + "上传成功");
                mFragment.showTip("文件上传成功");
            }

            @Override
            public void fail(String fileName) {
                super.fail(fileName);
                mFragment.showTip(fileName + "上传成功");
            }
        };
        for (FileInfo fileInfo : fileInfos){
            mRepository.uploadFile(user, fileInfo, callback);
        }
    }
}
