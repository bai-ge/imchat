package com.baige.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.baige.imchat.R;


public class SortDialog extends Dialog implements OnClickListener {

    private View mView = null;
    private Button mBtnSortNameUp = null;
    private Button mBtnSortNameDown = null;
    private Button mBtnSortSizeUp = null;
    private Button mBtnSortSizeDown = null;
    private Button mBtnSortDateUp = null;
    private Button mBtnSortDateDown = null;


    private OnSortDialogListener mListener;

    public SortDialog(Context context) {
        super(context);
        init();
    }

    public void setOnSorDialogListener(OnSortDialogListener mListener) {
        this.mListener = mListener;
    }


    public SortDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public SortDialog(Context context, int theme) {
        super(context, theme);
        init();
    }



    private void init() {
        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sort, null);

        mBtnSortNameUp = (Button) mView.findViewById(R.id.mBtnSortNameUp);
        mBtnSortNameDown = (Button) mView.findViewById(R.id.mBtnSortNameDown);
        mBtnSortSizeUp = (Button) mView.findViewById(R.id.mBtnSortSizeUp);
        mBtnSortSizeDown = (Button) mView.findViewById(R.id.mBtnSortSizeDown);
        mBtnSortDateUp = (Button) mView.findViewById(R.id.mBtnSortDateUp);
        mBtnSortDateDown = (Button) mView.findViewById(R.id.mBtnSortDateDown);

        mBtnSortNameUp.setOnClickListener(this);
        mBtnSortNameDown.setOnClickListener(this);
        mBtnSortSizeUp.setOnClickListener(this);
        mBtnSortSizeDown.setOnClickListener(this);
        mBtnSortDateUp.setOnClickListener(this);
        mBtnSortDateDown.setOnClickListener(this);

        this.setTitle("排序方式");
        this.setContentView(mView);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.mBtnSortNameUp:
            dismiss();
          if(mListener != null){
              mListener.onSortNameUp();
          }
            break;

        case R.id.mBtnSortNameDown:
            dismiss();
            if(mListener != null){
                mListener.onSortNameDown();
            }
            break;

        case R.id.mBtnSortSizeUp:
            dismiss();
            if(mListener != null){
                mListener.onSortSizeUp();
            }
            break;
        case R.id.mBtnSortSizeDown:
            dismiss();
            if(mListener != null){
                mListener.onSortSizeDown();
            }
            break;
        case R.id.mBtnSortDateUp:
            if(mListener != null){
                mListener.onSortTimeUp();
            }
            dismiss();
            break;
        case R.id.mBtnSortDateDown:
            if(mListener != null){
                mListener.onSortTimeDown();
            }
            dismiss();
            break;
        default:
            break;
        }
    }

    public interface OnSortDialogListener{
        void onSortNameUp();
        void onSortNameDown();
        void onSortSizeUp();
        void onSortSizeDown();
        void onSortTimeUp();
        void onSortTimeDown();
    }

}
