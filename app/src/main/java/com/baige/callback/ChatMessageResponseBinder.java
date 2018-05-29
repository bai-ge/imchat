package com.baige.callback;

import com.baige.common.Parm;
import com.baige.data.entity.ChatMsgInfo;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/5/14.
 */

public class ChatMessageResponseBinder extends AbstractResponseBinder {

    @Override
    public void parse(String json, HttpBaseCallback callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);
                    JSONObject chatJson;
                    //TODO 可能去掉MSG
                    if(jsonObject.has(Parm.MEAN)){
                        String text = jsonObject.getString(Parm.MEAN);
                        if(!Tools.isEmpty(text)){
                            callBack.meaning(text);
                        }
                    }

                    if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.CHAT)){
                        chatJson = jsonObject.getJSONObject(Parm.CHAT);
                        ChatMsgInfo chatMsgInfo = ChatMsgInfo.createByJson(chatJson);
                        if(chatMsgInfo != null){
                            callBack.loadMsg(chatMsgInfo);
                        }else{
                            callBack.notFind();
                        }
                    }else if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.CHAT_LIST)){
                        JSONArray jsonArray = jsonObject.getJSONArray(Parm.CHAT_LIST);
                        List<ChatMsgInfo> chatMsgInfos = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            chatJson = jsonArray.getJSONObject(i);
                            ChatMsgInfo chatMsgInfo = ChatMsgInfo.createByJson(chatJson);
                            if(chatMsgInfo != null){
                                chatMsgInfos.add(chatMsgInfo);
                            }
                        }
                        if(chatMsgInfos.size() > 0){
                            callBack.loadMsgList(chatMsgInfos);
                        }else{
                            callBack.notFind();
                        }
                    } else{
                        callbackCode(callBack, codeNum);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.fail();
            }
        }
    }

    @Override
    public void parse(String json, PushCallback callback) {

    }
}
