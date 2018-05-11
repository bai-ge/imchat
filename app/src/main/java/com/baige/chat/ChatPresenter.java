package com.baige.chat;


import android.support.annotation.NonNull;

import com.baige.data.source.Repository;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by 百戈 on 2017/2/19.
 */

public class ChatPresenter implements ChatContract.Presenter {

    private final Repository mRepository;

    private final ChatFragment mChatFragment;


    public ChatPresenter( @NonNull Repository dateRepository, @NonNull ChatFragment chatWithMeFragment) {
        this.mRepository = checkNotNull(dateRepository);
        this.mChatFragment = checkNotNull(chatWithMeFragment);
        mChatFragment.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
