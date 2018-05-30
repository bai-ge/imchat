package com.baige.view;

import android.view.View;

/**
 * Created by baige on 2018/5/29.
 */

 public interface IOnMenuItemClickListener {

    void onDownload(View view);

    void onShare(View view);

    void onSore(View view);

    void onRefresh(View view);

    void onMore(View view);
     class SimpleMenuItemClickListener implements IOnMenuItemClickListener{

        @Override
        public void onDownload(View view) {

        }

        @Override
        public void onShare(View view) {

        }

        @Override
        public void onSore(View view) {

        }

        @Override
        public void onRefresh(View view) {

        }

        @Override
        public void onMore(View view) {

        }
    }
}
