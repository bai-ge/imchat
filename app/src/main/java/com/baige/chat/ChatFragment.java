package com.baige.chat;

import android.content.Intent;
import android.os.Bundle;
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


import com.baige.adapter.ChatMsgAdapter;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.imchat.R;
import com.baige.telephone.PhoneActivity;

import java.util.ArrayList;


/**
 * Created by 百戈 on 2017/2/19.
 */


public class ChatFragment extends Fragment implements ChatContract.View {

    private ChatContract.Presenter mPresenter;

    private ChatMsgAdapter mChatAdapter;

    private EditText mInputText;

    private Button mBtnSend;

    private ListView mListView;

    private TextView mTextName;

    private TextView mTextNetwork;

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
        mTextName = root.findViewById(R.id.txt__toolbar_user_name);
        mTextNetwork = root.findViewById(R.id.txt__toolbar_network);

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


    private ChatMsgAdapter.OnChatMsgItemListener mOnChatMsgItemListener = new ChatMsgAdapter.OnChatMsgItemListener() {
        @Override
        public void onClickItem(ChatMsgInfo item) {

        }

        @Override
        public void onLongClickItem(ChatMsgInfo item) {

        }
    };


}
