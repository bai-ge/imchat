package com.baige.p2pcore;

import com.baige.util.Tools;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by baige on 2018/5/28.
 */

public class ConnectSession {

    public final static int WAITING = 0;
    public final static int RUNNING = 1;
    public final static int DESTROY = 2;

    private String uuid;

    private Runnable startRunnable;
    private Runnable destroyRunnable;

    private int state;

    private Map<String, Object> content;

    public ConnectSession(){
        content = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
    }

    public Object put(String key, Object obj){
        synchronized (content){
            if(Tools.isEmpty(key)){
                return null;
            }
            return content.put(key, obj);
        }
    }

    public Object remove(String key){
        synchronized (content){
            return content.remove(key);
        }
    }
    public Object get(String key){
        return content.get(key);
    }

    public synchronized void start(){
        if(isWaiting()){
            state = RUNNING;
            if(startRunnable != null){
                ConnectorManager.getInstance().submit(startRunnable);
            }
        }
    }



    public synchronized void destroy(){
        if(!isDestroy()){
            state = DESTROY;
            if(destroyRunnable != null){
                ConnectorManager.getInstance().submit(destroyRunnable);
            }
            ConnectorManager.getInstance().remove(this);
        }
    }

    public int getState() {
        return state;
    }

    public boolean isWaiting(){
        return state == WAITING;
    }
    public boolean isRunning(){
        return state == RUNNING;
    }
    public boolean isDestroy(){
        return state == DESTROY;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public Runnable getStartRunnable() {
        return startRunnable;
    }

    public void setStartRunnable(Runnable startRunnable) {
        this.startRunnable = startRunnable;
    }

    public Runnable getDestroyRunnable() {
        return destroyRunnable;
    }

    public void setDestroyRunnable(Runnable destroyRunnable) {
        this.destroyRunnable = destroyRunnable;
    }
}
