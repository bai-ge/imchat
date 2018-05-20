package com.baige.callback;

import com.baige.common.Parm;
import com.baige.data.entity.FileView;
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

public class FileResponseBinder extends AbstractResponseBinder {

    @Override
    public void parse(String json, HttpBaseCallback callBack) {
        checkNotNull(callBack);
        if(!Tools.isEmpty(json)){
            callBack.onResponse();
            try {
                JSONObject jsonObject = new JSONObject(json);
                if(jsonObject.has(Parm.CODE)){
                    int codeNum = jsonObject.getInt(Parm.CODE);
                    JSONObject fileJson;
                    //TODO 可能去掉MSG
                    if(jsonObject.has(Parm.MEAN)){
                        String text = jsonObject.getString(Parm.MEAN);
                        if(!Tools.isEmpty(text)){
                            callBack.meaning(text);
                        }
                    }

                    if(codeNum == Parm.SUCCESS_CODE && jsonObject.has(Parm.FILE)){
                        fileJson = jsonObject.getJSONObject(Parm.FILE);
                        FileView fileView = FileView.createByJson(fileJson);
                        if(fileView != null){
                            callBack.loadFile(fileView);
                        }else{
                            callBack.notFind();
                        }
                    }else if(codeNum == Parm.SUCCESS_CODE && jsonObject.has(Parm.FILES)){
                        JSONArray jsonArray = jsonObject.getJSONArray(Parm.FILES);
                        List<FileView> friendViews = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            fileJson = jsonArray.getJSONObject(i);
                            FileView f = FileView.createByJson(fileJson);
                            if(f != null){
                                friendViews.add(f);
                            }
                        }
                        if(friendViews.size() > 0){
                            callBack.loadFiles(friendViews);
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
}
