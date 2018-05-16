package com.baige.data.observer;

import com.baige.data.entity.ChatMsgInfo;

import java.util.List;


/**
 * Created by baige on 2018/5/15.
 */

public class ChatMessageObservable extends BaseObservable<ChatMsgInfo>{


    @Override
    public ChatMsgInfo put(ChatMsgInfo item) {
        if(item != null){
            synchronized (this){
                getCacheMap().put(item.getRemark(), item);
            }
            setChanged();
            notifyObservers(item);
        }
        return item;
    }

    @Override
    public List<ChatMsgInfo> put(List<ChatMsgInfo> items) {
        if(items != null && items.size() > 0){
            synchronized (this){
                for (int i = 0; i < items.size(); i++) {
                    getCacheMap().put(items.get(i).getRemark(), items.get(i));
                }
            }
            setChanged();
            notifyObservers(items);
        }
        return items;
    }

}
