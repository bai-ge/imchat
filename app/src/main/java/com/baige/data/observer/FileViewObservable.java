package com.baige.data.observer;

import com.baige.data.entity.FileView;
import java.util.List;


/**
 * Created by baige on 2018/5/16.
 */

public class FileViewObservable extends BaseObservable<FileView> {

    @Override
    public FileView put(FileView item) {
        if (item != null) {
            synchronized (this) {
                getCacheMap().put(item.getRemark(), item);
            }
            setChanged();
            notifyObservers(item);
        }
        return item;
    }


    @Override
    public List<FileView> put(List<FileView> items) {
        if (items != null && items.size() > 0) {
            synchronized (this) {
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
