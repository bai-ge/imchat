package com.baige.view;

/**
 * Created by baige on 2018/5/29.
 */

 public interface IOnMenuItemClickListener {

    void onDownload();

    void onShare();

    void onSore();

    void onRefresh();

    void onMore();
     class SimpleMenuItemClickListener implements IOnMenuItemClickListener{

        @Override
        public void onDownload() {

        }

        @Override
        public void onShare() {

        }

        @Override
        public void onSore() {

        }

        @Override
        public void onRefresh() {

        }

        @Override
        public void onMore() {

        }
    }
}
