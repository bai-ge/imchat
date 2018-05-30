package com.baige.connect.msg;

import com.baige.common.Parm;
import com.baige.data.dao.FileDAO;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.FileView;
import com.baige.p2pcore.ConnectorManager;
import com.baige.util.JsonTools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baige on 2018/5/29.
 */

public class MessageManagerOfFile {
    //文件传输

    public static String askDownloadFile(String from, String to, FileView fileView, String uuid, int slipWindowCount){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_DOWNLOAD_FILE);
            jsonObject.put(FileDAO.FILE_NAME, fileView.getFileName());
            jsonObject.put(FileDAO.FILE_PATH, fileView.getFilePath());
            jsonObject.put(Parm.UUID, uuid);
            jsonObject.put(Parm.SLIP_WINDOW_COUNT, slipWindowCount);
            ArrayList<Candidate> candidates = ConnectorManager.getInstance().getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject responseDownloadFile(String uuid, boolean isSuccess, String fileName, String filePath, long fileSize){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_DOWNLOAD_FILE);
            jsonObject.put(Parm.FILE_NAME, fileName);
            jsonObject.put(FileDAO.FILE_PATH, filePath);
            jsonObject.put(Parm.UUID, uuid);
            if(isSuccess){
                jsonObject.put(Parm.CODE, Parm.CODE_SUCCESS);
                jsonObject.put(FileDAO.FILE_SIZE, fileSize);
                ArrayList<Candidate> candidates = ConnectorManager.getInstance().getCandidates();
                if (candidates != null && candidates.size() > 0) {
                    JSONArray jsonArray = new JSONArray();
                    for (Candidate candidate : candidates){
                        jsonArray.put(JsonTools.getJSON(candidate));
                    }
                    jsonObject.put(Parm.CANDIDATES, jsonArray);
                }
            }else{
                jsonObject.put(Parm.CODE, Parm.CODE_FAIL);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject responeAffirm(String from, String to, String uuid, long number){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_RESPONE_AFFIRM);
            jsonObject.put(Parm.UUID, uuid);
            jsonObject.put(Parm.NUMBER, number);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject askPacket(String from, String to, String uuid, long number){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_ASK_PACKET);
            jsonObject.put(Parm.UUID, uuid);
            jsonObject.put(Parm.NUMBER, number);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject startDownload(String from, String to, String uuid){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_START_DOWNLOAD);
            jsonObject.put(Parm.UUID, uuid);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject finishDownload(String from, String to, String uuid){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_FINISH_DOWNLOAD);
            jsonObject.put(Parm.UUID, uuid);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject headerPacket(String from, String to, String uuid, long number, long current){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_FILE);
            jsonObject.put(Parm.UUID, uuid);
            jsonObject.put(Parm.NUMBER, number);
            jsonObject.put(Parm.CURRENT, current);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
