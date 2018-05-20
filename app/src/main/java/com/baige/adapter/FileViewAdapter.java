package com.baige.adapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.baige.data.entity.FileType;
import com.baige.data.entity.FileView;
import com.baige.imchat.R;
import com.baige.util.Tools;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/6.
 */

public class FileViewAdapter extends BaseAdapter {

    private final static String TAG = FileViewAdapter.class.getSimpleName();
    private List<FileView> mList;
    private OnFileViewItemListener mListener;
    private Handler mHandler;

    private int mFirstVisibleItem = 0;
    private int mVisibleItemCount = 0;
    private AbsListView mListView;

    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            FileViewAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }



    public FileViewAdapter(List<FileView> list, OnFileViewItemListener listener) {
        this.mList = checkNotNull(list);
        this.mListener = checkNotNull(listener);
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int getCount() {
        if(mList != null){
            return mList.size();
        }
        return 0;
    }

    @Override
    public FileView getItem(int i) {
        if(mList != null){
            return mList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_fileshare, parent, false);
            holder = new ViewHolder();
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.imgView = (ImageView) convertView.findViewById(R.id.img_file_format);
            holder.nameView = (TextView) convertView.findViewById(R.id.txt_file_name);
            holder.tagDivView = convertView.findViewById(R.id.tag_div);
            holder.describeView = (TextView) convertView.findViewById(R.id.txt_file_describe);
            holder.infoView = (TextView) convertView.findViewById(R.id.txt_info);
            holder.timeView = (TextView) convertView.findViewById(R.id.txt_file_create_time);
            holder.tagView = (TextView) convertView.findViewById(R.id.txt_file_tag);
            holder.downloadView = (TextView) convertView.findViewById(R.id.txt_download_count);
            holder.checkView = convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setHolder(holder, position);

        return convertView;
    }

    public void updateList(List<FileView> fileViews) {
        this.mList = fileViews;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(FileView fileView) {
        mList.add(fileView);
        notifyDataSetChanged();
    }



    /**
     * 设置Holder上的每一个组件的值
     *
     * @param holder
     * @param position
     */
    private void setHolder(ViewHolder holder, int position) {
        final FileView item = getItem(position);
        Log.d(TAG, item.toString() );

        /*设置图片*/
        int type = item.getFileType();

        holder.imgView.setImageResource(FileType.getResourceIdByType(item.getFileType()));

        holder.nameView.setText(item.getFileName());
        if(Tools.isEmpty(item.getUserName())){
            holder.tagDivView.setVisibility(View.INVISIBLE);
            holder.tagView.setText("");
        }else{
            holder.tagDivView.setVisibility(View.VISIBLE);
            holder.tagView.setText(item.getUserName());
        }

        holder.describeView.setText(item.getFileDescribe());
//        holder.tagDivView.setVisibility(View.INVISIBLE);
        holder.infoView.setText(Tools.getSizeSting(item.getFileSize()));

        // 创建时间
        holder.timeView.setText(Tools.formatTime(item.getUploadTime()));
        holder.downloadView.setText("下载量："+ item.getDownloadCount());
        holder.checkView.setChecked(item.isCheck());

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
                item.setCheck(b);
            }
        });
    }




    class ViewHolder {
        ViewGroup viewGroup;
        ImageView imgView;
        TextView nameView;
        TextView tagDivView;
        TextView describeView; //文件描述
        TextView infoView;     //文件大小
        TextView timeView;
        TextView tagView;      //文件用户名
        TextView downloadView; //下载量
        CheckBox checkView;
    }

    public interface OnFileViewItemListener {
        void onClickItem(FileView item);

        void onLongClickItem(FileView item);
    }
}
