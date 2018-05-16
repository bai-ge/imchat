package com.baige.chat;


import android.content.Intent;
import android.os.Bundle;

import com.baige.BaseActivity;
import com.baige.data.entity.FriendView;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.service.PullService;
import com.baige.util.ActivityUtils;


/**
 * Created by 百戈 on 2017/2/19.
 */

public class ChatActivity extends BaseActivity {

    private ChatPresenter mChatPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);

        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), chatFragment, R.id.content_frame);
        }

        mChatPresenter = new ChatPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), chatFragment);
        if (getIntent().getExtras().containsKey("friend")) {
            FriendView friendView = getIntent().getExtras().getParcelable("friend");
            if (friendView != null) {
                mChatPresenter.setFriendView(friendView);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.put("friend", mChatPresenter.getFriendView());
        super.onSaveInstanceState(outState);
    }
}
