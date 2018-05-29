package com.baige.p2pcore;

import android.util.Log;
import android.widget.Toast;

import com.baige.BaseApplication;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.SocketPacket;
import com.baige.connect.msg.MessageManagerOfFile;
import com.baige.connect.msg.MessageResponse;
import com.baige.data.entity.FileView;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.FileUtils;
import com.baige.util.Tools;

import org.json.JSONObject;

/**
 * Created by baige on 2018/5/29.
 */

public class FileReceiverSession {
    public final static String TAG = FileReceiverSession.class.getSimpleName();
    String uuid;
    String fileName;
    long fileFullSize;
    CacheFile cacheFile;
    SlipWindow.ReceiveWindow receiveWindow;
    ConnectedByUDP connectedByUDP;

    String from;
    String to;
    long startTime;

    String remark;

    boolean isReceiveData = false;


    public FileReceiverSession(String uuid, String remark, String fileName, long fileFullSize) {
        this.uuid = uuid;
        this.fileName = fileName;
        this.fileFullSize = fileFullSize;
        this.remark = remark;
    }

    public void start(ConnectedByUDP connectedByUDP){
        this.connectedByUDP = connectedByUDP;
        from = CacheRepository.getInstance().getDeviceId();
        to = connectedByUDP.getDeviceId();
        cacheFile = new CacheFile(BaseApplication.downloadPath, fileName, false);
        cacheFile.setFullsize(fileFullSize);
        receiveWindow = new SlipWindow().setUUID(uuid).setWindowCount(10).buildReceiveWindow(mPacketWriter);
    }

    public void startDownload(){
        if(!isReceiveData){
            JSONObject jsonObject = MessageManagerOfFile.startDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
        }
    }

    public void receivePacket(SocketPacket packet, String uuid, long number){
        if(!isReceiveData){
            isReceiveData = true;
            startTime = System.currentTimeMillis();
        }
        receiveWindow.receivePacket(packet, uuid, number);
    }

    private PacketWriter mPacketWriter = new PacketWriter() {
        @Override
        public boolean write(SocketPacket packet) {
            Log.d(TAG, "写入文件"+fileName);
            boolean res = false;
            if(packet.getContentBuf() != null && packet.getContentBuf().length > 0){
                res = cacheFile.write(packet.getContentBuf(), packet.getContentBuf().length);
                Log.d(TAG, "写入文件"+fileName + ", 结果："+res +", 大小："+packet.getContentBuf().length);
                long time = System.currentTimeMillis() - startTime;
                long speed = (long) (cacheFile.getFileSize() * 1.0 / time); // Byte/ms
                speed = speed * 1000; // Byte/s
                Log.i(TAG+"Speed", Tools.getSizeSting(speed) + "/s");
                FileView fileView = CacheRepository.getInstance().getFileViewObservable().get(remark);
                if(fileView != null){
                    fileView.setShowProgress(true);
                    fileView.setProgressPercent((float) (cacheFile.getFileSize() * 1.0 / fileFullSize));
                    CacheRepository.getInstance().getFileViewObservable().put(fileView);
                }
            }
            if(!res){
               error();
            }
            return res && cacheFile.isFinish();
        }

        @Override
        public void responeAffirm(String uuid, long number) {
            JSONObject jsonObject = MessageManagerOfFile.responeAffirm(from, to, uuid, number);
            connectedByUDP.sendString(jsonObject.toString());
        }

        @Override
        public void askPacket(String uuid, long number) {
            JSONObject jsonObject = MessageManagerOfFile.askPacket(from, to, uuid, number);
            connectedByUDP.sendString(jsonObject.toString());
        }

        @Override
        public void error() {
            cacheFile.close();
            JSONObject jsonObject = MessageManagerOfFile.finishDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
            connectedByUDP.disconnect();
            ConnectorManager.getInstance().getSession(uuid).destroy();
        }

        @Override
        public void finish() {
            cacheFile.close();
            JSONObject jsonObject = MessageManagerOfFile.finishDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
            connectedByUDP.disconnect();
            ConnectorManager.getInstance().getSession(uuid).destroy();
        }
    };


}
