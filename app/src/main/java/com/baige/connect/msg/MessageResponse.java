package com.baige.connect.msg;

import android.util.Log;

import com.baige.callback.BaseCallback;
import com.baige.callback.CallbackManager;
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

public class MessageResponse {

    private final static String TAG = MessageResponse.class.getSimpleName();
    // 收到回复
    public static void response(BaseConnector connector, JSONObject json, String from, String to) {
        if (connector == null || json == null) {
            return;
        }

        try {
            if (json.has(Parm.RESPONSE)) {
                JSONObject dataJson = json.getJSONObject(Parm.RESPONSE);
                String uuid = "";
                if (dataJson.has(Parm.FROM)) {
                    from = dataJson.getString(Parm.FROM);
                }
                if (dataJson.has(Parm.TO)) {
                    to = dataJson.getString(Parm.TO);
                }
                if(dataJson.has(Parm.UUID)){
                    uuid = dataJson.getString(Parm.UUID);
                }
                if (dataJson.has(Parm.DATA_TYPE)) {
                    int type = dataJson.getInt(Parm.DATA_TYPE);
                    Candidate candidate;
                    DeviceModel deviceModel;

                    switch (type) {
                        case Parm.TYPE_LOGIN:
                            break;
                        case Parm.TYPE_LOGOUT:
                            break;
                        case Parm.TYPE_UDP_TEST:
                            candidate = new Candidate();
                            candidate.setTime(System.currentTimeMillis());
                            if (dataJson.has(Parm.FROM)) {
                                candidate.setFrom(dataJson.getString(Parm.FROM));
                            }
                            if (dataJson.has(Parm.REMOTE_IP)) {
                                candidate.setRemoteIp(dataJson.getString(Parm.REMOTE_IP));
                            }
                            if (dataJson.has(Parm.REMOTE_UDP_PORT)) {
                                candidate.setRemotePort(dataJson.getString(Parm.REMOTE_UDP_PORT));
                            }
                            if (dataJson.has(Parm.LOCAL_IP)) {
                                candidate.setLocalIp(dataJson.getString(Parm.LOCAL_IP));
                            }
                            if (dataJson.has(Parm.LOCAL_UDP_PORT)) {
                                candidate.setLocalPort(dataJson.getString(Parm.LOCAL_UDP_PORT));
                            }
                            if (dataJson.has(Parm.SEND_TIME)) {
                                long time = dataJson.getLong(Parm.SEND_TIME);
                                candidate.setDelayTime(System.currentTimeMillis() - time);
                            }
                            candidate.setRelayIp(connector.getAddress().getRemoteIP());
                            candidate.setRelayPort(connector.getAddress().getRemotePort());
                            if (dataJson.has(Parm.CALLBACK)) {
                                String callback = dataJson.getString(Parm.CALLBACK);
                                BaseCallback baseCallBack = CallbackManager.getInstance().get(callback);
                                if (baseCallBack != null) {
                                    baseCallBack.loadObject(candidate);
                                }
                            }
                            ConnectorManager.getInstance().add(candidate);
                                //关闭此处连接会出现严重的问题
//                            connector.disconnect();
                            break;
                        case Parm.TYPE_TRY_PTP:
                            if (dataJson.has(Parm.CANDIDATES)) {
                                JSONArray jsonArray = dataJson.getJSONArray(Parm.CANDIDATES);
                                ArrayList<Candidate> candidates = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    candidate = (Candidate) JsonTools.toJavaBean(Candidate.class,
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
                                    ConnectorManager.tryPTPConnect(candidates,from, uuid);
                                }
                            }
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
                            }

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
                                    FileReceiverSession fileReceiverSession = (FileReceiverSession) session.get(FileReceiverSession.TAG);
                                    if(fileReceiverSession != null ){
                                        if(session.isWaiting()){
                                            session.start();
                                            fileReceiverSession.start((ConnectedByUDP) connector);
                                        }
                                        fileReceiverSession.startDownload();
                                    }
                                }
                            }
                            Log.d(TAG, "已经建立P2P 连接：id =" + from + ", connetor =" + connector.getAddress().getStringRemoteAddress());
                            TelePhone.getInstance().showLog("P2P 连接成功 " + connector.getAddress().getStringRemoteAddress());
                            CacheRepository.getInstance().setP2PConnectSuccess(true);

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

}
