package com.baige.fileshare;

import com.baige.BasePresenter;
import com.baige.BaseView;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileView;

import java.util.List;


/**
 * Created by baige on 2018/5/5.
 */

public interface FileShareContract {

    interface Presenter extends BasePresenter {
        void searchFiles();
        void downloadFiles(List<FileView> fileViews);
        void updateDownloadCount(FileView fileView);
        void deleteFiles(List<FileView> fileViews);
    }

    interface View extends BaseView<Presenter> {
        void showTip(String text);
        void showFileViews(List<FileView> list);
    }
}
