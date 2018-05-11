package com.baige.filelist;

import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.FileInfo;

import java.util.List;


/**
 * Created by baige on 2018/5/5.
 */

public interface FileListContract {

    interface Presenter extends BasePresenter {
        void loadFileInfo(String path);
        void rebackPack();
    }

    interface View extends BaseView<Presenter> {
        void showNavigationPath(String path);
        void showFileInfos(List<FileInfo> fileInfoList);
        void clearFileInfos();
        void addFileInfo(FileInfo fileInfo);
    }
}
