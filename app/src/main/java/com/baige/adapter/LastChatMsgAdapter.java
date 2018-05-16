package com.baige.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baige.data.entity.LastChatMsgInfo;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;

import java.util.List;

/**
 * Created by baige on 2018/5/7.
 */

public class LastChatMsgAdapter extends BaseAdapter {

    private final static String TAG = LastChatMsgAdapter.class.getSimpleName();
    private List<LastChatMsgInfo> mList;
    private OnLastChatMsgItemListener mListener;

    private ImageLoader mImageLoader;
    private Handler mHandler;


    private Runnable mNotifyRunnable = new Runnable() {//避免频繁刷新
        @Override
        public void run() {
            LastChatMsgAdapter.super.notifyDataSetChanged();
        }
    };

    @Override
    public void notifyDataSetChanged() {
//        super.notifyDataSetChanged();
        mHandler.removeCallbacks(mNotifyRunnable);
        mHandler.postDelayed(mNotifyRunnable, 20);
    }

    public LastChatMsgAdapter(List<LastChatMsgInfo> list, OnLastChatMsgItemListener listener) {
        mList = list;
        mListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public LastChatMsgInfo getItem(int i) {
        if (mList != null) {
            return mList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public int getMessageCount() {
        int res = 0;
        if (mList == null || mList.size() == 0) {
            return 0;
        } else {
            for (int i = 0; i < mList.size(); i++) {
                res += mList.get(i).getMsgCount();
            }
        }
        return res;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            convertView = inflater.inflate(R.layout.item_lastchat, parent, false);
            holder = new ViewHolder();
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.imgView = (CircleImageView) convertView.findViewById(R.id.img_user);
            holder.nameView = (TextView) convertView.findViewById(R.id.txt_user_name);
            holder.msgView = convertView.findViewById(R.id.txt_msg);
            holder.countView = convertView.findViewById(R.id.txt_msg_count);
            holder.timeView = convertView.findViewById(R.id.txt_last_msg_time);
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
        final LastChatMsgInfo item = mList.get(position);

        holder.nameView.setText(item.getSuitableName());

        //TODO 设置照片
        ImageLoader.loadUserImg(item.getImagName(), holder.imgView);

        if (Tools.isEmpty(item.getLastMessage())) {
            holder.msgView.setText("");
        } else {
            holder.msgView.setText(item.getLastMessage());
        }

        if (item.getMsgCount() > 0) {
            holder.countView.setText(String.valueOf(item.getMsgCount()));
            holder.countView.setVisibility(View.VISIBLE);
        } else {
            holder.countView.setVisibility(View.INVISIBLE);
        }
        holder.timeView.setText(Tools.getSuitableTimeFormat(item.getLastTime()));


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
    }

    public void updateList(List<LastChatMsgInfo> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(LastChatMsgInfo lastChatMsgInfo) {
        mList.add(lastChatMsgInfo);
        notifyDataSetChanged();
    }

    class ViewHolder {
        ViewGroup viewGroup;
        CircleImageView imgView;
        TextView nameView;
        TextView msgView;
        TextView countView;
        TextView timeView;
    }

    public interface OnLastChatMsgItemListener {

        void onClickItem(LastChatMsgInfo item);

        void onLongClickItem(LastChatMsgInfo item);
    }
}
