package com.baige.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.baige.imchat.R;


public class ShareHomeBottomToolBar extends LinearLayout implements OnClickListener {

    private View mRootView = null;

    private View mToolBarDownload = null;
    private View mTollBarSort = null;
    private View mToolBarRefresh = null;
    private View mToolBarMore = null;

    private IOnMenuItemClickListener mOnItemClickListener = null;

    public ShareHomeBottomToolBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ShareHomeBottomToolBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShareHomeBottomToolBar(Context context) {
        super(context);
        init(context);
    }

    public void setOnItemClickListener(IOnMenuItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    private void init(Context context) {
        mRootView = LayoutInflater.from(context).inflate(R.layout.share_home_bottom_menu, this, true);

        mToolBarDownload = mRootView.findViewById(R.id.mToolBarDownload);
        mTollBarSort = mRootView.findViewById(R.id.mToolBarSort);
        mToolBarRefresh = mRootView.findViewById(R.id.mToolBarRefresh);
        mToolBarMore = mRootView.findViewById(R.id.mToolBarMore);

        mToolBarDownload.setOnClickListener(this);
        mTollBarSort.setOnClickListener(this);
        mToolBarRefresh.setOnClickListener(this);
        mToolBarMore.setOnClickListener(this);

        DisplayMetrics outMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(outMetrics);
    }


    @Override
    public void onClick(View view) {
        if (mOnItemClickListener == null) {
            return;
        }
        switch (view.getId()) {
            case R.id.mToolBarDownload:
                mOnItemClickListener.onDownload(view);
                break;
            case R.id.mToolBarSort:
                mOnItemClickListener.onSore(view);
                break;
            case R.id.mToolBarRefresh:
                mOnItemClickListener.onRefresh(view);
                break;
            case R.id.mToolBarMore:
                mOnItemClickListener.onMore(view);
                break;
        }
    }



}
