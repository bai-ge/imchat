package com.baige.callback;

import com.baige.common.Parm;
import com.baige.data.entity.User;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/2/13.
 */

public class UserResponseBinder extends AbstractResponseBinder {
    private final static String TAG = UserResponseBinder.class.getCanonicalName();
    @Override
    public void parse(String json, HttpBaseCallback callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);
                    JSONObject userJson;
                    //TODO 可能去掉MSG
                    if(jsonObject.has(Parm.MEAN)){
                        String text = jsonObject.getString(Parm.MEAN);
                        if(!Tools.isEmpty(text)){
                            callBack.meaning(text);
                        }
                    }

                    if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.USER)){
                        userJson = jsonObject.getJSONObject(Parm.USER);
                        User user = User.createByJson(userJson);
                        if(user != null){
                            callBack.loadAUser(user);
                        }else{
                            callBack.notFind();
                        }
                    }else if(codeNum == Parm.CODE_SUCCESS && jsonObject.has(Parm.USERS)){
                        JSONArray jsonArray = jsonObject.getJSONArray(Parm.USERS);
                        List<User> users = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            userJson = jsonArray.getJSONObject(i);
                            User user = User.createByJson(userJson);
                            if(user != null){
                                users.add(user);
                            }
                        }
                        if(users.size() > 0){
                            callBack.loadUsers(users);
                        }else{
                            callBack.notFind();
                        }
                    }
                    callbackCode(callBack, codeNum);
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
