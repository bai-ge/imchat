package com.baige.data.observer;

import com.baige.data.entity.FriendView;

import java.util.List;


/**
 * Created by baige on 2018/5/16.
 */

public class FriendViewObservable extends BaseObservable<FriendView> {

    @Override
    public FriendView put(FriendView item) {
        if (item != null) {
            synchronized (this) {
                getCacheMap().put(item.getFriendId(), item);
            }
            setChanged();
            notifyObservers(item);
        }
        return item;
    }


    @Override
    public List<FriendView> put(List<FriendView> items) {
        if (items != null && items.size() > 0) {
            synchronized (this) {
                for (int i = 0; i < items.size(); i++) {
                    getCacheMap().put(items.get(i).getFriendId(), items.get(i));
                }
            }
            setChanged();
            notifyObservers(items);
        }
        return items;
    }
}
