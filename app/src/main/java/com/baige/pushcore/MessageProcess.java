package com.baige.pushcore;

import android.content.Context;
import android.util.Log;

import com.baige.callback.CallbackManager;
import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.connect.SocketPacket;
import com.baige.connect.msg.MessageManager;

import com.baige.connect.msg.ResponseMessage;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.telephone.PhoneActivity;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MessageProcess {

    private final static String TAG = MessageProcess.class.getCanonicalName();


    /**
     * IPush 或JPush 收到数据（TCP连接）
     * @param context
     * @param json
     */
    public static void receive(Context context, JSONObject json) {
        if (context == null || json == null) {
            return;
        }
        Log.v(TAG, "TCP收到 :" + json);
        if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
            response(context, json); //回复信息
        } else {
            String from = null;
            String to = null;
            String name = "";
            DeviceModel deviceModel;



        }

    }

    /**IPush 或JPush 收到数据（TCP连接）
     * @param context
     * @param json
     */
    public static void response(Context context, JSONObject json) {
        if (context == null || json == null) {
            return;
        }
        try {
            String from = null;
            String to = null;

            if (json.has(Parm.FROM)) {
                from = json.getString(Parm.FROM);
            }
            if (json.has(Parm.TO)) {
                to = json.getString(Parm.TO);
            }

            int code = json.getInt(Parm.CODE);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**一般是UDP收到数据
     * @param connector
     * @param packet
     */
    //UDP数据 解析头部和内容
    public static void receive(BaseConnector connector, SocketPacket packet) {

        if (connector == null || packet == null) {
            return;
        }
        if (packet.isHeartBeat() || packet.isDisconnected()) {
            return;
        }

        String to = null;
        String from = null;
        if (packet.getHeaderBuf() != null) {
            String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
            if (!Tools.isEmpty(msg)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    if (json.has(Parm.DATA_TYPE)) {
                        int type = json.getInt(Parm.DATA_TYPE);
                        if (json.has(Parm.FROM)) {
                            from = json.getString(Parm.FROM);
                        }
                        if (json.has(Parm.TO)) {
                            to = json.getString(Parm.TO);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        } else if (packet.getContentBuf() != null) {
            String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
            if (!Tools.isEmpty(msg)) {
                try {
                    JSONObject json = new JSONObject(msg);
                    receive(connector, json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }


    /**一般是UDP收到数据
     * @param connector
     * @param json
     */
    //UDP数据
    public static void receive(BaseConnector connector, JSONObject json) {
        if (connector == null || json == null) {
            return;
        }
        String to = null;
        String from = null;
        String name = "";
        DeviceModel deviceModel = null;
        ResponseMessage responseMessage = null;

        if (json.has(Parm.CODE) && json.has(Parm.DATA)) {
            response(connector, json); //回复信息
        } else {
            try {
                if (json.has(Parm.FROM)) {
                    from = json.getString(Parm.FROM);
                }
                if (json.has(Parm.TO)) {
                    to = json.getString(Parm.TO);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    /** 一般是UDP收到数据
     * @param connector
     * @param json
     */
    // 收到回复
    public static void response(BaseConnector connector, JSONObject json) {
        if (connector == null || json == null) {
            return;
        }

        try {
            JSONObject dataJson = json.getJSONObject(Parm.DATA);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
