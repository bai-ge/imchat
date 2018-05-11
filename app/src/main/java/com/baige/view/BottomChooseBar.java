package com.baige.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.baige.imchat.R;


public class BottomChooseBar extends LinearLayout implements OnClickListener {

    private View mView = null;
    private View mBottomCancel = null;
    private View mBottomEnsure = null;
    private int mHeight = 200;
    private boolean mIsShow = false;

    private OnBottomChooserBarClickListener mOnBarClickListener = null;

    public BottomChooseBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }
    
    public boolean  isShow() {
        return mIsShow;
    }

    public BottomChooseBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomChooseBar(Context context) {
        super(context);
        init();
    }

    public void setOnBottomChooserBarClickListener(OnBottomChooserBarClickListener listener) {
        mOnBarClickListener = listener;
    }

    private void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_choose_menu, this, true);
        mBottomCancel = findViewById(R.id.bottom_cancel);
        mBottomEnsure = findViewById(R.id.bottom_ensure);

        mBottomCancel.setOnClickListener(this);
        mBottomEnsure.setOnClickListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (!mIsShow) {
            hide();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
    }

    public void show() {
        mIsShow = true;
        mView.scrollTo(0, 0);
    }

    public void hide() {
        mIsShow = false;
        mView.scrollTo(0, -mHeight);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bottom_cancel:
            if (mOnBarClickListener != null) {
                mOnBarClickListener.onEnsure(mBottomEnsure);
            }
            hide();
            break;

        case R.id.bottom_ensure:
            if (mOnBarClickListener != null) {
                mOnBarClickListener.onCancel(mBottomCancel);
            }
            hide();
            break;

        default:
            break;
        }
    }

    public interface OnBottomChooserBarClickListener {

        void onEnsure(View v);
        void onCancel(View v);

    }
}
