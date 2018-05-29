package com.baige.p2pcore;

import android.util.Log;
import android.util.LruCache;

import com.baige.common.Parm;
import com.baige.connect.BaseConnector;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.connect.msg.MessageManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.Tools;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * 这里保存有P2P连接池
 * 本设备的候选连接路径
 * Created by baige on 2018/5/28.
 */

public class ConnectorManager {

    private final static String TAG = ConnectorManager.class.getSimpleName();
    private LruCache<String, BaseConnector> connectorPools;

    private Map<String, Candidate> candidateMap;

    private Map<String, ConnectSession> sessionMap;

    private static ExecutorService fixedThreadPool = null;

    private ConnectorManager(){
        connectorPools = new LruCache<>(5);
        sessionMap = Collections.synchronizedMap(new LinkedHashMap<String, ConnectSession>());
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池
    }
    private static ConnectorManager INSTANCE;

    public static ConnectorManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ConnectorManager.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new ConnectorManager();
                }
            }
        }
        return INSTANCE;
    }

    public Candidate add(Candidate candidate) {
        if(candidateMap == null){
            candidateMap = new LinkedHashMap<>();
        }
        if (candidate != null) {
            candidateMap.put(candidate.getFrom(), candidate);
        }
        return candidate;
    }

    public BaseConnector add(BaseConnector connector){
        if(connector == null || Tools.isEmpty(connector.getDeviceId())){
            Log.e(TAG, "连接对象无效"+connector);
        }else{
            connectorPools.put(connector.getDeviceId(), connector);
        }
        return connector;
    }

    public ConnectSession add(ConnectSession session){
        if(session == null || Tools.isEmpty(session.getUUID())){
            Log.e(TAG, "会话无效"+session);
        }else{
            sessionMap.put(session.getUUID(), session);
        }
        return session;
    }
    public void submit(Runnable runnable){
        if(runnable != null){
            fixedThreadPool.submit(runnable);
        }
    }
    public Object remove(Object obj){
        if(obj instanceof ConnectSession){
            ConnectSession session = (ConnectSession) obj;
            return sessionMap.remove(session.getUUID());
        }
        if(obj instanceof BaseConnector){
            BaseConnector connector = (BaseConnector) obj;
            return connectorPools.remove(connector.getDeviceId());
        }
        return null;
    }

    public ConnectSession getSession(String uuid){
        return sessionMap.get(uuid);
    }

    public ArrayList<Candidate> getCandidates() {
        if (candidateMap != null && candidateMap.size() > 0) {
            return new ArrayList<>(candidateMap.values());
        }
        return null;
    }

    public static void tryPTPConnect(List<Candidate> candidates, String deviceId, String uuid) {
        DeviceModel deviceModel = NetServerManager.getInstance().getDeviceModelById(deviceId);
//        if (deviceModel != null && deviceModel.getConnectedByUDP() != null && deviceModel.getConnectedByUDP().isConnected(5000)) {
//            Log.d(TAG, "已经建立P2P 连接：id ="+deviceId+", connetor ="+deviceModel.getConnectedByUDP().getAddress().getStringRemoteAddress() );
//            TelePhone.getInstance().showLog("P2P 连接成功 " + deviceModel.getConnectedByUDP().getAddress().getStringRemoteAddress());
//            return;
//        }
        ConnectedByUDP connectedByUDP = null;
        Candidate candidate = null;
        Candidate candidate1;
        Candidate candidate2;
        int port, port1, port2;
        String msg = null;
        CacheRepository cacheRepository = CacheRepository.getInstance();
        JSONObject jsonObject = MessageManager.tryPTPConnect(cacheRepository.getDeviceId(), deviceId, uuid);
        if(jsonObject != null){
            msg = jsonObject.toString();
        }
        //判断自己的NAT类型
//        1, 全锥型(Full Cone)
//
//        2,  受限锥型(Restricted Cone)， 或者说是IP受限锥型
//
//        3,  端口受限锥型(Port Restricted Cone), 或者说是IP + PORT受限锥型
//
//        4,  对称型(Symmetric)
        List<Candidate> candidateList = ConnectorManager.getInstance().getCandidates();
        if (candidateList == null || candidateList.size() == 0) {
            NetServerManager.tryUdpTest();
            return;
        }
        boolean isSymmetric = true;
        boolean isOpSymmetric = true;
        if (candidateList.size() == 1) {
            candidate = candidateList.get(0);
            if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
                isSymmetric = false;
            }

        } else if (candidateList.size() > 1) {
            candidate1 = candidateList.get(0);
            candidate2 = candidateList.get(1);
            if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
                isSymmetric = false;
            }
        }

        //判断对方NAT类型
        if (candidates.size() == 1) {
            candidate = candidates.get(0);
            if (Tools.isEquals(candidate.getLocalPort(), candidate.getRemotePort())) {
                isOpSymmetric = false;
            }
            //TODO 当Symmetric 处理
        } else if (candidates.size() > 1) {
            candidate1 = candidates.get(0);
            candidate2 = candidates.get(1);
            if (Tools.isEquals(candidate1.getRemotePort(), candidate2.getRemotePort())) {
                isOpSymmetric = false;
            }
        }
        if (isSymmetric && isOpSymmetric) {
            //自己和对方都是对称型，不能随便发送数据
            if (candidates.size() == 1 || candidateList.size() == 1) {
                //假对称型，不确定
                if(candidates.size() == 1){
                    candidate = candidates.get(0);
                    port = Integer.valueOf(candidate.getRelayPort());
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
                    connectedByUDP.sendString(msg);

                    if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                }else if(candidates.size() > 1){
                    candidate1 = candidates.get(0);
                    candidate2 = candidates.get(1);
                    port1 = Integer.valueOf(candidate1.getRemotePort());
                    port2 = Integer.valueOf(candidate2.getRemotePort());

                    if (Math.abs(port1 - port2) == 1) {//相差1
                        port = Math.max(port1, port2);
                        // 预测几条
                        for (int i = 0; i < 10; i++) {
                            port++;
                            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                            connectedByUDP.sendString(msg);
                        }

                        if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                            connectedByUDP.sendString(msg);
                        }

                    } else if (Math.abs(port1 - port2) < 10) {
                        port = Math.max(port1, port2);
                        // 预测几条
                        for (int i = 0; i < 10; i++) {
                            port++;
                            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                            connectedByUDP.sendString(msg);
                        }
                        if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                            connectedByUDP.sendString(msg);
                        }
                    } else { //不可能猜测
                        Log.d(TAG, "Symmetric 端口分配相差过大");
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                }
            } else {//真对称型
                candidate1 = candidates.get(0);
                candidate2 = candidates.get(1);
                port1 = Integer.valueOf(candidate1.getRemotePort());
                port2 = Integer.valueOf(candidate2.getRemotePort());

                if (Math.abs(port1 - port2) == 1) {//相差1
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port + 1);
                    connectedByUDP.sendString(msg);

                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }

                } else if (Math.abs(port1 - port2) < 10) {
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                } else { //不可能猜测
                    Log.d(TAG, "Symmetric 端口分配相差过大");
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                    connectedByUDP.sendString(msg);
                }

            }
        } else if (isSymmetric && !isOpSymmetric) {
            //自己对称型，而对方不是，目标唯一
            candidate = candidates.get(0);
            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
            connectedByUDP.sendString(msg);
            if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                connectedByUDP.sendString(msg);
            }
        } else if (!isSymmetric && isOpSymmetric) {
            //自己非对称型，而对方是，自己可以随便发送数据
            if (candidates.size() == 1) {
                candidate = candidates.get(0);
                port = Integer.valueOf(candidate.getRelayPort());
                // 预测几条
                for (int i = 0; i < 10; i++) {
                    port++;
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), port);
                    connectedByUDP.sendString(msg);
                }
                connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
                connectedByUDP.sendString(msg);

                if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                    connectedByUDP.sendString(msg);
                }
            } else if (candidates.size() > 1) {
                candidate1 = candidates.get(0);
                candidate2 = candidates.get(1);
                port1 = Integer.valueOf(candidate1.getRemotePort());
                port2 = Integer.valueOf(candidate2.getRemotePort());

                if (Math.abs(port1 - port2) == 1) {//相差1
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }

                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }

                } else if (Math.abs(port1 - port2) < 10) {
                    port = Math.max(port1, port2);
                    // 预测几条
                    for (int i = 0; i < 10; i++) {
                        port++;
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getRemoteIp(), port);
                        connectedByUDP.sendString(msg);
                    }
                    if (!Tools.isEquals(candidate1.getLocalIp(), candidate1.getRemoteIp())) {
                        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                        connectedByUDP.sendString(msg);
                    }
                } else { //不可能猜测
                    Log.d(TAG, "Symmetric 端口分配相差过大");
                    connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate1.getLocalIp(), candidate1.getLocalPort());
                    connectedByUDP.sendString(msg);
                }
            }

        } else {
            candidate = candidates.get(0);
            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getRemoteIp(), candidate.getRemotePort());
            connectedByUDP.sendString(msg);
            if (!Tools.isEquals(candidate.getLocalIp(), candidate.getRemoteIp())) {
                connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(candidate.getLocalIp(), candidate.getLocalPort());
                connectedByUDP.sendString(msg);
            }
        }
    }


}
