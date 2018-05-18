package com.baige.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.imchat.R;
import com.baige.register.RegisterActivity;
import com.baige.util.Tools;


/**
 * Created by baige on 2017/12/22.
 */

public class LoginFragment extends Fragment implements LoginContract.View{

    private final static String TAG = LoginFragment.class.getCanonicalName();

    private LoginContract.Presenter mPresenter;

    private EditText mTxtName;

    private EditText mTxtPsw;

    private Button mBtnLogin;

    private Button mBtnRegist;

    private Button mBtmHome;

    private Toast mToast;

    private Handler mHandler;

    private int clickCount;
    private long onClickTime;

    @Override
    public void setPresenter(LoginContract.Presenter presenter) {
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
        View root = inflater.inflate(R.layout.frag_login, container, false);
        initView(root);
        return root;
    }
    private void initView(View root){
        mTxtName = (EditText) root.findViewById(R.id.et_name);
        mTxtPsw = (EditText) root.findViewById(R.id.et_pwd);
        mBtnLogin = (Button) root.findViewById(R.id.btn_login);
        mBtnRegist = (Button) root.findViewById(R.id.btn_regist);
        mBtmHome = (Button) root.findViewById(R.id.btn_home);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mTxtName.getText().toString();
                String psw = mTxtPsw.getText().toString();
                if (Tools.isEmpty(name)) {
                    showTip("用户名为空");
                }  else if (Tools.isEmpty(psw)) {
                    showTip("密码为空");
                } else {
                    mPresenter.login(name, psw);
                }
            }
        });

        mBtnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RegisterActivity.class);
                getContext().startActivity(intent);
            }
        });
        mBtmHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTxtName.getText().toString();
                String psw = mTxtPsw.getText().toString();
                if (Tools.isEmpty(name)) {
                    showTip("用户名为空");
                }  else if (Tools.isEmpty(psw)) {
                    showTip("密码为空");
                } else {
                    User user = CacheRepository.getInstance().who();
                    if(user == null){
                        user = new User();
                        CacheRepository.getInstance().setYouself(user);
                    }
                    user.setName(name);
                    user.setPassword(psw);
                    CacheRepository.getInstance().setLogin(true);
                    getActivity().onBackPressed();
                }
            }
        });

        root.findViewById(R.id.icon_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long now = System.currentTimeMillis();
                if(now - onClickTime <= 500){
                    clickCount ++;
                }else{
                    clickCount = 0;
                }
                onClickTime = now;
                if(clickCount >= 5){
                    if(mBtmHome.getVisibility() == View.VISIBLE){
                        mBtmHome.setVisibility(View.INVISIBLE);
                    }else{
                        mBtmHome.setVisibility(View.VISIBLE);
                    }
                    clickCount = 0;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public void showName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtName.setText(name);
            }
        });
    }

    @Override
    public void setPsw(final String psw) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtPsw.setText(psw);
            }
        });
    }


    public void finishActivity() {
        getActivity().finish();
    }

    @Override
    public void onBack() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                getActivity().onBackPressed();
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
