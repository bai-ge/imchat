package com.baige.util;


import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by baige on 2018/5/16.
 */

public class PullService {

    private Repository mRepository;

    private TimerTask timerTask;

    private Timer timer;


    public PullService(Repository repository) {
        this.mRepository = repository;
    }

    public void onStart() {
        if(timer == null){
            timer = new Timer();
        }
        if(timerTask == null){
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    pullData();
                }
            };
            timer.schedule(timerTask, 0, 3000);
        }
    }
    public void pullData(){
      List<ChatMsgInfo> chatMsgInfoList  = CacheRepository.getInstance().getChatMessageObservable().loadCache();
        if(chatMsgInfoList == null || chatMsgInfoList.isEmpty()){
            loadMsg();
        }else{
            Collections.sort(chatMsgInfoList);
            loadMsgAfterTime(chatMsgInfoList.get(chatMsgInfoList.size() - 1).getSendTime());
        }
    }

    public void loadMsg() {
        User user = CacheRepository.getInstance().who();
        if (user != null) {
            mRepository.findMsg(user.getId(), user.getVerification(),  new HttpBaseCallback() {
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    Collections.sort(chatMsgInfos);
                    CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfos);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                }
            });
        }
    }


    public void loadMsgAfterTime(long time) {
        User user = CacheRepository.getInstance().who();
        if ( user != null) {
            mRepository.findMsgAfterTime(user.getId(), user.getVerification(), time, new HttpBaseCallback() {
                @Override
                public void loadMsgList(List<ChatMsgInfo> chatMsgInfos) {
                    super.loadMsgList(chatMsgInfos);
                    Collections.sort(chatMsgInfos);
                    CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfos);
                }

                @Override
                public void meaning(String text) {
                    super.meaning(text);
                }
            });
        }
    }

    public void onDestroy() {
        if(timer != null){
            timer.cancel();
        }
    }
}
