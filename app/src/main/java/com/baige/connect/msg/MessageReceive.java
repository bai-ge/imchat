package com.baige.connect.msg;

import android.util.Log;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.p2pcore.ConnectSession;
import com.baige.p2pcore.ConnectorManager;
import com.baige.p2pcore.FileReceiverSession;
import com.baige.p2pcore.FileSenderSession;
import com.baige.telephone.TelePhone;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageReceive {

    private final static String TAG = MessageReceive.class.getSimpleName();

    // 自己接收到消息
    public static void receive(BaseConnector connector, JSONObject json, String from, String to) {
        if (connector == null || json == null) {
            return;
        }
        try {
            if (json.has(Parm.RESPONSE) ) {
                //收到回复消息
                MessageResponse.response(connector, json, from, to);
            } else if (json.has(Parm.DATA_TYPE)) {
                int type = json.getInt(Parm.DATA_TYPE);
                DeviceModel deviceModel = null;
                ResponseMessage responseMessage = null;
                long number = 0;
                String uuid = "";
                if(json.has(Parm.UUID)){
                    uuid = json.getString(Parm.UUID);
                }
                CacheRepository cacheRepository = CacheRepository.getInstance();
                switch (type) {
                    case Parm.TYPE_LOGIN:

                        break;
                    case Parm.TYPE_UDP_TEST:
                        responseMessage = new ResponseMessage();
                        JSONObject dataJson = new JSONObject();
                        dataJson.put(Parm.CODE, Parm.CODE_SUCCESS);
                        if (json.has(Parm.CALLBACK)) {
                            dataJson.put(Parm.CALLBACK, json.getString(Parm.CALLBACK));
                        }
                        if (json.has(Parm.LOCAL_IP)) {
                            dataJson.put(Parm.LOCAL_IP, json.getString(Parm.LOCAL_IP));
                        }
                        if (json.has(Parm.LOCAL_UDP_PORT)) {
                            dataJson.put(Parm.LOCAL_UDP_PORT, json.getString(Parm.LOCAL_UDP_PORT));
                        }
                        if (json.has(Parm.SEND_TIME)) {
                            dataJson.put(Parm.SEND_TIME, json.getString(Parm.SEND_TIME));
                        }
                        dataJson.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
                        dataJson.put(Parm.REMOTE_IP, connector.getAddress().getRemoteIP());
                        dataJson.put(Parm.REMOTE_UDP_PORT, connector.getAddress().getRemotePort());
                        dataJson.put(Parm.FROM, CacheRepository.getInstance().getDeviceId());
                        dataJson.put(Parm.TO, from);
                        responseMessage.setResponse(dataJson);
                        connector.sendString(responseMessage.toJson());
                        deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                        if (deviceModel != null && connector instanceof ConnectedByUDP) {

                            deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                            if (json.has(Parm.LOCAL_IP)) {
                                deviceModel.setLocalIp(json.getString(Parm.LOCAL_IP));
                            }
                            if (json.has(Parm.LOCAL_UDP_PORT)) {
                                deviceModel.setLocalUdpPort(json.getInt(Parm.LOCAL_UDP_PORT));
                            }
                            deviceModel.setRemoteUdpPort(connector.getAddress().getRemotePortIntegerValue());
                            deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
                        }
                        break;
                    case Parm.TYPE_TRY_PTP:

                        if (json.has(Parm.CANDIDATES)) {
                            JSONArray jsonArray = json.getJSONArray(Parm.CANDIDATES);
                            ArrayList<Candidate> candidates = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Candidate candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
                                        jsonArray.getJSONObject(i));
                                if (candidate != null) {
                                    candidates.add(candidate);
                                }
                            }
                            // 建立P2P连接
                            if (candidates != null && candidates.size() > 0) {
                                deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                                if (deviceModel != null) {
                                    deviceModel.setCandidates(candidates);
                                }
                                ConnectorManager.tryPTPConnect(candidates, from, uuid);
                            }
                        }
                        // 回复自己的Candidate
                        responseMessage = new ResponseMessage();
                        responseMessage.setFrom(to);
                        responseMessage.setTo(from);
                        responseMessage.setResponse(MessageManager.sendCandidateTo(from));
                        connector.sendString(responseMessage.toJson());
                        break;
                    case Parm.TYPE_TRY_PTP_CONNECT:

                        if (to.equals(CacheRepository.getInstance().getDeviceId())) {
                            deviceModel = NetServerManager.getInstance().getDeviceModelById(from);
                            if (deviceModel == null) {
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);
                                NetServerManager.getInstance().put(from, deviceModel);
                            }
                            if (connector instanceof ConnectedByUDP) {
                                deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                            }
                            responseMessage = new ResponseMessage();
                            JSONObject jsonObject = MessageManager.tryPTPConnect(to, from, uuid);
                            jsonObject.put(Parm.CODE, Parm.CODE_SUCCESS);
                            responseMessage.setResponse(jsonObject);
                            connector.sendString(responseMessage.toJson());
                            Log.d(TAG, "已经建立P2P 连接：id =" + from + ", connetor =" + connector.getAddress().getStringRemoteAddress());

                            if(!Tools.isEmpty(uuid)){
//                                TODO 保存p2p连接连接对象
//                                ConnectorManager.getInstance().add(connector);
                                ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                                if(session != null){
                                    FileSenderSession fileSenderSession = (FileSenderSession) session.get(FileSenderSession.TAG);
                                    if(fileSenderSession != null && session.isWaiting()){
                                        session.start();
                                        fileSenderSession.start((ConnectedByUDP) connector);
                                    }
                                    //不能在此开始下载
//                                    FileReceiverSession fileReceiverSession = (FileReceiverSession) session.get(FileReceiverSession.TAG);
//                                    if(fileReceiverSession != null ){
//                                        synchronized (session){
//                                            if(session.isWaiting()){
//                                                session.start();
//                                                fileReceiverSession.start((ConnectedByUDP) connector);
//                                            }
//                                            fileReceiverSession.startDownload();
//                                        }
//                                    }
                                }
                            }
                            TelePhone.getInstance().showLog("P2P 连接成功 " + connector.getAddress().getStringRemoteAddress());
                            CacheRepository.getInstance().setP2PConnectSuccess(true);
                        }
                        break;

                    //*******************************文件操作
                    case Parm.TYPE_START_DOWNLOAD:
                        Log.i(TAG, "开始发送文件");
                        if(json.has(Parm.UUID)){
                            uuid = json.getString(Parm.UUID);
                            ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                            if(session != null){
                                FileSenderSession senderSession = (FileSenderSession) session.get(FileSenderSession.TAG);
                                if(senderSession != null && session.isRunning()){
                                    senderSession.startSend();
                                }else{
                                    Log.e(TAG, "发送文件的会话为空,或没有建立P2P连接");
                                }
                            }else{
                                Log.e(TAG, "收到开始下载文件信号，但无法找到会话");
                            }
                        }
                        break;
                    case Parm.TYPE_ASK_PACKET:
                        if(json.has(Parm.NUMBER)){
                            number = json.getLong(Parm.NUMBER);
                        }
                        if(json.has(Parm.UUID)){
                            uuid = json.getString(Parm.UUID);
                            ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                            if(session != null){
                                FileSenderSession senderSession = (FileSenderSession) session.get(FileSenderSession.TAG);
                                if(senderSession != null){
                                    senderSession.askPacket(uuid, number);
                                }else{
                                    Log.e(TAG, "发送文件的会话为空");
                                }
                            }else{
                                Log.e(TAG, "收到文件请求信号，但无法找到会话");
                            }
                        }
                        break;
                    case Parm.TYPE_RESPONE_AFFIRM:
                        if(json.has(Parm.NUMBER)){
                            number = json.getInt(Parm.NUMBER);
                        }
                        if(json.has(Parm.UUID)){
                            uuid = json.getString(Parm.UUID);
                            ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                            if(session != null){
                                FileSenderSession senderSession = (FileSenderSession) session.get(FileSenderSession.TAG);
                                if(senderSession != null){
                                    senderSession.affirmReceive(uuid, number);
                                }else{
                                    Log.e(TAG, "发送文件的会话为空");
                                }
                            }else{
                                Log.e(TAG, "收到文件回复确认信号，但无法找到会话");
                            }
                        }
                        break;

                    case Parm.TYPE_FINISH_DOWNLOAD:
                        if(json.has(Parm.UUID)){
                            uuid = json.getString(Parm.UUID);
                            ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                            if(session != null){
                                FileSenderSession senderSession = (FileSenderSession) session.get(FileSenderSession.TAG);
                                if(senderSession != null){
                                    senderSession.finish();
                                    session.remove(FileSenderSession.TAG);
                                    session.destroy();
                                }else{
                                    Log.e(TAG, "发送文件的会话为空");
                                }
                            }else{
                                Log.e(TAG, "收到文件传输完成信号，但无法找到会话");
                            }
                        }
                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
