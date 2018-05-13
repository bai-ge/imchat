package com.baige.callback;



import com.baige.common.Parm;
import com.baige.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2018/2/13.
 */

public class SimpleResponseBinder extends AbstractResponseBinder {

    @Override
    public void parse(String json, HttpBaseCallback callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);

                    //TODO 可能去掉MSG
                    if(jsonObject.has(Parm.MEAN)){
                        String text = jsonObject.getString(Parm.MEAN);
                        if(!Tools.isEmpty(text)){
                            callBack.meaning(text);
                        }
                    }
                    callbackCode(callBack, codeNum);
                    //TODO 暂时仅处理返回码
                }
            } catch (JSONException e) {
                e.printStackTrace();
                callBack.fail();
            }
        }
    }
}
