package com.baige.connect;


import android.util.Log;

import com.baige.connect.msg.MessageManager;
import com.baige.connect.msg.MessageProcess;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.source.cache.CacheRepository;
import com.baige.pushcore.MessagePushProcess;
import com.baige.util.IPUtil;
import com.baige.util.Loggerx;
import com.baige.util.Tools;

import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class NetServerManager {
    private final static String TAG = NetServerManager.class.getCanonicalName();

    private final NetServerManager self;

    private static NetServerManager INSTANCE = null;

    private DatagramSocketServer datagramSocketServer;

    private Map<String, DeviceModel> devicesMap;

    private NetServerManager() {

        self = this;

        devicesMap = Collections.synchronizedMap(new LinkedHashMap<String, DeviceModel>());

        datagramSocketServer = new DatagramSocketServer();
        datagramSocketServer.registerServerListener(mOnDatagramSocketServerListener);

        datagramSocketServer.start();
    }

    public static NetServerManager getInstance() {
        if (INSTANCE == null) {
            synchronized (NetServerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetServerManager();
                }
            }
        }
        return INSTANCE;
    }

    public int getUdpPort() {
        if (datagramSocketServer != null && datagramSocketServer.isStart()) {
            return datagramSocketServer.getLocalPort();
        }
        return 0;
    }


    public List<DeviceModel> getAllDevices() {
        if (devicesMap != null) {
            return new ArrayList<>(devicesMap.values());
        }
        return null;
    }


    private OnDatagramSocketServerListener mOnDatagramSocketServerListener = new OnDatagramSocketServerListener() {

        @Override
        public void onServerStart(DatagramSocketServer server) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "UDP 监听端口：" + server.getLocalPort());
        }

        @Override
        public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
            // TODO Auto-generated method stub
            if(!packet.isHeartBeat() && !packet.isDisconnected()){
                if(packet.getHeaderBuf() == null){
                    Log.v(TAG, "UDP 接收到数据:" + packet);
                }else{
//                    Log.v("voice", "size ="+packet.getContentBuf().length+"connect ="+connector.getAddress().getStringRemoteAddress());
                    Log.v("header", packet.toString());
                }
                MessageProcess.receive(connector, packet);
            }

        }

        @Override
        public void onServerClose(DatagramSocketServer server) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "UDP 停止监听端口：" + server.getLocalPort());
        }

        @Override
        public void onClientDisconnected(ConnectedByUDP connector) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onClientConnected(ConnectedByUDP connector) {
            // TODO Auto-generated method stub

        }
    };


    //登录验证之后的用户使用的监听器
    private OnConnectedListener mClientConnectedListener = new OnConnectedListener() {

        @Override
        public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
            // TODO Auto-generated method stub
            Loggerx.d(TAG, "TCP接收到消息" + responsePacket.toString());
        }

        @Override
        public void onDisconnected(BaseConnector connector) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnected(BaseConnector connector) {
            // TODO Auto-generated method stub

        }
    };

    public void connectToAddressByTCP(SocketClientAddress address, OnConnectedListener callback) {
        ConnectedByTCP connectedByTCP = new ConnectedByTCP(address);
        connectedByTCP.registerConnectedListener(callback);
        connectedByTCP.registerConnectedListener(mClientConnectedListener); //双方都需要发送自己的UUID等验证信息
        connectedByTCP.connect();
    }

    public void connectToAddressByUDP(SocketClientAddress address, OnDatagramSocketServerListener callack) {
        ConnectedByUDP connectedByUDP = new ConnectedByUDP(address);
        connectedByUDP.setRunningSocket(datagramSocketServer);
        datagramSocketServer.registerServerListener(callack);
        connectedByUDP.connect();
    }

    public DeviceModel getDeviceModelById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            return devicesMap.get(deviceid);
        }
    }

    public ConnectedByTCP getTCPConnectorById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if (deviceModel != null) {
                return deviceModel.getConnectedByTCP();
            }
        }
        return null;
    }

    public ConnectedByUDP getUDPConnectorById(String deviceid) {
        if (Tools.isEmpty(deviceid)) {
            return null;
        } else {
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if (deviceModel != null) {
                return deviceModel.getConnectedByUDP();
            }
        }
        return null;
    }
    public void put(String from, DeviceModel deviceModel) {
        // TODO Auto-generated method stub
        devicesMap.put(from, deviceModel);
    }
    public DeviceModel remove(String deviceId) {
        // TODO Auto-generated method stub
        if (devicesMap != null) {
            return devicesMap.remove(deviceId);
        }
        return null;
    }

    public ConnectedByUDP getUDPConnectorByAddress(String ip, int port) {
        ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
        if (connectedByUDP == null) {
            connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
        }
        connectedByUDP.setRunningSocket(datagramSocketServer);
        return connectedByUDP;
    }

    public ConnectedByUDP getUDPConnectorByAddress(String ip, String port) {
        ConnectedByUDP connectedByUDP = datagramSocketServer.get(ip + ":" + port);
        if (connectedByUDP == null) {
            connectedByUDP = new ConnectedByUDP(new SocketClientAddress(ip, port));
        }
        connectedByUDP.setRunningSocket(datagramSocketServer);
        return connectedByUDP;
    }

    public boolean sendMessage(String ip, int port, byte[] buf) {
        if (datagramSocketServer != null && datagramSocketServer.isStart()) {
            InetAddress address = null;
            try {
                address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                datagramSocketServer.getRunningServerSocket().send(packet);
                return true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return false;
    }


    public static void tryUdpTest() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        String localIp = IPUtil.getLocalIPAddress(true);
        String localPort = NetServerManager.getInstance().getUdpPort()+"";
        String msg = MessageManager.udpTest(cacheRepository.getDeviceId(), localIp, localPort);
        ConnectedByUDP connectedByUDP;
        if (!cacheRepository.getServerIp().equals("120.78.148.180") && !cacheRepository.getServerIp().equals("39.180.74.14")) {
            connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort());
            connectedByUDP.sendString(msg);
        }
        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress("120.78.148.180", 12059);
        connectedByUDP.sendString(msg);

        connectedByUDP = NetServerManager.getInstance().getUDPConnectorByAddress("39.180.74.14", 12059);
        connectedByUDP.sendString(msg);
    }

    public void tryUdpTest(String msg) {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        ConnectedByUDP connectedByUDP;
        if (!cacheRepository.getServerIp().equals("120.78.148.180") && !cacheRepository.getServerIp().equals("39.180.74.14")) {
            connectedByUDP = getUDPConnectorByAddress(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort());
            connectedByUDP.sendString(msg);
        }
        connectedByUDP = getUDPConnectorByAddress("120.78.148.180", 12059);
        connectedByUDP.sendString(msg);

        connectedByUDP = getUDPConnectorByAddress("39.180.74.14", 12059);
        connectedByUDP.sendString(msg);
    }


}
