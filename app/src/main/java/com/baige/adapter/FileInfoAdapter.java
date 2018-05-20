package com.baige.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baige.BaseApplication;
import com.baige.data.entity.AppPackgeInfo;
import com.baige.data.entity.FileInfo;
import com.baige.data.entity.FileType;
import com.baige.filelocal.FileComparator;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/6.
 */

public class FileInfoAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    private final static String TAG = FileInfoAdapter.class.getSimpleName();
    private List<FileInfo> mList;
    private OnFileInfoItemListener mListener;
    private ImageLoader mImageLoader = null;
    private FileComparator mFileComparator;
    private ExecutorService mPool = null;
    private Handler mHandler;

    private int mFirstVisibleItem = 0;
    private int mVisibleItemCount = 0;
    private AbsListView mListView;

    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            FileInfoAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }



    public FileInfoAdapter(List<FileInfo> list, OnFileInfoItemListener listener) {
        this.mList = checkNotNull(list);
        this.mListener = checkNotNull(listener);
        mFileComparator = new FileComparator();
        mHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();
        mPool = Executors.newFixedThreadPool(3);
    }

    @Override
    public int getCount() {
        if(mList != null){
            return mList.size();
        }
        return 0;
    }

    @Override
    public FileInfo getItem(int i) {
        if(mList != null){
            return mList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    public List<FileInfo> getSelectItems(){
        List<FileInfo> list = new ArrayList<>();
        for (FileInfo fileInfo : mList){
            if(fileInfo.isChecked()){
                list.add(fileInfo);
            }
        }
        return list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_filelocal, parent, false);
            holder = new ViewHolder();
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.imgView = (ImageView) convertView.findViewById(R.id.img_file_format);
            holder.nameView = (TextView) convertView.findViewById(R.id.txt_file_name);
            holder.tagDivView = convertView.findViewById(R.id.tag_div);
            holder.pathView = (TextView) convertView.findViewById(R.id.txt_file_path);
            holder.infoView = (TextView) convertView.findViewById(R.id.txt_info);
            holder.timeView = (TextView) convertView.findViewById(R.id.txt_file_create_time);
            holder.tagView = (TextView) convertView.findViewById(R.id.txt_file_tag);
            holder.checkView = convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setHolder(holder, position);

        return convertView;
    }

    public void updateList(List<FileInfo> fileInfos) {
        this.mList = fileInfos;
        if (mList != null) {
            Collections.sort(mList, mFileComparator);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(FileInfo fileInfo) {
        mList.add(fileInfo);
        if (mList != null) {
            Collections.sort(mList, mFileComparator);
        }
        notifyDataSetChanged();
    }



    /**
     * 设置Holder上的每一个组件的值
     *
     * @param holder
     * @param position
     */
    private void setHolder(ViewHolder holder, int position) {
        final FileInfo item = getItem(position);

        /*设置图片*/
        int type = item.getFileType();

        if (type == FileType.TYPE_APK) {
            AppPackgeInfo pkgInfo = BaseApplication.getApkPackgageInfo(item.getPath());
            if (pkgInfo != null) {
                holder.imgView.setImageDrawable(pkgInfo.getIcon());
            } else {
                holder.imgView.setImageResource(R.drawable.icon_fm_apk);
            }
        } else if (type == FileType.TYPE_PIC || type == FileType.TYPE_MP4 || type == FileType.TYPE_AVI
                || type == FileType.TYPE_3GP || type == FileType.TYPE_RMVB || type == FileType.TYPE_PNG || type == FileType.TYPE_JPG) {
            //TODO 不一定是本地目录，文件可能无法读取,需要向远程设备请求数据
            Bitmap bitmap = mImageLoader.getBitmapFromMemoryCache(item.getPath());
            if (bitmap == null) {
                holder.imgView.setImageResource(FileType.getResourceIdByType(item.getFileType()));
                LoadImageToImageLoaderRunnable runnable = new LoadImageToImageLoaderRunnable(item.getPath());
                mPool.execute(runnable);
            } else {
                holder.imgView.setImageBitmap(bitmap);
            }
        } else {
            holder.imgView.setImageResource(FileType.getResourceIdByType(item.getFileType()));
        }
        Log.d(TAG, item.toString() );
        holder.imgView.setTag(item.getPath());

        holder.nameView.setText(item.getName());
        holder.pathView.setText(item.getPath());
        holder.tagView.setText("");
        holder.tagDivView.setVisibility(View.INVISIBLE);
        if (item.getFileType() == FileType.TYPE_FOLDER) {
            if (!Tools.isEmpty(item.getAppName())) {
                holder.tagDivView.setVisibility(View.VISIBLE);
                holder.tagView.setText(item.getAppName());
            }
            holder.infoView.setText(String.valueOf(item.getFileSize() + "项"));
        } else {
            holder.infoView.setText(Tools.getSizeSting(item.getFileSize()));
        }
        // 创建时间
        holder.timeView.setText(Tools.formatTime(item.getCreateTime()));
        holder.checkView.setChecked(item.isChecked());

        holder.viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onClickItem(item);
                }
            }
        });
        holder.viewGroup.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mListener != null) {
                    mListener.onLongClickItem(item);
                }
                return true;
            }
        });
        holder.checkView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                item.setChecked(b);
            }
        });
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
           // loadBitmaps(view, mFirstVisibleItem, mVisibleItemCount);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mListView = view;
        mFirstVisibleItem = firstVisibleItem;
        mVisibleItemCount = visibleItemCount;
        Log.d(TAG, "文件："+firstVisibleItem+"异步加载数据");
       // loadBitmaps(view, firstVisibleItem, visibleItemCount);
    }

    // 加载图片
    public void loadBitmaps(AbsListView view, int first, int pageCount) {
        for (int i = first; i < first + pageCount; i++) {
            int fileType = mList.get(i).getFileType();
            if (i < mList.size()
                    && (fileType == FileType.TYPE_PIC || fileType == FileType.TYPE_MP4 || fileType == FileType.TYPE_AVI
                    || fileType == FileType.TYPE_3GP || fileType == FileType.TYPE_RMVB || fileType == FileType.TYPE_PNG || fileType == FileType.TYPE_JPG)) {

                String url = mList.get(i).getPath();
                Log.d(TAG, "文件url："+url+"异步加载数据");
                Bitmap bitmap = mImageLoader.getBitmapFromMemoryCache(url);
                if (bitmap == null) {
                    // 异步加载
                    LoadImageToImageLoaderRunnable runnable = new LoadImageToImageLoaderRunnable(url);
                    mPool.execute(runnable);
                } else {
                    ImageView imageView = (ImageView) view.findViewWithTag(url);
                    if (imageView != null && bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            }
        }
    }

    class LoadImageToImageLoaderRunnable implements Runnable {

        private String mUrl = null;

        public LoadImageToImageLoaderRunnable(String url) {
            mUrl = url;
        }

        @Override
        public void run() {
            if(!Tools.isEmpty(mUrl)){
                final Bitmap bitmap = ImageLoader.decodeSampledBitmapFromResource(mUrl, 80);
                mImageLoader.addBitmapToMemoryCache(mUrl, bitmap);
                if(bitmap != null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(mListView == null){
                                return;
                            }
                            Log.d(TAG, "文件："+mUrl+"异步加载数据");
                            ImageView imageView = (ImageView) mListView.findViewWithTag(mUrl);
                            if (imageView != null ) {
                                imageView.setImageBitmap(bitmap);

                                Animation anim = AnimationUtils.loadAnimation(BaseApplication.getAppContext(), R.anim.alpha_action_long);
                                imageView.startAnimation(anim);
                            }
                        }
                    });
                }
            }

        }
    }


    class ViewHolder {
        ViewGroup viewGroup;
        ImageView imgView;
        TextView nameView;
        TextView tagDivView;
        TextView pathView;
        TextView infoView;
        TextView timeView;
        TextView tagView;
        CheckBox checkView;
    }

    public interface OnFileInfoItemListener {
        void onClickItem(FileInfo item);

        void onLongClickItem(FileInfo item);
    }
}
