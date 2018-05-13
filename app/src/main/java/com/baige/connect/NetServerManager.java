package com.baige.connect;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.baige.connect.msg.MessageManager;
import com.baige.connect.msg.Parm;
import com.baige.data.entity.DeviceModel;
import com.baige.service.DaemonServiceRepository;
import com.baige.util.IPUtil;
import com.baige.util.Loggerx;
import com.baige.util.Tools;

import org.json.JSONException;
import org.json.JSONObject;


public class NetServerManager {
	private final static String TAG = NetServerManager.class.getCanonicalName();

	private final NetServerManager self;

	private static NetServerManager INSTANCE = null;

	private DatagramSocketServer datagramSocketServer;

	private SocketServer socketServer;
	
	private Map<String, DeviceModel> devicesMap;

    private DeviceModel mServerDevice;
    private final Object serverConnectLock = new Object();

    private DaemonServiceRepository mDaemonServiceRepository;

	private NetServerManager() {

		self = this;
        mDaemonServiceRepository = DaemonServiceRepository.getInstance();

		devicesMap = Collections.synchronizedMap(new LinkedHashMap<String, DeviceModel>());
	
		socketServer = new SocketServer();
		socketServer.registerSocketServerListener(mOnSocketServerListener);
		
		datagramSocketServer = new DatagramSocketServer();
		datagramSocketServer.registerServerListener(mOnDatagramSocketServerListener);
		
		socketServer.beginListen();
		if(!socketServer.isListening()){
			Loggerx.d(TAG, "TCP 监听端口失败："+socketServer.getPort());
		}
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

	public synchronized DeviceModel  connectTOServer(String ip, int port){
        ConnectedByTCP connectedByTCP;
        Loggerx.d(TAG, "开始连接服务器"+ip+":"+port);
        synchronized (serverConnectLock) {
            if (mServerDevice == null) {//开始连接
                mServerDevice = new DeviceModel();
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP() == null) {
                connectedByTCP = new ConnectedByTCP(new SocketClientAddress(ip, port));
                mServerDevice.setConnectedByTCP(connectedByTCP);
                connectedByTCP.registerConnectedListener(mOnConnectedListener);
                connectedByTCP.connect();
            } else if (mServerDevice.getConnectedByTCP().getAddress() == null) {
                mServerDevice.getConnectedByTCP().setAddress(new SocketClientAddress(ip, port));
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().connect();
            } else if (!mServerDevice.getConnectedByTCP().isConnected()) {
                mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                mServerDevice.getConnectedByTCP().connect();
            } else {
                if (mServerDevice.getConnectedByTCP().getAddress().getRemotePort().equals(ip)
                        && mServerDevice.getConnectedByTCP().getAddress().getRemotePortIntegerValue() == port) {
                    return mServerDevice;
                } else {
                    mServerDevice.getConnectedByTCP().disconnect();
                    mServerDevice.getConnectedByTCP().getAddress().setRemoteIP(ip);
                    mServerDevice.getConnectedByTCP().getAddress().setRemotePortWithInteger(port);
                    mServerDevice.getConnectedByTCP().registerConnectedListener(mOnConnectedListener);
                    mServerDevice.getConnectedByTCP().connect();
                }
            }
        }
        return mServerDevice;
    }
	
	public List<DeviceModel> getAllDevices() {
		if (devicesMap != null) {
			return new ArrayList<>(devicesMap.values());
		}
		return null;
	}
	
	private OnSocketServerListener mOnSocketServerListener = new OnSocketServerListener(){

		@Override
		public void onServerBeginListen(SocketServer socketServer, int port) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "TCP 监听端口："+port);
		}

		@Override
		public void onServerStopListen(SocketServer socketServer, int port) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "TCP 停止监听端口："+port);
		}

		@Override
		public void onClientConnected(SocketServer socketServer, com.baige.connect.ConnectedByTCP socketServerClient) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "游客连接成功: "+socketServerClient);
			socketServerClient.registerConnectedListener(mBeOnConnectedListener);
		}

		@Override
		public void onClientDisconnected(SocketServer socketServer,
				com.baige.connect.ConnectedByTCP socketServerClient) {
			// TODO Auto-generated method stub
		}
		
	};
	
	private OnDatagramSocketServerListener mOnDatagramSocketServerListener = new OnDatagramSocketServerListener() {
		
		@Override
		public void onServerStart(DatagramSocketServer server) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "UDP 监听端口："+server.getLocalPort());
		}
		
		@Override
		public void onServerReceivePacket(ConnectedByUDP connector, SocketPacket packet) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServerClose(DatagramSocketServer server) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "UDP 停止监听端口："+server.getLocalPort());
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
	
	//被动连接的用户登录验证使用的监听器
	private OnConnectedListener mBeOnConnectedListener = new OnConnectedListener.SimpleOnConnectedListener() {
		
		@Override
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			// TODO Auto-generated method stub
			//if(验证成功) {获取uuid, 添加进设备中}
            if (responsePacket != null && !(responsePacket.isHeartBeat() || responsePacket.isDisconnected())
                    && responsePacket.getContentBuf() != null && responsePacket.getContentBuf().length > 0) {
                String text = Tools.dataToString(responsePacket.getContentBuf(), Tools.DEFAULT_ENCODE);
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if(jsonObject.has(Parm.DATA_TYPE)){
                        int type = jsonObject.getInt(Parm.DATA_TYPE);
                        if(type == Parm.TYPE_LOGIN && jsonObject.has(Parm.FROM)){
                            String from = jsonObject.getString(Parm.FROM);
                            DeviceModel deviceModel = devicesMap.get(from);
                            if(deviceModel == null){
                                deviceModel = new DeviceModel();
                                deviceModel.setDeviceId(from);
                                if(connector instanceof ConnectedByTCP){
                                    deviceModel.setConnectedByTCP((ConnectedByTCP) connector);
                                }else if(connector instanceof ConnectedByUDP){
                                    deviceModel.setConnectedByUDP((ConnectedByUDP) connector);
                                }
                                devicesMap.put(from, deviceModel);
                            }
                            if(jsonObject.has(Parm.LOCAL_IP)){
                                deviceModel.setLocalIp(jsonObject.getString(Parm.LOCAL_IP));
                            }
                            if(jsonObject.has(Parm.LOCAL_PORT)){
                                deviceModel.setLocalPort(jsonObject.getInt(Parm.LOCAL_PORT));
                            }
                            if(jsonObject.has(Parm.ACCEPT_PORT)){
                                deviceModel.setAcceptPort(jsonObject.getInt(Parm.ACCEPT_PORT));
                            }
                            if(jsonObject.has(Parm.LOCAL_UDP_PORT)){
                                deviceModel.setLocalUdpPort(jsonObject.getInt(Parm.LOCAL_UDP_PORT));
                            }

                            deviceModel.setRemoteIp(connector.getAddress().getRemoteIP());
                            deviceModel.setRemotePort(connector.getAddress().getRemotePortIntegerValue());
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
			connector.unRegisterConnectedListener(mBeOnConnectedListener);
			connector.registerConnectedListener(mClientConnectedListener);
            Loggerx.d(TAG,  "TCP接收到消息"+responsePacket.toString());
		}
	};

    //主动连接登录验证使用的监听器
    private OnConnectedListener mOnConnectedListener = new OnConnectedListener.SimpleOnConnectedListener() {

        @Override
        public void onConnected(BaseConnector connector) {
            super.onConnected(connector);
            Log.v(TAG, "连接成功"+connector);
            String msg = MessageManager.login(mDaemonServiceRepository.getDeviceId(), IPUtil.getLocalIPAddress(true), ""+socketServer.getPort(), ""+socketServer.getPort(), ""+datagramSocketServer.getLocalPort());
            if(msg != null){
                connector.sendString(msg);
                Log.v(TAG, "发送数据："+msg);
            }else{
                throw new IllegalArgumentException("the login msg is null");
            }
        }

        @Override
        public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
            // TODO Auto-generated method stub

            //if(验证成功) {获取uuid, 添加进设备中
            Log.v(TAG, "接收到数据"+connector);

            connector.registerConnectedListener(mClientConnectedListener);
            Loggerx.d(TAG,  "TCP接收到消息"+responsePacket.toString());
        }

        @Override
        public void onDisconnected(final BaseConnector connector) {
            super.onDisconnected(connector);
            //TODO 启动定时连接
            Log.v(TAG, "连接关闭"+connector);
            DaemonServiceRepository daemonServiceRepository = DaemonServiceRepository.getInstance();
            if(connector.getTryConnectCount() <= 20 && (daemonServiceRepository.isNetworkValid() || daemonServiceRepository.isWifiValid())){
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            sleep(10 * 1000);
                            connector.connect();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    };
	
	
	//登录验证之后的用户使用的监听器
	private OnConnectedListener mClientConnectedListener = new OnConnectedListener() {
		
		@Override
		public void onResponse(BaseConnector connector, SocketPacket responsePacket) {
			// TODO Auto-generated method stub
            Loggerx.d(TAG,  "TCP接收到消息"+responsePacket.toString());
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

	public void connectToAddressByTCP(SocketClientAddress address, OnConnectedListener callback){
        ConnectedByTCP connectedByTCP = new ConnectedByTCP(address);
        connectedByTCP.registerConnectedListener(callback);
        connectedByTCP.registerConnectedListener(mBeOnConnectedListener); //双方都需要发送自己的UUID等验证信息
        connectedByTCP.connect();
    }

    public void connectToAddressByUDP(SocketClientAddress address, OnDatagramSocketServerListener callack){
        ConnectedByUDP connectedByUDP = new ConnectedByUDP(address);
        connectedByUDP.setRunningSocket(datagramSocketServer);
        datagramSocketServer.registerServerListener(callack);
        connectedByUDP.connect();
    }

    public DeviceModel getDeviceModelById(String deviceid){
       if(Tools.isEmpty(deviceid)){
           return null;
       }else{
           return devicesMap.get(deviceid);
       }
    }

    public ConnectedByTCP getTCPConnectorById(String deviceid){
        if(Tools.isEmpty(deviceid)){
            return null;
        }else{
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if(deviceModel != null){
                return deviceModel.getConnectedByTCP();
            }
        }
        return null;
    }

    public ConnectedByUDP getUDPConnectorById(String deviceid){
        if(Tools.isEmpty(deviceid)){
            return null;
        }else{
            DeviceModel deviceModel = devicesMap.get(deviceid);
            if(deviceModel != null){
                return deviceModel.getConnectedByUDP();
            }
        }
        return null;
    }



}
