package com.baige.filelocal;

import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.baige.adapter.FileInfoAdapter;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.imchat.R;
import com.baige.util.FileUtils;
import com.baige.util.SystemOpenType;
import com.baige.util.Tools;
import com.baige.view.BottomChooseBar;
import com.baige.view.FileListBottomOperatorMenu;
import com.baige.view.FileListBottomToolBar;
import com.baige.view.IOnMenuItemClickListener;
import com.baige.view.LocalFileDialog;
import com.baige.view.ShareDialog;
import com.baige.view.SortDialog;

import java.io.File;
import java.lang.reflect.Method;
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

    @Override
    public void showSortDialog() {
        SortDialog dialog = new SortDialog(getContext());
        dialog.setOnSorDialogListener(mAdapter);
        dialog.show();
    }

    @Override
    public void showShareDialog(List<FileInfo> fileInfos) {
        ShareDialog dialog = new ShareDialog(getContext(), fileInfos);
        dialog.setOnShareDialogListener(mShareDialogListener);
        dialog.show();
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.file_more_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                List<FileInfo >fileInfos = null;
                switch (item.getItemId()) {
                    case R.id.delete_file:

                        fileInfos = mAdapter.getSelectItems();
                        if(fileInfos == null || fileInfos.isEmpty()){
                            showTip("未选择文件");
                        }else{
                            for (FileInfo fileInfo : fileInfos){
                                File file = new File(fileInfo.getPath());
                                if(!file.isDirectory() && file.exists()){
                                     file.delete();
                                }
                            }
                            mPresenter.refresh();
                        }
                        break;
                    case R.id.file_information:
                        fileInfos = mAdapter.getSelectItems();
                        if(fileInfos == null || fileInfos.isEmpty()){
                            showTip("未选择文件");
                        }else{
                            LocalFileDialog localFileDialog = new LocalFileDialog(getContext(), fileInfos.get(0));
                            localFileDialog.show();
                        }
                       showTip("详情");
                        break;
                }
                return false;
            }
        });
        setIconEnable(popupMenu.getMenu(), true);
        popupMenu.show();
    }

    private void setIconEnable(Menu menu, boolean enable)
    {
        try
        {
            Class<?> clazz = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Method m = clazz.getDeclaredMethod("setOptionalIconsVisible", boolean.class);
            m.setAccessible(true);
            //传入参数
            m.invoke(menu, enable);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 播放指定名称的歌曲
     * @param audioPath 指定默认播放的音乐
     */
    public  void playAudio(String audioPath){
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        String u = "file://"+audioPath;
        Log.d(TAG, "uri路径:"+u);
        Uri uri = Uri.parse(u);//替换成audiopath
        intent.setDataAndType(uri , "audio/mp3");
        startActivity(intent);
    }



    private FileInfoAdapter.OnFileInfoItemListener mOnFileInfoItemListener = new FileInfoAdapter.OnFileInfoItemListener() {
        @Override
        public void onClickItem(FileInfo item) {
            if (item.getFileType() == FileType.TYPE_FOLDER) {
                mPresenter.loadFileInfo(item.getPath());
            }else if(item.getFileType() == FileType.TYPE_MP3){
              playAudio(item.getPath());
//                Uri uri =Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"1");
//                Intent it = new Intent(Intent.ACTION_VIEW,uri);
//                startActivity(it);
            }else{
                SystemOpenType.systemOpen(item, getContext());
            }
        }

        @Override
        public void onLongClickItem(FileInfo item) {

        }
    };

    private IOnMenuItemClickListener onMenuItemClickListener = new IOnMenuItemClickListener.SimpleMenuItemClickListener() {
        @Override
        public void onShare(View view) {
            Log.d(TAG, "onShare()");
            Log.d(TAG, ""+mAdapter.getSelectItems());

            List<FileInfo> fileInfos = mAdapter.getSelectItems();
            if(fileInfos == null || fileInfos.isEmpty()){
                showTip("未选择分享文件");
            }else{
                showShareDialog(fileInfos);
            }
        }

        @Override
        public void onSore(View view) {
            Log.d(TAG, "onSore()");
            showSortDialog();
        }

        @Override
        public void onRefresh(View view) {
            Log.d(TAG, "onRefresh()");
        }

        @Override
        public void onMore(View view) {
            Log.d(TAG, "onMore()");
            showPopupMenu(view);
        }
    };

    private ShareDialog.OnShareDialogListener mShareDialogListener = new ShareDialog.OnShareDialogListener() {
        @Override
        public void onShareLocal(List<FileInfo> list) {
            Log.d(TAG, "onShareLocal()");
            mPresenter.shareFile(list);
        }

        @Override
        public void onShareRemove(List<FileInfo> list) {
            Log.d(TAG, "onShareRemove()");
            mPresenter.uploadFile(list);
        }

    };

}
