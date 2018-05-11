package com.baige.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.baige.AppConfigure;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;

import java.io.File;
import java.util.List;

/**
 * Created by baige on 2018/5/7.
 */

public class ChatMsgAdapter extends BaseAdapter {

    private final static String TAG = ChatMsgAdapter.class.getSimpleName();
    private List<ChatMsgInfo> mList;
    private OnChatMsgItemListener mListener;

    private ImageLoader mImageLoader;
    private Handler mHandler;


    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            ChatMsgAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }
    public ChatMsgAdapter(List<ChatMsgInfo> list, OnChatMsgItemListener listener) {
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
    public ChatMsgInfo getItem(int i) {
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
            convertView = inflater.inflate(R.layout.item_chat_msg, parent, false);
            holder = new ViewHolder();
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
    private void setHolder(ViewHolder holder, int position) {
        final ChatMsgInfo item = mList.get(position);

    }

    public void updateList(List<ChatMsgInfo> users) {
        this.mList = users;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(ChatMsgInfo user) {
        mList.add(user);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ViewGroup viewGroup;
        ImageView imgView;
        TextView nameView;
    }

   public interface OnChatMsgItemListener{

        void onClickItem(ChatMsgInfo item);

        void onLongClickItem(ChatMsgInfo item);
    }
}
