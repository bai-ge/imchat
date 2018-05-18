package com.baige.adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baige.BaseApplication;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;


import java.util.Collections;
import java.util.List;

import me.kareluo.ui.OptionMenu;
import me.kareluo.ui.OptionMenuView;
import me.kareluo.ui.PopupMenuView;

/**
 * Created by baige on 2018/5/7.
 */

public class ChatMsgAdapter extends BaseAdapter {

    private final static String TAG = ChatMsgAdapter.class.getSimpleName();
    private List<ChatMsgInfo> mList;
    private OnChatMsgItemListener mListener;

    private ImageLoader mImageLoader;
    private Handler mHandler;

    private PopupMenuView mWorningMenuView;

    private PopupMenuView mChatMenuView;

    private int mWorningMenuId = 0;

    private int mChatMenuId = 0;


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

    public ChatMsgAdapter(@NonNull Context context, List<ChatMsgInfo> list, OnChatMsgItemListener listener) {
        mList = list;
        mListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
        mImageLoader = ImageLoader.getInstance();

        mChatMenuView = new PopupMenuView(context, R.menu.chat_menu, new MenuBuilder(context));
        mWorningMenuView = new PopupMenuView(context, R.menu.chat_warning_menu, new MenuBuilder(context) );
        mWorningMenuView.setOnMenuClickListener(new OptionMenuView.OnOptionMenuClickListener() {
            @Override
            public boolean onOptionMenuClick(int position, OptionMenu menu) {
                ChatMsgInfo chatMsgInfo = getItem(mWorningMenuId);
                switch (menu.getId()) {
                    case R.id.resend:
                        if(chatMsgInfo != null && mListener != null){
                            mListener.onResendChat(chatMsgInfo);
                        }
                        break;
                    case R.id.delete:
                        if(chatMsgInfo != null && mListener != null){
                            mListener.onDeleteChat(chatMsgInfo);
                        }
                        break;
                }
                return true;
            }
        });

        mChatMenuView.setOnMenuClickListener(new OptionMenuView.OnOptionMenuClickListener() {
            @Override
            public boolean onOptionMenuClick(int position, OptionMenu menu) {
                ChatMsgInfo chatMsgInfo = getItem(mChatMenuId);
                switch (menu.getId()) {
                    case R.id.chat_delete:
                        //删除信息
                        if(chatMsgInfo != null && mListener != null){
                            mListener.onDeleteChat(chatMsgInfo);
                        }
                        break;
                    case R.id.chat_repeal:
                        if(chatMsgInfo != null && mListener != null){
                            mListener.onRepealChat(chatMsgInfo);
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public int getCount() {
        if (mList != null) {
            return mList.size();
        }
        return 0;
    }

    @Override
    public ChatMsgInfo getItem(int i) {
        if (mList != null) {
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
            holder.viewGroup = convertView.findViewById(R.id.linear_item);
            holder.leftLayout = convertView.findViewById(R.id.left_layout);
            holder.rightLayout = convertView.findViewById(R.id.right_layout);
            holder.informLayout = convertView.findViewById(R.id.inform_layout);
            holder.leftImgView = convertView.findViewById(R.id.left_head);
            holder.rightImgView = convertView.findViewById(R.id.right_head);
            holder.leftTxtUserNameView = convertView.findViewById(R.id.left_name);
            holder.rightTxtUserNameView = convertView.findViewById(R.id.right_name);
            holder.leftMsgBgLayout = convertView.findViewById(R.id.left_msg_frame);
            holder.rightMsgBgLayout = convertView.findViewById(R.id.right_msg_frame);
            holder.leftTxtMsgView = convertView.findViewById(R.id.left_msg);
            holder.rightTxtMsgView = convertView.findViewById(R.id.right_msg);
            holder.leftProgressBarView = convertView.findViewById(R.id.left_progress);
            holder.rightProgressBarView = convertView.findViewById(R.id.right_progress);
            holder.leftBtnWarning = convertView.findViewById(R.id.left_warning_but);
            holder.rightBtnWarning = convertView.findViewById(R.id.right_warning_but);
            holder.informTxt = convertView.findViewById(R.id.inform_msg);
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
    private void setHolder(final ViewHolder holder, final int position) {
        final ChatMsgInfo item = mList.get(position);
        final ChatMsgInfo preItem = position > 1 ? mList.get(position- 1) : null;

        if(item.isReceive()){
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.informLayout.setVisibility(View.GONE);

            ImageLoader.loadUserImg(item.getUserImgName(), holder.leftImgView);
            holder.leftTxtUserNameView.setText(item.getUserName());
            if(item.isText()){
                holder.leftTxtMsgView.setText(item.getContext());
            }else {
                holder.leftTxtMsgView.setText("");
            }
            if(item.isSending()){
                holder.leftProgressBarView.setVisibility(View.VISIBLE);
            }else{
                holder.leftProgressBarView.setVisibility(View.GONE);
            }
            if(item.isWarning()){
                holder.leftBtnWarning.setVisibility(View.VISIBLE);
            }else {
                holder.leftBtnWarning.setVisibility(View.GONE);
            }
            if(position == 0 || preItem != null && item.getSendTime() - preItem.getSendTime() >= 5 * 60 * 1000){
                holder.informTxt.setText(Tools.getSuitableTimeFormat(item.getSendTime()));
                holder.informLayout.setVisibility(View.VISIBLE);
            }
        }else if(item.isSend()){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.informLayout.setVisibility(View.GONE);
            ImageLoader.loadUserImg(item.getUserImgName(), holder.rightImgView);

            holder.rightTxtUserNameView.setText(item.getUserName());
            if(item.isText()){
                holder.rightTxtMsgView.setText(item.getContext());
            }else {
                holder.rightTxtMsgView.setText("");
            }
            if(item.isSending()){
                holder.rightProgressBarView.setVisibility(View.VISIBLE);
            }else{
                holder.rightProgressBarView.setVisibility(View.GONE);
            }
            if(item.isWarning()){
                holder.rightBtnWarning.setVisibility(View.VISIBLE);
            }else {
                holder.rightBtnWarning.setVisibility(View.GONE);
            }
           if(position == 0 || preItem != null && item.getSendTime() - preItem.getSendTime() >= 5 * 60 * 1000){
                holder.informTxt.setText(Tools.getSuitableTimeFormat(item.getSendTime()));
                holder.informLayout.setVisibility(View.VISIBLE);
            }
        }else {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.informLayout.setVisibility(View.VISIBLE);
            holder.informTxt.setText(item.getContext());
        }
        //设置监听动作
        holder.leftMsgBgLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mChatMenuId = position;
                Log.e("ChatAdapter", "更新ChatId" + mChatMenuId);
                mChatMenuView.show(holder.leftMsgBgLayout);
                return true;
            }
        });
        holder.rightMsgBgLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mChatMenuId = position;
                Log.e("ChatAdapter", "更新ChatId" + mChatMenuId);
                mChatMenuView.show( holder.rightMsgBgLayout);
                return true;
            }
        });
        holder.leftBtnWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorningMenuId = position;
                Log.e("ChatAdapter", "更新WorningId" + mWorningMenuId);
                mWorningMenuView.show(holder.leftBtnWarning);
            }
        });
        holder.rightBtnWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWorningMenuId = position;
                Log.e("ChatAdapter", "更新WorningId" + mWorningMenuId);
                mWorningMenuView.show( holder.rightBtnWarning);
            }
        });
    }

    public void updateList(List<ChatMsgInfo> chatMsgInfos) {
        this.mList = chatMsgInfos;
        if(mList != null){
            Collections.sort(mList);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(ChatMsgInfo chatMsgInfo) {
        mList.add(chatMsgInfo);
        Collections.sort(mList);
        notifyDataSetChanged();
    }
    public void addItem(ChatMsgInfo chatMsgInfo, boolean notify) {
        mList.add(chatMsgInfo);
        Collections.sort(mList);
       if(notify){
           notifyDataSetChanged();
       }
    }

    class ViewHolder {
        ViewGroup viewGroup;
        ViewGroup leftLayout;
        ViewGroup rightLayout;
        ViewGroup informLayout;
        CircleImageView leftImgView;
        CircleImageView rightImgView;
        TextView leftTxtUserNameView;
        TextView rightTxtUserNameView;
        ViewGroup leftMsgBgLayout;
        ViewGroup rightMsgBgLayout;
        TextView leftTxtMsgView;
        TextView rightTxtMsgView;
        ProgressBar leftProgressBarView;
        ProgressBar rightProgressBarView;
        Button leftBtnWarning;
        Button rightBtnWarning;
        TextView informTxt;
    }

    public interface OnChatMsgItemListener {

        void onClickItem(ChatMsgInfo item);

        void onLongClickItem(ChatMsgInfo item);

        void onResendChat(ChatMsgInfo item);

        void onDeleteChat(ChatMsgInfo item);

        void onRepealChat(ChatMsgInfo item);
    }
}
