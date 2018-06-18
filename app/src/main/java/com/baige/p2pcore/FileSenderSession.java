package com.baige.p2pcore;

import android.util.Log;

import com.baige.BaseApplication;
import com.baige.common.Parm;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.SocketPacket;
import com.baige.connect.msg.MessageManagerOfFile;
import com.baige.data.source.cache.CacheRepository;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by baige on 2018/5/29.
 */

public class FileSenderSession {
    public final static String TAG = FileSenderSession.class.getSimpleName();
    String uuid;
    String fileName;
    String filePath;
    long fileFullSize;
    CacheFile cacheFile;

    SlipWindow.SendWindow sendWindow;

    ConnectedByUDP connectedByUDP;

    String from;
    String to;

    int packetSize = 2048; //默认包大小
    long currentIndex = 0;

    int slipWindowCount = 10;

    /**
     * @param uuid
     * @param fileName
     * @param filePath 注意这里仅有文件的路径，没有文件名
     */
    public FileSenderSession(String uuid, String fileName, String filePath) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    public void start(ConnectedByUDP connectedByUDP) {
        this.connectedByUDP = connectedByUDP;
        from = CacheRepository.getInstance().getDeviceId();
        to = connectedByUDP.getDeviceId();
        cacheFile = new CacheFile(this.filePath, fileName, true);
        fileFullSize = cacheFile.getFileSize();
        cacheFile.setFullsize(fileFullSize);
        sendWindow = new SlipWindow().setUUID(uuid).setWindowCount(slipWindowCount).buildSendWindow(mPacketReader);
        sendWindow.setMaxPacketNum((fileFullSize + (packetSize - 1)) / packetSize );
    }

    public void startSend() {
        boolean res = sendWindow.startSend();
        Log.d(TAG, "开始发送文件：" + res);
    }

    public void affirmReceive(String uuid, long num) {
        sendWindow.affirmReceive(uuid, num);
    }

    public void askPacket(String uuid, long num) {
        boolean res = sendWindow.askPacket(uuid, num);
        Log.d(TAG, "请求数据包"+num+ ", 发送 res ="+res);
    }

    public void finish(){
        cacheFile.close();
    }

    private PacketReader mPacketReader = new PacketReader() {
        @Override
        public SocketPacket read(String uuid, long number) {
            SocketPacket socketPacket = new SocketPacket();
            byte[] buf = cacheFile.read(currentIndex, packetSize);
            JSONObject jsonObject = MessageManagerOfFile.headerPacket(from, to, uuid, number, currentIndex);
            socketPacket.setHeaderBuf(jsonObject.toString().getBytes());
            if (buf != null && buf.length > 0) {
                currentIndex += buf.length;
                socketPacket.setContentBuf(buf);
                return socketPacket;
            }
            return null;
        }

        @Override
        public void sendSocketPacket(SocketPacket socketPacket) {
            connectedByUDP.sendPacket(socketPacket);
        }
    };

    private long remain() { //返回文件剩余长度
        return fileFullSize - currentIndex;
    }

    public int getSlipWindowCount() {
        return slipWindowCount;
    }

    public void setSlipWindowCount(int slipWindowCount) {
        this.slipWindowCount = slipWindowCount;
    }
}
