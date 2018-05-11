package com.baige.connect.msg;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baige on 2018/3/26.
 */

public class ResponseMessage {
    private int code;
    private Object data;

    public String toJson(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.CODE, code);
            jsonObject.put(Parm.DATA, data);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
