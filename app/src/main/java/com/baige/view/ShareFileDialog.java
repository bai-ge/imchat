package com.baige.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baige.data.entity.FileView;
import com.baige.imchat.R;
import com.baige.util.Tools;


public class ShareFileDialog extends Dialog implements OnClickListener {

    private View root = null;
    private TextView mTxtFileName;
    private TextView mTxtFileOwner;
    private TextView mTxtFileDownloadCount;
    private TextView mTxtSource;
    private TextView mTxtFileSize;
    private Button mBtnOK;

    private FileView mFileView;

    public ShareFileDialog(Context context) {
        super(context);
        init();
    }

    public ShareFileDialog(Context context, FileView fileView){
        super(context);
        this.mFileView = fileView;
        init();
    }


    public ShareFileDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init();
    }

    public ShareFileDialog(Context context, int theme) {
        super(context, theme);
        init();
    }


    private void init() {
        root = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share_file, null);
        mTxtFileName = root.findViewById(R.id.txt_file_name);
        mTxtFileOwner = root.findViewById(R.id.txt_file_owner);
        mTxtFileDownloadCount = root.findViewById(R.id.txt_download_count);
        mTxtSource = root.findViewById(R.id.txt_file_source);
        mTxtFileSize = root.findViewById(R.id.txt_file_size);
        mBtnOK = root.findViewById(R.id.btn_ok);


        mBtnOK.setOnClickListener(this);

        this.setTitle("文件信息");

        if(mFileView != null){
            mTxtFileName.setText(mFileView.getFileName());
            mTxtFileOwner.setText(mFileView.getUserName());
            mTxtFileDownloadCount.setText(String.valueOf(mFileView.getDownloadCount()));
            if(mFileView.isRemote()){
                mTxtSource.setText("服务器");
            }else{
                mTxtSource.setText("用户本地");
            }
            mTxtFileSize.setText(Tools.getSizeSting(mFileView.getFileSize())+"("+Tools.formatNumber(mFileView.getFileSize())+")");
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
