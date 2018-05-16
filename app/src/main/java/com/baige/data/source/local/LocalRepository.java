package com.baige.data.source.local;

import android.content.Context;
import android.support.annotation.NonNull;

import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.source.DataSource;

import rx.Observable;
import rx.Subscriber;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/4.
 */

public class LocalRepository implements DataSource {

    private final static String TAG = LocalRepository.class.getCanonicalName();

    private static LocalRepository INSTANCE;

    private LocalRepository(@NonNull Context context) {
        checkNotNull(context);
    }

    public static LocalRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            synchronized (LocalRepository.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new LocalRepository(context);
                }
            }
        }
        return INSTANCE;
    }




}
