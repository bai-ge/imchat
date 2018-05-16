package com.baige.data.observer;


import android.util.LruCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by baige on 2018/5/16.
 */

public abstract class BaseObservable<T> extends Observable {
    private Map<Object, T> cacheMap;

//    private LruCache<Object, T> cacheMap; //TODO 采用最近最久未被使用，但不符合经常需求大量全部读取的情况

    protected Map<Object, T> getCacheMap() {
        if (cacheMap == null) {
            cacheMap = Collections.synchronizedMap(new LinkedHashMap<Object, T>());
        }
        return cacheMap;
    }

    public void clear() {
        synchronized (this) {
            getCacheMap().clear();
        }
        setChanged();
        notifyObservers();
    }

    public List<T> loadCache() {
        ArrayList<T> list = new ArrayList<>(getCacheMap().values());
        return list;
    }

    public void remote(Object key) {
        synchronized (this) {
            if (key != null) {
                getCacheMap().remove(key);
                setChanged();
                notifyObservers();
            }
        }

    }

    public T get(Object key) {
        return getCacheMap().get(key);
    }

    public boolean containsKey(Object key) {
        return getCacheMap().containsKey(key);
    }

    public int size() {
        return getCacheMap().size();
    }


    public abstract T put(T item);

    public abstract List<T> put(List<T> items);

}
