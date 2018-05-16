package com.baige.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.baige.callback.HttpBaseCallback;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;
import com.baige.data.source.local.LocalRepository;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by baige on 2018/5/16.
 */

public class PullService extends Service {

    private Repository mRepository;

    private TimerTask timerTask;

    private Timer timer;
    @Override
    public void onCreate() {
        mRepository = Repository.getInstance(LocalRepository.getInstance(getApplicationContext()));
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
            timer.schedule(timerTask, 0, 10000);
        }
        return super.onStartCommand(intent, flags, startId);
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
                    showTip(text);
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
                    showTip(text);
                }
            });
        }
    }
    protected void showTip(final String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
