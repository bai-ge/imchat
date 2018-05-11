package com.baige.register;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.baige.imchat.R;
import com.baige.util.CodeUtils;
import com.baige.util.Tools;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by baige on 2017/12/22.
 */

public class RegisterFragment extends Fragment implements RegisterContract.View {
    private final static String TAG = RegisterFragment.class.getCanonicalName();

    private RegisterContract.Presenter mPresenter;

    private Handler mHandler;

    private Toast mToast;

    private EditText mTxtName;

    private EditText mTxtPsw;

    private EditText mTxtCheckPsw;

    private EditText mTxtCode;

    private ImageView mImgCode;

    private Button mBtnRegister;

    private Button mBtnBack;

    private CodeUtils mCodeUtils;

    @Override
    public void setPresenter(RegisterContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_register, container, false);
        initView(root);
        return root;
    }

    private void initView(View root) {
        mTxtName = (EditText) root.findViewById(R.id.et_name);
        mTxtPsw = (EditText) root.findViewById(R.id.et_pwd);
        mTxtCheckPsw = (EditText) root.findViewById(R.id.et_check_pwd);
        mTxtCode = (EditText) root.findViewById(R.id.et_pincode);
        mImgCode = (ImageView) root.findViewById(R.id.img_code);
        mBtnRegister = (Button) root.findViewById(R.id.btn_regist);
        mBtnBack = (Button) root.findViewById(R.id.btn_back);

        mImgCode.setImageBitmap(CodeUtils.getInstance().createBitmap());
        mImgCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = CodeUtils.getInstance().createBitmap();
                mImgCode.setImageBitmap(bitmap);
            }
        });

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTxtName.getText().toString();
                String psw = mTxtPsw.getText().toString();
                String checkPsw = mTxtCheckPsw.getText().toString();
                String code = mTxtCode.getText().toString();
                if (Tools.isEmpty(name)) {
                    showTip("用户名为空");
                }  else if (Tools.isEmpty(psw)) {
                    showTip("密码为空");
                } else if (!psw.matches("^[A-Za-z0-9]{6,16}$")) {
                    showTip("密码不符");
                } else if (!psw.equals(checkPsw)) {
                    showTip("密码不一致");
                } else if (Tools.isEmpty(code)) {
                    showTip("验证码为空");
                } else if(!Tools.isEquals(code, CodeUtils.getInstance().getCode())){
                    showTip("验证码错误");
                }else{
                    mPresenter.register(name, psw);
                }
            }
        });
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
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
