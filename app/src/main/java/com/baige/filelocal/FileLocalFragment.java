package com.baige.filelocal;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baige.adapter.FileInfoAdapter;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.imchat.R;
import com.baige.util.FileUtils;
import com.baige.util.Tools;
import com.baige.view.BottomChooseBar;
import com.baige.view.FileListBottomOperatorMenu;
import com.baige.view.FileListBottomToolBar;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by baige on 2018/5/5.
 */

public class FileLocalFragment extends Fragment implements FileLocalContract.View {

    private static final String TAG = FileLocalFragment.class.getSimpleName();

    private FileLocalContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    /*组件*/
    private LinearLayout mLinearTopNavi = null;// 顶部目录导航
    private ListView mListView;
    private FileListBottomToolBar mBottomToolBar = null;// 底部操作工具条
    private FileListBottomOperatorMenu mBottomMenu = null;// 底部弹出式操作菜单
    private BottomChooseBar mBottomChooseBar = null;// 底部确认取消按钮
    private ViewGroup mNothingView = null;
    private FileInfoAdapter mAdapter = null;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
        mAdapter = new FileInfoAdapter(new ArrayList<FileInfo>(0), mOnFileInfoItemListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_filelocal, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mLinearTopNavi = root.findViewById(R.id.linear_top_navigation);
        mListView = root.findViewById(R.id.list_view);
        mBottomToolBar = root.findViewById(R.id.bottom_toolbar);
        mBottomMenu = root.findViewById(R.id.bottom_operator_menu);
        mBottomChooseBar = root.findViewById(R.id.bottom_choose_bar);
        mNothingView = root.findViewById(R.id.layout_null);
        mListView.setAdapter(mAdapter);

        mListView.setOnScrollListener(mAdapter);//异步加载的关键

        mBottomToolBar.setOnItemClickListener(onMenuItemClickListener);
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
    public void setPresenter(FileLocalContract.Presenter presenter) {
        mPresenter = presenter;
    }

    public static FileLocalFragment newInstance() {
        return new FileLocalFragment();
    }


    @Override
    public void clearFileInfos() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
            }
        });
    }

    @Override
    public void addFileInfo(final FileInfo fileInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.addItem(fileInfo);
            }
        });
    }

    @Override
    public void showNavigationPath(final String path) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                initNavigationTopPath(path);
            }
        });
    }

    private void initNavigationTopPath(String path) {
        mLinearTopNavi.removeAllViews();
        List<String> pathStackList = FileUtils.generatePathStack(path);
        for (int i = 0; i < pathStackList.size(); i++) {
            addToNaviList(pathStackList.get(i));
        }
    }

    /* 设置顶部导航条 */
    private void addToNaviList(String path) {
        final View naviItemView = LayoutInflater.from(getContext()).inflate(R.layout.file_navi_item, null);
        final TextView tv = (TextView) naviItemView.findViewById(R.id.txt_file_name);
        String text = FileUtils.getFileName(path);
        if (text == null || text.equals("") || text.equals("/")) {
            text = "Root";
        }
        tv.setText(text);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTip(tv.getText().toString());
                String filePath = (String) naviItemView.getTag();
                if (!Tools.isEmpty(filePath)) {
                    mPresenter.loadFileInfo(filePath);
                }
            }
        });
        naviItemView.setTag(path);
        mLinearTopNavi.addView(naviItemView);
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
    public void showFileInfos(final List<FileInfo> fileInfoList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mAdapter.updateList(fileInfoList);
            }
        });
    }

    private FileInfoAdapter.OnFileInfoItemListener mOnFileInfoItemListener = new FileInfoAdapter.OnFileInfoItemListener() {
        @Override
        public void onClickItem(FileInfo item) {
            if (item.getFileType() == FileType.TYPE_FOLDER) {
                mPresenter.loadFileInfo(item.getPath());
            }
        }

        @Override
        public void onLongClickItem(FileInfo item) {

        }
    };

    private FileListBottomToolBar.IOnMenuItemClickListener onMenuItemClickListener = new FileListBottomToolBar.IOnMenuItemClickListener() {
        @Override
        public void onShare() {
            Log.d(TAG, "onShare()");
            Log.d(TAG, ""+mAdapter.getSelectItems());
            List<FileInfo> fileInfos = mAdapter.getSelectItems();
            mPresenter.uploadFile(fileInfos);
        }

        @Override
        public void onSore() {
            Log.d(TAG, "onSore()");
        }

        @Override
        public void onRefresh() {
            Log.d(TAG, "onRefresh()");
        }

        @Override
        public void onMore() {
            Log.d(TAG, "onMore()");
        }
    };

}
