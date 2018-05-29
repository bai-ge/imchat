package com.baige.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.baige.data.entity.FileInfo;
import com.baige.imchat.R;

import java.util.List;


public class ShareDialog extends Dialog implements OnClickListener {

    private View root = null;
    private Button mBtnShareLocal = null;
    private Button mBtnShareRemove = null;
    private EditText mEditFileList = null;

    private List<FileInfo> mShareFiles;

    private OnShareDialogListener mListener;

    public ShareDialog(Context context) {
        super(context);
        init();
    }

    public ShareDialog(Context context, List<FileInfo> shareFiles){
        super(context);
        this.mShareFiles = shareFiles;
        init();
    }

    public void setOnShareDialogListener(OnShareDialogListener listener) {
        this.mListener = listener;
    }


    public ShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public ShareDialog(Context context, int theme) {
        super(context, theme);
        init();
    }


    private void init() {
        root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share, null);

        mEditFileList = root.findViewById(R.id.edit_file_list);
        mBtnShareLocal = (Button) root.findViewById(R.id.btn_share_local);
        mBtnShareRemove = (Button) root.findViewById(R.id.btn_share_remove);

        mBtnShareLocal.setOnClickListener(this);
        mBtnShareRemove.setOnClickListener(this);

        this.setTitle("分享文件");
        StringBuffer buffer = new StringBuffer();
        for (FileInfo f: mShareFiles) {
            buffer.append(f.getName()+"\n");
        }
        mEditFileList.setText(buffer.toString());
        this.setContentView(root);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_share_local:
                dismiss();
                if (mListener != null) {
                    mListener.onShareLocal(mShareFiles);
                }
                break;
            case R.id.btn_share_remove:
                dismiss();
                if (mListener != null) {
                    mListener.onShareRemove(mShareFiles);
                }
                break;
            default:
                break;
        }
    }

    public interface OnShareDialogListener {
        void onShareLocal(List<FileInfo> list);

        void onShareRemove(List<FileInfo> list);
    }

}
