package com.baige.friend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.baige.BaseApplication;
import com.baige.data.entity.FriendView;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.imchat.R;
import com.baige.register.RegisterActivity;
import com.baige.util.ImageLoader;
import com.baige.util.Tools;
import com.baige.view.CircleImageView;

import java.io.File;


/**
 * Created by baige on 2017/12/22.
 */

public class FriendFragment extends Fragment implements FriendContract.View{

    private final static String TAG = FriendFragment.class.getSimpleName();

    private FriendContract.Presenter mPresenter;

    private Toast mToast;

    private Handler mHandler;

    private CircleImageView mCircleImageView;

    private TextView mTxtUserName;

    private TextView mTxtUserAlias;

    private EditText mEditFriendAlias;

    private ImageButton mBtnEditFriendAlias;

    private Button mBtnFriend;

    @Override
    public void setPresenter(FriendContract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_friend, container, false);
        initView(root);
        return root;
    }
    private void initView(View root){
        mCircleImageView = root.findViewById(R.id.img_user);
        mTxtUserName = root.findViewById(R.id.txt_user_name);
        mTxtUserAlias = root.findViewById(R.id.txt_user_alias);
        mEditFriendAlias = root.findViewById(R.id.edit_friend_alias);
        mBtnEditFriendAlias = root.findViewById(R.id.btn_edit_friend_alias);

        mEditFriendAlias.setEnabled(false);
        mBtnEditFriendAlias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditFriendAlias.setEnabled(!mEditFriendAlias.isEnabled());
                if(mEditFriendAlias.isEnabled()){
                    mEditFriendAlias.requestFocus();
                    Tools.showInputMethod(getContext(), mEditFriendAlias);
                    mEditFriendAlias.setSelection(mEditFriendAlias.getText().length());
                }else{
                   mPresenter.updateFriendAlias(mEditFriendAlias.getText().toString());
                }
            }
        });

        mEditFriendAlias.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_BACK)) {
                    mEditFriendAlias.setEnabled(false);
                    mPresenter.updateFriendAlias(mEditFriendAlias.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void showFriendView(FriendView friendView) {
        Log.d(TAG, friendView.toString());
        showFriendImg(friendView.getFriendImgName());
        showFriendName(friendView.getFriendName());
        showUserAlias(friendView.getAlias());
        showFriendAlias(friendView.getFriendAlias());
    }

    @Override
    public void showFriendImg(String imgName) {
        if (!Tools.isEquals(mCircleImageView.getTag(), imgName)) {
            Log.d(TAG, "加载"+imgName);
            String url = BaseApplication.headImgPath + File.separator + imgName;
            Bitmap bitmap = ImageLoader.getInstance().getBitmapFromMemoryCache(url);
            if(bitmap == null){
                Log.d(TAG, "从文件"+imgName);
                int size = mCircleImageView.getWidth();
                Log.d(TAG, "图片宽度："+size);
                if(size <= 10){
                    size = 200;
                }
                bitmap = ImageLoader.decodeSampledBitmapFromResource(url, size);
            }
            if (bitmap == null) {
                Log.d(TAG, "从网络"+imgName);
                mPresenter.downloadImg(imgName);
            } else {
                Log.d(TAG, "显示"+imgName);
                mCircleImageView.setTag(imgName);
                ImageLoader.getInstance().addBitmapToMemoryCache(url, bitmap);
                showFriendImg(bitmap);
            }
        }
    }

    @Override
    public void showFriendImg(final Bitmap img) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCircleImageView.setImageBitmap(img);
            }
        });
    }

    @Override
    public void showFriendName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(Tools.isEmpty(name)){
                    mTxtUserName.setText("");
                }else{
                    mTxtUserName.setText(name);
                }
            }
        });
    }

    @Override
    public void showUserAlias(final String alias) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(Tools.isEmpty(alias)){
                    mTxtUserAlias.setText("");
                }else{
                    mTxtUserAlias.setText(alias);
                }
            }
        });
    }

    @Override
    public void showFriendAlias(final String friendAlias) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(Tools.isEmpty(friendAlias)){
                    mEditFriendAlias.setText("");
                }else{
                    mEditFriendAlias.setText(friendAlias);
                }
            }
        });
    }

    public static FriendFragment newInstance() {
        return new FriendFragment();
    }

    @Override
    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }
}
