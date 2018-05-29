package com.baige.connect.msg;

import android.util.Log;

import com.baige.common.Parm;
import com.baige.connect.SocketPacket;
import com.baige.data.entity.Candidate;
import com.baige.data.source.cache.CacheRepository;
import com.baige.p2pcore.ConnectorManager;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

//            Base64 编码
//            无论是编码还是解码都会有一个参数Flags，Android提供了以下几种
//
//            DEFAULT 这个参数是默认，使用默认的方法来加密
//
//            NO_PADDING 这个参数是略去加密字符串最后的”=”
//
//            NO_WRAP 这个参数意思是略去所有的换行符（设置后CRLF就没用了）
//
//            CRLF 这个参数看起来比较眼熟，它就是Win风格的换行符，意思就是使用CR LF这一对作为一行的结尾而不是Unix风格的LF
//
//            URL_SAFE 这个参数意思是加密时不使用对URL和文件名有特殊意义的字符来作为加密字符，具体就是以-和_取代+和/

/**
 * Created by baige on 2017/10/24.
 */

public class MessageManager {

    private final static String TAG = MessageManager.class.getName();

    public final static int VERSION = 2;

    public static String login(String deviceId, String localIp, String localPort, String acceptPort, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGIN);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_PORT, localPort);
//            jsonObject.put(Parm.ACCEPT_PORT, acceptPort);
//            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loginSuccess(String serverId, String deviceId, String remoteIp, String remotePort, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGIN);
            jsonObject.put(Parm.FROM, serverId);
            jsonObject.put(Parm.DEVICE_ID, deviceId);
            jsonObject.put(Parm.REMOTE_IP, remoteIp);
            jsonObject.put(Parm.REMOTE_PORT, remotePort);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String udpTest(String deviceId, String callbackId, String localIp, String localUdpPort){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.CALLBACK, callbackId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String udpTest(String deviceId, String localIp, String localUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, deviceId);
            jsonObject.put(Parm.LOCAL_IP, localIp);
            jsonObject.put(Parm.LOCAL_UDP_PORT, localUdpPort);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject udpTestResponse(String serverId, String deviceId, String remoteIp, String remoteUdpPort) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_UDP_TEST);
            jsonObject.put(Parm.FROM, serverId);
            jsonObject.put(Parm.DEVICE_ID, deviceId);
            jsonObject.put(Parm.REMOTE_IP, remoteIp);
            jsonObject.put(Parm.REMOTE_UDP_PORT, remoteUdpPort);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String logout(String deviceId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_LOGOUT);
            jsonObject.put(Parm.FROM, deviceId);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 注意需要告诉对方自己使用的通话端口
     */
    public static String callTo(String talkWith, String name, int uid) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.USERNAME, name);
            jsonObject.put(Parm.UID, uid);
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_CALL_TO);
            ArrayList<Candidate> candidates = ConnectorManager.getInstance().getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            Log.d(TAG, "call To MSG:"+jsonObject.toString());
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String replyCallTo(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_REPLY_CALL_TO);
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
    public static JSONObject sendCandidateTo(String to){
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.CODE, Parm.CODE_SUCCESS);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_TRY_PTP);
            ArrayList<Candidate> candidates = ConnectorManager.getInstance().getCandidates();
            if (candidates != null && candidates.size() > 0) {
                JSONArray jsonArray = new JSONArray();
                for (Candidate candidate : candidates){
                    jsonArray.put(JsonTools.getJSON(candidate));
                }
                jsonObject.put(Parm.CANDIDATES, jsonArray);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param from
     * @param to
     * @param uuid 根据UUID 获取p2p连接成功后的会话，执行会话开始任务
     * @return
     */
    public static JSONObject tryPTPConnect(String from, String to, String uuid){
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_TRY_PTP_CONNECT);
            if(!Tools.isEmpty(uuid)){
                jsonObject.put(Parm.UUID, uuid);
            }
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 注意需要告诉对方自己使用的通话端口
     */
    public static String onPickUp(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_PICK_UP);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String onHangUp(String talkWith) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, cacheRepository.getDeviceId());
            jsonObject.put(Parm.TO, talkWith);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_HANG_UP);
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static SocketPacket voice(String from, String to, byte[] voice, int delayTime) {
        if(voice == null || voice.length == 0){
            return  null;
        }
        SocketPacket socketPacket = new SocketPacket();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Parm.FROM, from);
            jsonObject.put(Parm.TO, to);
            jsonObject.put(Parm.SEND_TIME, System.currentTimeMillis());
            jsonObject.put(Parm.DELAY_TIME, delayTime);
            jsonObject.put(Parm.DATA_TYPE, Parm.TYPE_VOICE);
            socketPacket.setHeaderBuf(jsonObject.toString().getBytes());
            socketPacket.setContentBuf(voice);
            return socketPacket;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    public static String getAllServers() {
//        String msg;
//        CacheRepository cacheRepository = CacheRepository.getInstance();
//        MessageHeader header = new MessageHeader();
//        header.setVersion(VERSION);
//        header.setFrom(cacheRepository.getDeviceId());
//        header.setMethod(MessageHeader.Method.GET);
//        header.setParam(MessageHeader.Param.SERVERS);
//        msg = MessageParser.getJSON(header);
//        return msg;
        return null;
    }


}
