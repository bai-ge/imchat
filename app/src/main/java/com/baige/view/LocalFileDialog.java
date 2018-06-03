package com.baige.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baige.data.entity.FileInfo;
import com.baige.imchat.R;
import com.baige.util.Tools;


public class LocalFileDialog extends Dialog implements OnClickListener {

    private View root = null;
    private TextView mTxtFileName;
    private TextView mTxtFilePath;
    private TextView mTxtFileSize;
    private Button mBtnOK;

    private FileInfo mFileInfo;

    public LocalFileDialog(Context context) {
        super(context);
        init();
    }

    public LocalFileDialog(Context context, FileInfo fileInfo){
        super(context);
        this.mFileInfo = fileInfo;
        init();
    }


    public LocalFileDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public LocalFileDialog(Context context, int theme) {
        super(context, theme);
        init();
    }


    private void init() {
        root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_local_file, null);
        mTxtFileName = root.findViewById(R.id.txt_file_name);
        mTxtFilePath = root.findViewById(R.id.txt_file_path);
        mTxtFileSize = root.findViewById(R.id.txt_file_size);
        mBtnOK = root.findViewById(R.id.btn_ok);


        mBtnOK.setOnClickListener(this);

        this.setTitle("文件信息");

        if(mFileInfo != null){
            mTxtFileName.setText(mFileInfo.getName());
            mTxtFilePath.setText(mFileInfo.getPath());
            mTxtFileSize.setText(Tools.getSizeSting(mFileInfo.getFileSize())+"("+Tools.formatNumber(mFileInfo.getFileSize())+")");
        }
        this.setContentView(root);
        this.setCanceledOnTouchOutside(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                dismiss();
                break;
            default:
                break;
        }
    }


}
