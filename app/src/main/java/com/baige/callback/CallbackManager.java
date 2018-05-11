package com.baige.callback;


import com.baige.util.Tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;

/**
 * Created by baige on 2018/2/13.
 */

public class CallbackManager {
    private final static String TAG = CallbackManager.class.getCanonicalName();

    private static CallbackManager INSTANCE = null;

    private Map<String, BaseCallback> mCallBackMap;

    private Timer mTimer;

    private CallbackManager(){
        mCallBackMap = Collections.synchronizedMap(new LinkedHashMap<String, BaseCallback>());
        mTimer = new Timer();
    }

    public static CallbackManager getInstance() {
        if (INSTANCE == null) {
            synchronized (CallbackManager.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new CallbackManager();
                }
            }
        }
        return INSTANCE;
    }

    public void put(BaseCallback baseCallBack){
        if(baseCallBack != null && !Tools.isEmpty(baseCallBack.getId())){
            synchronized (mCallBackMap){
                mCallBackMap.put(baseCallBack.getId(), baseCallBack);
                if(baseCallBack.getTimeout() <= 0){
                    baseCallBack.setTimeout(30000);
                }
                mTimer.schedule(baseCallBack.getTimerTask(), baseCallBack.getTimeout());
            }
        }
    }

    public BaseCallback get(String id){
        if(!Tools.isEmpty(id)){
           return mCallBackMap.get(id);
        }
        return null;
    }
    public BaseCallback remote(String id){
        if(!Tools.isEmpty(id)){
            return mCallBackMap.remove(id);
        }
        return null;
    }
}
