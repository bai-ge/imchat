package com.baige.p2pcore;

import android.util.Log;

import com.baige.connect.SocketPacket;
import com.baige.util.Tools;

import java.util.UUID;

/**
 * Created by baige on 2018/5/28.
 */

public class SlipWindow {

    private final static String TAG = SlipWindow.class.getSimpleName();
    private String uuid;
    private int windowCount;
    private long currentPosition;
    private int startIndex; // 窗口开始的下标，即与currentPosition 重合的下标是哪一个

    private boolean[] affirmTab;

    private SocketPacket[] socketPackets;

    private long[] realNum;

    private long sendNum;

    private long maxPacketNum;

    private Object windowLock = new Object();

    private SendWindow mSendWindow;

    private ReceiveWindow mReceiveWindow;

    private PacketWriter mPacketWriter;

    private PacketReader mPacketReader;

    private int affirmCountDown;

    public SlipWindow() {
        windowCount = 5;
        currentPosition = 0;
        uuid = UUID.randomUUID().toString();

    }

    private void init() {
        affirmTab = new boolean[windowCount];
        socketPackets = new SocketPacket[windowCount];
        realNum = new long[windowCount];
        sendNum = 0;
        currentPosition = 0;
        startIndex = 0;
        affirmCountDown = windowCount;
    }

    public SendWindow buildSendWindow(PacketReader packetReader) {
        if (mSendWindow != null) {
            throw new IllegalStateException("This is a ReceiveWindow. Don't rebuild the SilpWindow!");
        }
        if (mSendWindow == null) {
            init();
            mSendWindow = new SendWindow();
        }
        this.mPacketReader = packetReader;
        return mSendWindow;
    }

    public ReceiveWindow buildReceiveWindow(PacketWriter packetWriter) {
        if (mSendWindow != null) {
            throw new IllegalStateException("This is a SendWindow. Don't rebuild the SilpWindow!");
        }
        if (mReceiveWindow == null) {
            init();
            mReceiveWindow = new ReceiveWindow();
        }
        this.mPacketWriter = packetWriter;
        return mReceiveWindow;
    }


    class ReceiveWindow {
        /**
         * 接收消息者
         * 向前滑动一格
         */
        private void slip() {
            SocketPacket nowPacket = null;
            Log.d(TAG, "保存数据包" + currentPosition);
            synchronized (windowLock) {
                int newIndex = startIndex;
                nowPacket = socketPackets[startIndex];
                currentPosition++;
                startIndex = (startIndex + 1) % windowCount;
                affirmTab[newIndex] = false;
            }

            if (nowPacket != null) {
                boolean isFinish = mPacketWriter.write(nowPacket);
                if (isFinish) {
                    mPacketWriter.finish();
                }
            } else {
                //TODO 可能已经结束
                Log.d(TAG, "接收窗口错误，packet = null");
            }
        }

        /**
         * @param packet
         * @param uuid
         * @param num
         * @return
         */
        public boolean receivePacket(SocketPacket packet, String uuid, long num) {
            boolean res = false;
            Log.d(TAG, "收到数据包" + num);
            if (Tools.isEquals(getUUID(), uuid)) {
                synchronized (windowLock) {
                    if (num >= currentPosition && num < currentPosition + windowCount) {
                        int realPosition = (int) ((num - currentPosition + startIndex) % windowCount);
                        affirmTab[realPosition] = true;
                        socketPackets[realPosition] = packet;
                        res = true;
                    } else {
                        mPacketWriter.askPacket(getUUID(), currentPosition);
                        Log.e(TAG, "错误数据包" + num);
                    }
                    while (affirmTab[startIndex]) {
                        //需要向后偏移
                        slip();
                    }
                    /*每收到五个包确认一次*/
                    affirmCountDown--;
                    if (affirmCountDown == 0) {
                        affirmCountDown = windowCount;
                        mPacketWriter.responeAffirm(getUUID(), currentPosition-1);
                    }

                }
            }
            return res;
        }

        public String getUUID() {
            return uuid;
        }

        public void askPacket(){
            mPacketWriter.askPacket(getUUID(), currentPosition);
        }

    }

    public class SendWindow {

        /**
         * 收到对方确认包
         * 返回是否已经正确处理
         *
         * @param uuid
         * @param num
         * @return
         */
        public boolean affirmReceive(String uuid, long num) {
            boolean res = false;
            Log.d(TAG, "确认收到数据包" + num);
            if (Tools.isEquals(getUUID(), uuid)) {
                synchronized (windowLock) {
                    if (num >= currentPosition && num < currentPosition + windowCount) {
                        int realPosition = (int) ((num - currentPosition + startIndex) % windowCount);
                        affirmTab[realPosition] = true;
                        for (int i = 0; i < windowCount; i++) {
                            if (realNum[i] <= num) {
                                affirmTab[i] = true;
                                Log.i(TAG, "确认之前的包" + realNum[i]);
                            }
                        }
                        res = true;
                    }
                    while (affirmTab[startIndex]) {
                        //需要向后偏移
                        slip();
                    }
                }

            }
            return res;
        }

        public boolean askPacket(String uuid, long num) {
            boolean res = false;
            Log.d(TAG, "请求数据包" + num);
            if (Tools.isEquals(getUUID(), uuid)) {
                synchronized (windowLock) {
                    if (num >= currentPosition && num < currentPosition + windowCount) {
                        int realPosition = (int) ((num - currentPosition + startIndex) % windowCount);
                        if (realNum[realPosition] == num) {
                            mPacketReader.sendSocketPacket(socketPackets[realPosition]);
                            if(num > (getMaxPacketNum() - getWindowCount())){ // 最后几个数据包，发送端已经不会自动发送新的数据包了
                                for (int i = 0; i < windowCount; i++) {
                                    if (realNum[i] > num) {
                                        mPacketReader.sendSocketPacket(socketPackets[i]);
                                        break;
                                    }
                                }
                            }
                            res = true;
                        } else {
                            Log.e(TAG, "请求数据包" + num + ",realPosition 计算不准确，当前位置" + currentPosition + ", realNum[realPosition]=" + realNum[realPosition]);
                            for (int i = 0; i < windowCount; i++) {
                                if (realNum[i] == num) {
                                    mPacketReader.sendSocketPacket(socketPackets[i]);
                                    res = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "请求数据包" + num + ",但内存中没有，当前位置" + currentPosition);
                    }
                }
            }
            if(res){
                affirmReceive(uuid, num - 1);
            }
            return res;
        }

        public synchronized boolean startSend() {
            boolean res = false;
            Log.d(TAG, "开始发送数据包" + currentPosition);
            if (sendNum == 0) {
                res = true;
                for (int i = 0; i < windowCount; i++) {
                    SocketPacket sendPacket = null;
                    synchronized (windowLock) {
                        affirmTab[i] = false;
                        sendPacket = mPacketReader.read(getUUID(), sendNum);
                        if (sendPacket != null) {
                            socketPackets[i] = sendPacket;
                            realNum[i] = sendNum;
                            mPacketReader.sendSocketPacket(sendPacket);
                            Log.i(TAG, "数据包已经发送" + sendNum);
                            sendNum++;
                        } else {
                            Log.d(TAG, "数据包已经发送结束，等待对方关闭连接");
                            Log.i(TAG, "数据包为空" + i);
                            break;
                        }
                    }
                }
            }
            return res;
        }

        /**
         * 向前滑动一格
         */
        private void slip() {
            SocketPacket nextPacket = null;
            synchronized (windowLock) {
                int newIndex = startIndex;
                currentPosition++;
                startIndex = (startIndex + 1) % windowCount;

                affirmTab[newIndex] = false;
//                sendNum =  currentPosition + windowCount - 1;
                nextPacket = mPacketReader.read(getUUID(), sendNum);
                if (nextPacket != null) {
                    socketPackets[newIndex] = nextPacket;
                    realNum[newIndex] = sendNum;
                    mPacketReader.sendSocketPacket(nextPacket);
                    Log.i(TAG, "数据包已经发送" + sendNum);
                    sendNum++;
                } else {
                    //TODO 可能已经结束, 等待对方断开连接
                    Log.d(TAG, "数据包已经发送结束，等待对方关闭连接");
                    Log.i(TAG, "数据包为空" + (sendNum));
                }
            }
        }

        public String getUUID() {
            return uuid;
        }

        public long getMaxPacketNum(){
            return maxPacketNum;
        }

        public void setMaxPacketNum(long max){
            if(max < 0){
                max = 0;
            }
            maxPacketNum = max;
        }

    }


    public int getWindowCount() {
        return windowCount;
    }

    public SlipWindow setWindowCount(int windowCount) {
        this.windowCount = windowCount;
        return this;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }


    public String getUUID() {
        return uuid;
    }

    public SlipWindow setUUID(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
