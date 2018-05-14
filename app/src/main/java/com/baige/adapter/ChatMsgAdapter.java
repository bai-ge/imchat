package com.baige.adapter;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baige.AppConfigure;
import com.baige.BaseApplication;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.imchat.R;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.Collections;
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
    private void setHolder(ViewHolder holder, int position) {
        final ChatMsgInfo item = mList.get(position);
        if(item.isReceive()){
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.informLayout.setVisibility(View.GONE);

            loadUserImg(item.getUserImgName(), holder.leftImgView);
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
        }else if(item.isSend()){
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.informLayout.setVisibility(View.GONE);
            loadUserImg(item.getUserImgName(), holder.rightImgView);

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
        }else {
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.informLayout.setVisibility(View.VISIBLE);
            holder.informTxt.setText(item.getContext());
        }
    }

    public void updateList(List<ChatMsgInfo> users) {
        this.mList = users;
        if(mList != null){
            Collections.sort(mList);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.mList.clear();
        notifyDataSetChanged();
    }

    public void addItem(ChatMsgInfo user) {
        mList.add(user);
        Collections.sort(mList);
        notifyDataSetChanged();
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
    }
}
