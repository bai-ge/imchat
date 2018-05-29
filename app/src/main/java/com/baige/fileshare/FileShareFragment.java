package com.baige.fileshare;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.baige.adapter.FileViewAdapter;
import com.baige.data.entity.FileView;
import com.baige.imchat.R;
import com.baige.view.IOnMenuItemClickListener;
import com.baige.view.ShareHomeBottomToolBar;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by baige on 2018/5/5.
 */

public class FileShareFragment extends Fragment implements FileShareContract.View {

    private static final String TAG = FileShareFragment.class.getSimpleName();

    private FileShareContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;


    /*组件*/

    private ListView mListView;
    private ViewGroup mNothingView = null;
    private FileViewAdapter mAdapter = null;
    private ShareHomeBottomToolBar mShareHomeBottomToolBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
        mAdapter = new FileViewAdapter(new ArrayList<FileView>(0), onFileViewItemListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_fileshare, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mListView = root.findViewById(R.id.list_view);
        mNothingView = root.findViewById(R.id.layout_null);
        mShareHomeBottomToolBar = root.findViewById(R.id.bottom_toolbar);
        mShareHomeBottomToolBar.setOnItemClickListener(mOnMenuItemClickListener);
        mListView.setAdapter(mAdapter);


        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mAdapter.getCount() > 0) {
                    mNothingView.setVisibility(View.INVISIBLE);
                } else {
                    mNothingView.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }


    @Override
    public void setPresenter(FileShareContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static FileShareFragment newInstance() {
        return new FileShareFragment();
    }


    @Override
    public void showFileViews(final List<FileView> list) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.updateList(list);
            }
        });
    }

    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.stop();
    }

    private FileViewAdapter.OnFileViewItemListener onFileViewItemListener = new FileViewAdapter.OnFileViewItemListener() {
        @Override
        public void onClickItem(FileView item) {

        }

        @Override
        public void onLongClickItem(FileView item) {

        }
    };

    private IOnMenuItemClickListener mOnMenuItemClickListener = new IOnMenuItemClickListener.SimpleMenuItemClickListener() {
        @Override
        public void onDownload() {
            super.onDownload();
            Log.d(TAG, "onDownload()");
            List<FileView> fileViews = mAdapter.getSelectItems();
            if(fileViews == null || fileViews.isEmpty()){
                showTip("未选择文件");
            }else{
                mPresenter.downloadFiles(fileViews);
            }
        }

        @Override
        public void onShare() {
            super.onShare();
            Log.d(TAG, "onShare()");
        }

        @Override
        public void onSore() {
            super.onSore();
            Log.d(TAG, "onSore()");
        }

        @Override
        public void onRefresh() {
            super.onRefresh();
            Log.d(TAG, "onRefresh()");
        }

        @Override
        public void onMore() {
            super.onMore();
            Log.d(TAG, "onMore()");
        }
    };


}
