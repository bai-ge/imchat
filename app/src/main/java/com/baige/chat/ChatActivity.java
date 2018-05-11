package com.baige.chat;


import android.os.Bundle;

import com.baige.BaseActivity;
import com.baige.data.source.Repository;
import com.baige.data.source.local.LocalRepository;
import com.baige.imchat.R;
import com.baige.util.ActivityUtils;



/**
 * Created by 百戈 on 2017/2/19.
 */

public class ChatActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_common);

        ChatFragment chatFragment = (ChatFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (chatFragment == null) {
            chatFragment = ChatFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), chatFragment, R.id.content_frame);
        }

        ChatPresenter chatPresenter = new ChatPresenter(Repository.getInstance(LocalRepository.getInstance(getApplicationContext())), chatFragment);
    }
}
