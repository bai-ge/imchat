package com.baige.connect.msg;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvInputService;
import android.util.Log;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.NetServerManager;
import com.baige.connect.SocketPacket;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.p2pcore.ConnectSession;
import com.baige.p2pcore.ConnectorManager;
import com.baige.p2pcore.FileReceiverSession;
import com.baige.pushcore.SendMessageBroadcast;
import com.baige.telephone.PhoneActivity;
import com.baige.telephone.TelePhone;
import com.baige.util.JsonTools;
import com.baige.util.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
* 消息分为几种类型
* 1.自己的
*   1)别人发给自己的消息
*   2)别人回复自己的消息
* 2.转发的
*
*
* */

public class MessageProcess {

    private final static String TAG = MessageProcess.class.getSimpleName();
    /**
     * 处理转发
     *
     * @param connector
     * @param packet
     */
    //不转发的类型
    public static int[] filterNotTranspond = new int[]{Parm.TYPE_UDP_TEST, Parm.TYPE_TRY_PTP_CONNECT};

    public static void receive(BaseConnector connector, SocketPacket packet) {
        if (connector == null || packet == null) {
            return;
        }
        if (packet.isHeartBeat() || packet.isDisconnected()) {
            return;
        }
        Log.i(TAG, packet.toString() + ", size = " + packet.size());
        String from = null;
        String to = null;
        String uuid = null;
        long number = 0;
        long currentIndex;
        if (packet.getHeaderBuf() != null) { // 语音信息或文件信息
            try {
                String msg = Tools.dataToString(packet.getHeaderBuf(), Tools.DEFAULT_ENCODE);
                if (!Tools.isEmpty(msg)) {
                    JSONObject json = new JSONObject(msg);

                    if (json.has(Parm.FROM)) {
                        from = json.getString(Parm.FROM);
                    }
                    if (json.has(Parm.TO)) {
                        to = json.getString(Parm.TO);
                    }
                    if (Tools.isEmpty(to) || CacheRepository.getInstance().getDeviceId().equals(to)) {
                        // 自己收到自己的消息
                        if (json.has(Parm.DATA_TYPE)) {
                            int type = json.getInt(Parm.DATA_TYPE);
                            switch (type) {
                                case Parm.TYPE_VOICE:
                                    long sendTime = 0;
                                    int delayTime;
                                    long diffTime = 0;
                                    if (json.has(Parm.SEND_TIME)) {
                                        sendTime = json.getLong(Parm.SEND_TIME);
                                        diffTime = System.currentTimeMillis() - sendTime;
                                        TelePhone.getInstance().setDiffTime(diffTime);
                                    }
                                    if (json.has(Parm.DELAY_TIME)) {
                                        delayTime = json.getInt(Parm.DELAY_TIME);
                                        if (sendTime > 0 && delayTime != 0) {
                                            TelePhone.getInstance().setDelayTime((long) ((diffTime + delayTime) * 1.0 / 2));
                                        }
                                    }
                                    if (packet.getContentBuf() != null && packet.getContentBuf().length > 0) {
                                        TelePhone.getInstance().play(packet.getContentBuf());
                                    }
                                    break;
                                case Parm.TYPE_FILE:
                                    if (json.has(Parm.NUMBER)) {
                                        number = json.getLong(Parm.NUMBER);
                                    }
                                    if (json.has(Parm.CURRENT)) {
                                        currentIndex = json.getLong(Parm.CURRENT);
                                        Log.d(TAG, "收到数据包" + number + ", current =" + currentIndex);
                                    }
                                    if (json.has(Parm.UUID)) {
                                        uuid = json.getString(Parm.UUID);
                                        if (!Tools.isEmpty(uuid)) {
                                            ConnectSession session = ConnectorManager.getInstance().getSession(uuid);
                                            if (session != null) {
                                                FileReceiverSession fileReceiverSession = (FileReceiverSession) session.get(FileReceiverSession.TAG);
                                                if (fileReceiverSession != null) {
                                                    fileReceiverSession.receivePacket(packet, uuid, number);
                                                } else {
                                                    Log.d(TAG, "找到会话，但没有找到接收文件会话");
                                                }
                                            } else {
                                                Log.d(TAG, "收到文件数据，但无法找到会话");
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    Log.d(TAG, "未处理UDP 数据" + json);
                                    break;
                            }
                        }
                        return;
                    } else {
                        MessageTranspond.transpond(connector, packet, json, to);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (packet.getContentBuf() != null) {
            try {
                String msg = Tools.dataToString(packet.getContentBuf(), Tools.DEFAULT_ENCODE);
                if (!Tools.isEmpty(msg)) {
                    JSONObject json = new JSONObject(msg);
                    if (json.has(Parm.FROM)) {
                        from = json.getString(Parm.FROM);
                    }
                    if (json.has(Parm.TO)) {
                        to = json.getString(Parm.TO);
                    }
                    if (Tools.isEmpty(to) || CacheRepository.getInstance().getDeviceId().equals(to)) {
                        // 收到属于自己的消息
                        MessageReceive.receive(connector, json, from, to);
                        return;
                    } else {
                        //过滤不转发的类型
                        if (json.has(Parm.DATA_TYPE)) {
                            int type = json.getInt(Parm.DATA_TYPE);
                            for (int i = 0; i < filterNotTranspond.length; i++) {
                                if (type == filterNotTranspond[i]) {
                                    return;
                                }
                            }

                        }
                        MessageTranspond.transpond(connector, packet, json, to);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
