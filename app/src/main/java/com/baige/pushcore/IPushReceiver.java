package com.baige.pushcore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.baige.common.Parm;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by baige on 2018/3/27.
 */

public class IPushReceiver extends BroadcastReceiver {
    private final static String TAG = IPushReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        if(action.equals(SendMessageBroadcast.ACTION_RECEIVE_MSG)){
            if (bundle.containsKey(SendMessageBroadcast.KEY_RECEIVE_MSG)) {
                String msg = bundle.getString(SendMessageBroadcast.KEY_RECEIVE_MSG);
                if(!Tools.isEmpty(msg)){
                    try {
                        JSONObject jsonObject = new JSONObject(msg);
                        if(jsonObject.has(Parm.CHAT)){
                            JSONObject chatJson = jsonObject.getJSONObject(Parm.CHAT);
                            ChatMsgInfo chatMsgInfo = (ChatMsgInfo) JsonTools.toJavaBean(ChatMsgInfo.class, chatJson);
                            if(chatMsgInfo != null){
                                CacheRepository.getInstance().getChatMessageObservable().put(chatMsgInfo);
                            }
                        }
                        MessageProcess.receive(context, jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if(action.equals(SendMessageBroadcast.ACTION_SEND_MSG_FIAL)){
            if (bundle.containsKey(SendMessageBroadcast.KEY_SEND_MSG)) {
                String msg = bundle.getString(SendMessageBroadcast.KEY_RECEIVE_MSG);
                String serverIP = bundle.getString(SendMessageBroadcast.KEY_SERVER_IP);
                if(!Tools.isEmpty(msg)){
                    ConnectedByUDP connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(serverIP, 12059);
                    connectedByUDP.sendString(msg);
                }
            }
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
        }
        return sb.toString();
    }
}
