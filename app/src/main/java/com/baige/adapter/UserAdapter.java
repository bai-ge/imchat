package com.baige.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.baige.BaseApplication;
import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.remote.LoadingManager;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;

import java.io.File;
import java.util.List;

/**
 * Created by baige on 2018/5/7.
 */

public class UserAdapter extends BaseAdapter {

    private final static String TAG = UserAdapter.class.getSimpleName();
    private List<User> mList;
    private OnUserItemListener mListener;

    private ImageLoader mImageLoader;
    private Handler mHandler;


    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            UserAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }
    public UserAdapter(List<User> list, OnUserItemListener listener) {
        mList = list;
        mListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();
    }
    @Override
    public int getCount() {
        if(mList != null){
            return mList.size();
        }
        return 0;
    }

    @Override
    public User getItem(int i) {
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
            convertView = inflater.inflate(R.layout.item_user, parent, false);
            holder = new ViewHolder();
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.imgView = (ImageView) convertView.findViewById(R.id.img_user);
            holder.nameView = (TextView) convertView.findViewById(R.id.txt_user_name);
            holder.aliasView = (TextView) convertView.findViewById(R.id.txt_user_alias);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setHolder(holder, position);

        return convertView;
    }

    /**
     * 设置Holder上的每一个组件的值
     *
     * @param holder
     * @param position
     */
    private void setHolder(final ViewHolder holder, int position) {
        final User item = mList.get(position);
        String name = item.getName();
        holder.nameView.setText(name);
        if(!Tools.isEmpty(item.getAlias())){
            holder.aliasView.setText(item.getAlias());
        }else {
            holder.aliasView.setText("");
        }
        //TODO 设置照片
        if(!Tools.isEmpty(item.getImgName())){
            final String path = BaseApplication.headImgPath + File.separator + item.getImgName();
            Bitmap bitmap = mImageLoader.getBitmapFromMemoryCache(path);
            if(bitmap == null){
                Log.d(TAG, "从文件"+item.getImgName());
                int size = holder.imgView.getWidth();
                if(size <= 0){
                    size = 90;
                }
                Log.d(TAG, "图片宽度："+size);
                bitmap = ImageLoader.decodeSampledBitmapFromResource(path, size);
            }
            if (bitmap == null) {
                holder.imgView.setImageResource(R.drawable.head_img);
                Log.d(TAG, "从网络"+item.getImgName());
                String url = "http://"+CacheRepository.getInstance().getServerIp() + ":8080/imchat/user/downloadImg.action?imgFileName="+item.getImgName();
                LoadingManager.getInstance().downloadFile(url, BaseApplication.headImgPath, item.getImgName(), new HttpBaseCallback(){
                    @Override
                    public void downloadFinish(String fileName) {
                        super.downloadFinish(fileName);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int size = holder.imgView.getWidth();
                                if(size <= 0){
                                    size = 90;
                                }
                                Bitmap bm = ImageLoader.decodeSampledBitmapFromResource(path, size);
                                if(bm != null){
                                    ImageLoader.getInstance().addBitmapToMemoryCache(path, bm);
                                    Bitmap showBm = bm.copy(Bitmap.Config.ARGB_8888, true);
                                    holder.imgView.setImageBitmap(showBm);
                                }
                            }
                        });
                    }
                });
            } else {
                Log.d(TAG, "显示"+item.getImgName());
                ImageLoader.getInstance().addBitmapToMemoryCache(path, bitmap);
                Bitmap showBm = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                holder.imgView.setImageBitmap(showBm);
            }
        }
        holder.viewGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener != null){
                    mListener.onClickItem(item);
                }
            }
        });
        holder.viewGroup.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(mListener != null){
                    mListener.onLongClickItem(item);
                }
                return true;
            }
        });
    }

    public void updateList(List<User> users) {
        this.mList = users;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(User user) {
        mList.add(user);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ViewGroup viewGroup;
        ImageView imgView;
        TextView nameView;
        TextView aliasView;
    }

   public interface OnUserItemListener{

        void onClickItem(User item);

        void onLongClickItem(User item);
    }
}
