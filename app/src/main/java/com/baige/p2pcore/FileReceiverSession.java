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

import java.util.Timer;
import java.util.TimerTask;

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

    long lastReceiveTime;
    int failCount;

    Timer mTimer = null;
    TimerTask mTimerTask = null;

    int slipWindowCount = 5;


    private void startDownloadTask(){
        if(mTimerTask == null){
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    if(System.currentTimeMillis() - lastReceiveTime > 5000){
                        failCount ++;
                        Log.e(TAG, "下载失败一次，共"+failCount);
                        if(failCount < 20){
                            receiveWindow.askPacket();
                        }else{
                            mPacketWriter.error();
                        }
                    }
                }
            };
        }
        if(mTimer == null){
            mTimer = new Timer();
            mTimer.schedule(mTimerTask, 5000, 5000);
        }
    }

    private void stopDownloadTask(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
            mTimerTask = null;
        }
    }


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
        receiveWindow = new SlipWindow().setUUID(uuid).setWindowCount(slipWindowCount).buildReceiveWindow(mPacketWriter);
    }

    public synchronized void startDownload(){
        if(!isReceiveData){
            JSONObject jsonObject = MessageManagerOfFile.startDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
            lastReceiveTime = System.currentTimeMillis();
            failCount = 0;
            startDownloadTask();
        }
    }

    public void receivePacket(SocketPacket packet, String uuid, long number){
        if(!isReceiveData){
            isReceiveData = true;
            startTime = System.currentTimeMillis();
        }
        lastReceiveTime = System.currentTimeMillis();
        failCount = 0;

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
            stopDownloadTask();
            cacheFile.close();
            JSONObject jsonObject = MessageManagerOfFile.finishDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
            connectedByUDP.disconnect();
            ConnectorManager.getInstance().getSession(uuid).destroy();
        }

        @Override
        public void finish() {
            stopDownloadTask();
            cacheFile.close();
            JSONObject jsonObject = MessageManagerOfFile.finishDownload(from, to, uuid);
            connectedByUDP.sendString(jsonObject.toString());
            connectedByUDP.disconnect();
            ConnectorManager.getInstance().getSession(uuid).destroy();
        }
    };

    public int getSlipWindowCount() {
        return slipWindowCount;
    }

    public void setSlipWindowCount(int slipWindowCount) {
        this.slipWindowCount = slipWindowCount;
    }

}
