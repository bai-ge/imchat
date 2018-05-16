package com.baige.data.observer;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by baige on 2018/5/16.
 */

public  class BaseObserver implements Observer{

    @Override
    public  void update(Observable o, Object arg) {
        Log.d("observer", "Base observer:"+arg);
        if(o instanceof ChatMessageObservable){
            update((ChatMessageObservable) o, arg);
        }else if(o instanceof FriendViewObservable){
            update((FriendViewObservable) o, arg);
        }else if(o instanceof LastChatMessageObservable){
            update((LastChatMessageObservable) o, arg);
        }
    }


    public void update(ChatMessageObservable observable, Object arg) {

    }


    public void update(FriendViewObservable observable, Object arg) {

    }


    public void update(LastChatMessageObservable observable, Object arg) {

    }
}
