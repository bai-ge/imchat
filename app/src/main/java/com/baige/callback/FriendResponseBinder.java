package com.baige.callback;

import com.baige.common.Parm;
import com.baige.data.entity.FriendView;
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

public class FriendResponseBinder extends AbstractResponseBinder {

    @Override
    public void parse(String json, HttpBaseCallback callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);
                    JSONObject friendJson;
                    //TODO 可能去掉MSG
                    if(jsonObject.has(Parm.MEAN)){
                        String text = jsonObject.getString(Parm.MEAN);
                        if(!Tools.isEmpty(text)){
                            callBack.meaning(text);
                        }
                    }

                    if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.FRIEND)){
                        friendJson = jsonObject.getJSONObject(Parm.FRIEND);
                        FriendView friendView = FriendView.createByJson(friendJson);
                        if(friendView != null){
                            callBack.loadFriendView(friendView);
                        }else{
                            callBack.notFind();
                        }
                    }else if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.FRIENDS)){
                        JSONArray jsonArray = jsonObject.getJSONArray(Parm.FRIENDS);
                        List<FriendView> friendViews = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            friendJson = jsonArray.getJSONObject(i);
                            FriendView f = FriendView.createByJson(friendJson);
                            if(f != null){
                                friendViews.add(f);
                            }
                        }
                        if(friendViews.size() > 0){
                            callBack.loadFriendViews(friendViews);
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
