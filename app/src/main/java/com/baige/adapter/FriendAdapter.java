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
import com.baige.data.entity.FriendView;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.remote.LoadingManager;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;

import java.io.File;
import java.util.List;

/**
 * Created by baige on 2018/5/7.
 */

public class FriendAdapter extends BaseAdapter {

    private final static String TAG = FriendAdapter.class.getSimpleName();
    private List<FriendView> mList;
    private OnFriendItemListener mListener;

    private ImageLoader mImageLoader;
    private Handler mHandler;


    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            FriendAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }
    public FriendAdapter(List<FriendView> list, OnFriendItemListener listener) {
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
    public FriendView getItem(int i) {
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
            convertView = inflater.inflate(R.layout.item_friend, parent, false);
            holder = new ViewHolder();
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.imgView = (ImageView) convertView.findViewById(R.id.img_user);
            holder.friendAliasView = (TextView) convertView.findViewById(R.id.txt_friend_alias);
            holder.nameView = (TextView) convertView.findViewById(R.id.txt_user_name);
            holder.aliasView = (TextView) convertView.findViewById(R.id.txt_user_alias);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setHolder(holder, position);

        return convertView;
    }

    private void loadUserImg(String imgName, CircleImageView view){
        if(!Tools.isEmpty(imgName)){
            String url = BaseApplication.headImgPath + File.separator + imgName;
            Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
            if(bitmap == null){
                bitmap = ImageLoader.decodeSampledBitmapFromResource(url, view.getWidth() <= 50 ? 50 : view.getWidth());
            }
            if(bitmap != null){
                ImageLoader.getInstance().addBitmapToMemoryCache(url, bitmap);
                view.setImageBitmap(bitmap);
            }else{
                view.setImageResource(R.drawable.head_img);
            }
        }else{
            view.setImageResource(R.drawable.head_img);
        }
    }
    /**
     * 设置Holder上的每一个组件的值
     *
     * @param holder
     * @param position
     */
    private void setHolder(final ViewHolder holder, int position) {
        final FriendView item = mList.get(position);

        holder.friendAliasView.setText(item.getSuitableName());//备注

        holder.nameView.setText(item.getFriendName());//好友账号

        if(!Tools.isEmpty(item.getAlias())){//好友别名
            holder.aliasView.setText("("+item.getAlias()+")");
        }else{
            holder.aliasView.setText("");
        }

        //TODO 设置照片
        if(!Tools.isEmpty(item.getFriendImgName())){
            final String path = BaseApplication.headImgPath + File.separator + item.getFriendImgName();
            Bitmap bitmap = mImageLoader.getBitmapFromMemoryCache(path);
            if(bitmap == null){
                Log.d(TAG, "从文件"+item.getFriendImgName());
                int size = holder.imgView.getWidth();
                if(size <= 0){
                    size = 90;
                }
                Log.d(TAG, "图片宽度："+size);
                bitmap = ImageLoader.decodeSampledBitmapFromResource(path, size);
            }
            if (bitmap == null) {
                holder.imgView.setImageResource(R.drawable.head_img);
                Log.d(TAG, "从网络"+item.getFriendImgName());
                String url = "http://"+CacheRepository.getInstance().getServerIp() + ":8080/imchat/user/downloadImg.action?imgFileName="+item.getFriendImgName();
                LoadingManager.getInstance().downloadFile(url, BaseApplication.headImgPath, item.getFriendImgName(), new HttpBaseCallback(){
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
                Log.d(TAG, "显示"+item.getFriendImgName());
                ImageLoader.getInstance().addBitmapToMemoryCache(path, bitmap);
                holder.imgView.setImageBitmap(bitmap);
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

    public void updateList(List<FriendView> friendViews) {
        this.mList = friendViews;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(FriendView friendView) {
        mList.add(friendView);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ViewGroup viewGroup;
        ImageView imgView;
        TextView friendAliasView;
        TextView nameView;
        TextView aliasView;
    }

   public interface OnFriendItemListener{

        void onClickItem(FriendView item);

        void onLongClickItem(FriendView item);
    }
}
