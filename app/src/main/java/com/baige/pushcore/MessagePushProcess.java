package com.baige.pushcore;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.baige.callback.BaseCallback;
import com.baige.callback.CallbackManager;
import com.baige.common.Parm;
import com.baige.connect.NetServerManager;
import com.baige.connect.msg.MessageManager;

import com.baige.connect.msg.MessageManagerOfFile;
import com.baige.connect.msg.ResponseMessage;
import com.baige.data.dao.FileDAO;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.entity.FriendView;
import com.baige.data.source.cache.CacheRepository;
import com.baige.p2pcore.ConnectSession;
import com.baige.p2pcore.ConnectorManager;
import com.baige.p2pcore.FileReceiverSession;
import com.baige.p2pcore.FileSenderSession;
import com.baige.telephone.PhoneActivity;
import com.baige.telephone.TelePhone;
import com.baige.util.FileUtils;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;


public class MessagePushProcess {

    private final static String TAG = MessagePushProcess.class.getCanonicalName();



    /**
     * IPush 或JPush 收到数据（TCP连接）
     *
     * @param context
     * @param json
     */
    public static void receive(Context context, JSONObject json) {
        if (context == null || json == null) {
            return;
        }
        String from = null;
        String to = null;
        String name = "";
        String uuid = "";
        int uid = 0;
        DeviceModel deviceModel;
        Log.v(TAG, "TCP收到 :" + json);

        try {
            if (json.has(Parm.FROM)) {
                from = json.getString(Parm.FROM);
            }
            if (json.has(Parm.TO)) {
                to = json.getString(Parm.TO);
                if (Tools.isEmpty(to) || !to.equals(CacheRepository.getInstance().getDeviceId())) {
                    return;
                }
            }
            if(json.has(Parm.UUID)){
                uuid = json.getString(Parm.UUID);
            }
            if (json.has(Parm.RESPONSE)) {
                response(context, json, from, to); //回复信息
            } else {
                if (json.has(Parm.DATA_TYPE)) {
                    int type = json.getInt(Parm.DATA_TYPE);
                    switch (type) {
                        case Parm.TYPE_CALL_TO:
                            if (json.has(Parm.USERNAME)) {
                                name = json.getString(Parm.USERNAME);
                            }
                            if(json.has(Parm.UID)){
                                uid = json.getInt(Parm.UID);
                            }
                            if (!TelePhone.getInstance().isLeisure()) {
                                ResponseMessage responseMessage = new ResponseMessage();
                                json.put(Parm.CODE, Parm.CODE_BUSY);
                                responseMessage.setResponse(json);
                                responseMessage.setFrom(to);
                                responseMessage.setTo(from);
                                SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                            } else {
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                       ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                    }

                                }

                                TelePhone.getInstance().setTalkWithDevice(deviceModel);
                                TelePhone.getInstance().afxBeCall(from, name);
                                //TODO 被呼叫
                                Intent intent = new Intent(context, PhoneActivity.class);
                                FriendView friendView = CacheRepository.getInstance().getFriendViewObservable().get(uid);
                                if(friendView == null){
                                    friendView = new FriendView();
                                    friendView.setFriendId(uid);
                                    friendView.setName(name);
                                }
                                intent.putExtra(Parm.FRIEND, friendView);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }

                            break;
                        case Parm.TYPE_REPLY_CALL_TO:

                            if (!Tools.isEmpty(from) && Tools.isEquals(from, TelePhone.getInstance().getTalkWithId())) {
                                //对方已经收到
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);

                                if (json.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        deviceModel.setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                        ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                    }
                                }

                                TelePhone.getInstance().setTalkWithDevice(deviceModel);
                                JSONObject jsonMsg = MessageManager.sendCandidateTo(from);
                                if (jsonMsg != null) {
                                    SendMessageBroadcast.getInstance().sendMessage(jsonMsg.toString());
                                }
                                if (TelePhone.getInstance().isCalling()) {
                                    TelePhone.getInstance().connectSuccess();
                                }
                            }
                            break;
                        case Parm.TYPE_TRY_PTP:
                            if (json.has(Parm.CANDIDATES)) {
                                JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                ArrayList<Candidate> candidates = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                    if (candidate != null) {
                                        candidates.add(candidate);
                                    }
                                }
                                if (candidates != null && candidates.size() > 0) {
                                    if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                        TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                        Log.d(TAG, "传输Candidates" + candidates.toString());
                                    }
                                    ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                }
                            }


                            //回复自己的Candidate
                            ResponseMessage responseMessage = new ResponseMessage();
                            responseMessage.setFrom(to);
                            responseMessage.setTo(from);
                            JSONObject resObject = MessageManager.sendCandidateTo(from);
                            if(!Tools.isEmpty(uuid)){
                                resObject.put(Parm.UUID, uuid);
                            }
                            responseMessage.setResponse(resObject);
                            SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                            Log.v(TAG, "发送PTP:" + responseMessage.toJson());

                            break;
                        case Parm.TYPE_PICK_UP:
                            if (from.equals(TelePhone.getInstance().getTalkWithId())) {
                                TelePhone.getInstance().canTalk();
                            }
                            break;
                        case Parm.TYPE_HANG_UP:
                            if (from.equals(TelePhone.getInstance().getTalkWithId())) {
                                TelePhone.getInstance().stop();
                            }
                            break;

                        //***************************文件传输
                        case Parm.TYPE_DOWNLOAD_FILE:
                            String path = null;
                            String fileName = null;
                            if(json.has(FileDAO.FILE_NAME)){
                                fileName = json.getString(FileDAO.FILE_NAME);
                            }
                            if(json.has(FileDAO.FILE_PATH)){
                                path = json.getString(FileDAO.FILE_PATH);
                                File file = new File(path);
                                if(file.exists()){

                                    NetServerManager.tryUdpTest();
                                    //新建会话
                                    ConnectSession connectSession = new ConnectSession();
                                    connectSession.setUUID(uuid);
                                    FileSenderSession fileSenderSession = new FileSenderSession(uuid, fileName, FileUtils.getParent(path));
                                    connectSession.put(FileSenderSession.TAG, fileSenderSession);
                                    ConnectorManager.getInstance().add(connectSession);


                                    responseMessage = new ResponseMessage();
                                    responseMessage.setFrom(to);
                                    responseMessage.setTo(from);
                                    responseMessage.setResponse(MessageManagerOfFile.responseDownloadFile(uuid, true, fileName, path, file.length()));
                                    SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());

                                    if (json.has(Parm.CANDIDATES)) {
                                        JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                                        ArrayList<Candidate> candidates = new ArrayList<>();
                                        for (int i = 0; i < jsonArray.length(); i++) {
                                            Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                            if (candidate != null) {
                                                candidates.add(candidate);
                                            }
                                        }
                                        if (candidates != null && candidates.size() > 0) {
//                                    deviceModel.setCandidates(candidates);
                                            Log.d(TAG, "传输Candidates" + candidates.toString());
                                            ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                        }
                                    }


                                }else{
                                    responseMessage = new ResponseMessage();
                                    responseMessage.setFrom(to);
                                    responseMessage.setTo(from);
                                    responseMessage.setResponse(MessageManagerOfFile.responseDownloadFile(uuid, false, fileName, path, 0));
                                    SendMessageBroadcast.getInstance().sendMessage(responseMessage.toJson());
                                }
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**IPush 或JPush 收到数据（TCP连接）
     * @param context
     * @param json
     */
    public static void response(Context context, JSONObject json, String from, String to) {
        if (context == null || json == null) {
            return;
        }
        try {
            if (json.has(Parm.RESPONSE)) {
                JSONObject dataJson = json.getJSONObject(Parm.RESPONSE);
                int code = 0;
                int type = 0;
                String uuid = "";

                if (dataJson.has(Parm.DATA_TYPE)) {
                    type = dataJson.getInt(Parm.DATA_TYPE);
                    if(dataJson.has(Parm.CODE)){
                        code = dataJson.getInt(Parm.CODE);
                    }
                    if (dataJson.has(Parm.FROM)) {
                        from = dataJson.getString(Parm.FROM);
                    }
                    if (dataJson.has(Parm.TO)) {
                        to = dataJson.getString(Parm.TO);
                    }
                    if(dataJson.has(Parm.UUID)){
                        uuid = dataJson.getString(Parm.UUID);
                    }
                    switch (type) {
                        case Parm.TYPE_TRY_PTP:
                            from = dataJson.getString(Parm.FROM);
                            to = dataJson.getString(Parm.TO);
                            if (!Tools.isEmpty(to) && to.equals(CacheRepository.getInstance().getDeviceId())) {

                                if (dataJson.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                            TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                            Log.d(TAG, "传输Candidates" + candidates.toString());
                                        }
                                       ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                    }
                                    //TODO 尝试建立P2P连接
                                }
                            }
                            break;
                        case Parm.TYPE_CALL_TO:
                            if (code == Parm.CODE_NOTFIND) {
                                //服务器转发失败

                            } else if (code == Parm.CODE_BUSY) {
                                //对方正在通话中
                                if (TelePhone.getInstance().isCalling()) {
                                    TelePhone.getInstance().oppBusy();
                                }
                            }
                            break;

                        //*************************文件传输

                        case Parm.TYPE_DOWNLOAD_FILE:
                            String path = null;
                            String fileName = null;
                            long fileSize = 0;

                            if(dataJson.has(FileDAO.FILE_NAME)){
                                fileName = dataJson.getString(FileDAO.FILE_NAME);
                            }
                            if(dataJson.has(FileDAO.FILE_PATH)){
                                path = dataJson.getString(FileDAO.FILE_PATH);
                            }
                            if(dataJson.has(FileDAO.FILE_SIZE)){
                                fileSize = dataJson.getLong(FileDAO.FILE_SIZE);
                            }
                            if(code == Parm.CODE_SUCCESS){

//                                //新建会话, 这里应该在发送下载文件请求时已经创建
//                                ConnectSession connectSession = new ConnectSession();
//                                connectSession.setUUID(uuid);
//                                FileReceiverSession fileReceiverSession = new FileReceiverSession(uuid, fileName, fileSize);
//                                connectSession.put(FileSenderSession.TAG, fileReceiverSession);
//                                ConnectorManager.getInstance().add(connectSession);


                                //文件存在，并且对方同意
                                Log.d(TAG, "文件存在，开始建立P2P连接");
                                if (dataJson.has(Parm.CANDIDATES)) {
                                    JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
                                    ArrayList<Candidate> candidates = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class, jsonArray.getJSONObject(i));
                                        if (candidate != null) {
                                            candidates.add(candidate);
                                        }
                                    }
                                    if (candidates != null && candidates.size() > 0) {
                                        if (TelePhone.getInstance().getTalkWithDevice() != null) {
                                            TelePhone.getInstance().getTalkWithDevice().setCandidates(candidates);
                                            Log.d(TAG, "传输Candidates" + candidates.toString());
                                        }
                                        ConnectorManager.tryPTPConnect(candidates, from, uuid);
                                    }
                                }

                            }else{
                                //TODO 文件不存在
                                Log.d(TAG, "文件不存在");
                            }
                        default:
                            break;
                    }
                }
                if (dataJson.has(Parm.CALLBACK)) {
                    BaseCallback callBack = CallbackManager.getInstance().get(dataJson.getString(Parm.CALLBACK));
                    callBack.response(json.toString());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
