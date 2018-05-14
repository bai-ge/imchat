package com.baige.chat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.baige.adapter.ChatMsgAdapter;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.imchat.R;
import com.baige.telephone.PhoneActivity;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by 百戈 on 2017/2/19.
 */


public class ChatFragment extends Fragment implements ChatContract.View {

    private String TAG = ChatFragment.class.getSimpleName();

    private Toast mToast;

    private Handler mHandler;

    private ChatContract.Presenter mPresenter;

    private ChatMsgAdapter mChatAdapter;

    private EditText mInputText;

    private Button mBtnSend;

    private ListView mListView;

    private TextView mTxtName;

    private TextView mTxtNetwork;

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        mListView.setFocusable(true);
        mListView.setFocusableInTouchMode(true);
        mListView.requestFocus();
        mListView.requestFocusFromTouch();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_LONG);
        mChatAdapter = new ChatMsgAdapter(new ArrayList<ChatMsgInfo>(), mOnChatMsgItemListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_chat, container, false);
        initView(root);
        return root;
    }

    private void initView(View root){
        initToolbar(root);
        mListView = (ListView) root.findViewById(R.id.list_view);
        mListView.setAdapter(mChatAdapter);
        mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        mInputText = (EditText) root.findViewById(R.id.input_text);
        mBtnSend = (Button) root.findViewById(R.id.btn_send_msg);

        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mInputText.getText().toString().equals("")) {
                    mBtnSend.setBackgroundResource(R.drawable.ic_btn_invalid);
                } else {
                    mBtnSend.setBackgroundResource(R.drawable.ic_btn_enable);
                }
            }
        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mInputText.getText().toString().equals("")) {
                    String msg = mInputText.getText().toString();
//                    ChatMsgInfo chatMsgInfo = new ChatMsgInfo(CacheRepository.getInstance().who().getName(), msg, Parm.MSG_IS_SEND);
//                    int i = mChatAdapter.getCount() % 3;
//                    chatMsgInfo.setShowType(i + Parm.MSG_IS_RECEIVE);
//                    mChatAdapter.addItem(chatMsgInfo);
                    mPresenter.sendMsg(msg);
                    mInputText.setText("");
                }
            }
        });
    }
    private void initToolbar(View root){
        root.findViewById(R.id.btn_toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        mTxtName = root.findViewById(R.id.txt__toolbar_user_name);
        mTxtNetwork = root.findViewById(R.id.txt__toolbar_network);

        root.findViewById(R.id.btn__toolbar_phone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), PhoneActivity.class);
                startActivity(intent);
            }
        });
        root.findViewById(R.id.btn__toolbar_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public void setPresenter(ChatContract.Presenter presenter) {
        this.mPresenter = presenter;
    }



    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    public void showTip(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mToast.setText(text);
                mToast.show();
            }
        });
    }

    @Override
    public void clearMsg() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.clear();
            }
        });
    }

    @Override
    public void showMsg(final List<ChatMsgInfo> msgInfoList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.updateList(msgInfoList);
            }
        });

    }

    @Override
    public void showMsg(final ChatMsgInfo chatMsgInfo) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.addItem(chatMsgInfo);
            }
        });
    }

    @Override
    public void notifyChange() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mChatAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void showFriendName(final String name) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtName.setText(name);
            }
        });
    }

    @Override
    public void showFriendNetwork(final String network) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mTxtNetwork.setText(network);
            }
        });
    }

    private ChatMsgAdapter.OnChatMsgItemListener mOnChatMsgItemListener = new ChatMsgAdapter.OnChatMsgItemListener() {
        @Override
        public void onClickItem(ChatMsgInfo item) {

        }

        @Override
        public void onLongClickItem(ChatMsgInfo item) {

        }
    };


}
