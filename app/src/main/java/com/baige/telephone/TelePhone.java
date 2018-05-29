package com.baige.telephone;


import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;

import com.baige.BaseApplication;
import com.baige.callback.BaseResponseBinder;
import com.baige.callback.PushCallback;
import com.baige.data.entity.BaseEntity;
import com.baige.p2pcore.ConnectorManager;
import com.baige.pushcore.SendMessageBroadcast;
import com.baige.callback.CallbackManager;
import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.connect.SocketPacket;
import com.baige.connect.msg.MessageManager;
import com.baige.data.entity.Candidate;
import com.baige.data.entity.DeviceModel;
import com.baige.data.entity.User;
import com.baige.data.source.cache.CacheRepository;
import com.baige.util.IPUtil;
import com.baige.util.Tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/30.
 * <p>
 * TODO 即将改为服务，通过发送广播控制通话服务器
 */

public class TelePhone implements SpeexTalkRecorder.OnRecorderListener, SpeexTalkPlayer.OnPlayerListener, TelePhoneAPI {

    private static final String TAG = TelePhone.class.getCanonicalName();

    private static TelePhone INSTANCE = null;

    private SpeexTalkRecorder recorder;

    private SpeexTalkPlayer player;

    private OnTelePhoneListener mListener;

    private int mStatus;

    private String mTalkWith; //通话对方的ID

    private DeviceModel mTalkWithDevice = null;

    private boolean mOppSpeakerphone = false;

    private String mTalkWithName = ""; //通话对方的名字

    private static ExecutorService fixedThreadPool = null;

    private long mDelayTime;//真正的网络延时

    private long mDiffTime; //两个系统时间的差值，包括网络延时

    private long mPlayTime;

    private boolean mbSpeakerOn;

    private ByteBuffer voiceBuf;

    private static MediaPlayer mMediaPlayer;

    private static Vibrator mVibrator;

    private ArrayList<LogBean> logs = new ArrayList<>();

    private TelePhone() {
        mStatus = Status.LEISURE;
        voiceBuf = ByteBuffer.allocate(20 * 20);//20ms * 20个
        mMediaPlayer = new MediaPlayer();
        fixedThreadPool = Executors.newFixedThreadPool(5);//创建最多能并发运行5个线程的线程池

//        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                if(isCalling() || beCalled()){
//                    mMediaPlayer.start();
//                }
//            }
//        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.setLooping(true);
                mMediaPlayer.start();
            }
        });
    }

    public static TelePhone getInstance() {
        if (INSTANCE == null) {
            synchronized (TelePhone.class) { //对获取实例的方法进行同步
                if (INSTANCE == null) {
                    INSTANCE = new TelePhone();
                }
            }
        }
        return INSTANCE;
    }

    public void startActivity(Class activity) {
        if (activity != null) {
            Intent intent = new Intent(BaseApplication.getAppContext(), PhoneActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            BaseApplication.getAppContext().startActivity(intent);
        }
    }


    public String getTalkWithId() {
        return mTalkWith;
    }

    public void setTalkWithId(String mTalkWith) {
        this.mTalkWith = mTalkWith;
    }

    public boolean isOppSpeakerphone() {
        return mOppSpeakerphone;
    }

    public void setOppSpeakerphone(boolean on) {
        this.mOppSpeakerphone = on;
    }

    public String getTalkWithName() {
        return mTalkWithName;
    }

    public void setTalkWithName(String talkWithName) {
        this.mTalkWithName = talkWithName;
    }

    public void setOnTelePhoneListener(OnTelePhoneListener listener) {
        this.mListener = listener;
    }

    public void play(byte[] recordData) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(recordData);
        byte[] voice = new byte[20];
        if (player != null) {
            mPlayTime = System.currentTimeMillis();
            while (byteBuffer.remaining() >= voice.length) {
                byteBuffer.get(voice);
                synchronized (TelePhone.class) {
                    if (player != null) {
                        player.play(voice);
                    } else {
                        return;
                    }
                }
            }
        }
        if (mListener != null) {
            mListener.showDelay(mDelayTime);
        }
    }

    public void ring(Context context, Uri uri) {
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(context, uri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            ring();
        }
    }

    public void ring() {
        mMediaPlayer.reset();
        try {
            AssetManager assetManager = BaseApplication.getAppContext().getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("mi_ring.ogg");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 振动
     */
    public void vibrate() {
        mVibrator = (Vibrator) BaseApplication.getAppContext().getSystemService(Context.VIBRATOR_SERVICE);
        // 前一个代表等待多少毫秒启动vibrator，后一个代表vibrator持续多少毫秒停止。
        // 从repeat索引开始的振动进行循环。-1表示只振动一次，非-1表示从pattern的指定下标开始重复振动。
        mVibrator.vibrate(new long[]{1000, 1000}, 0);
    }

    public void stopRing() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
        if (mVibrator != null && mVibrator.hasVibrator()) {
            mVibrator.cancel();
        }
    }

    public int getStatus() {
        return mStatus;
    }

    private void setStatus(int status) {
        //TODO 进行严格的状态转换
        mStatus = status;
        if (mListener != null) {
            mListener.onChange(status);
        }
    }

    public long getDelayTime() {
        return mDelayTime;
    }

    public void setDelayTime(long delayTime) {
        this.mDelayTime = delayTime;
    }

    public long getDiffTime() {
        return mDiffTime;
    }

    public void setDiffTime(long diffTime) {
        this.mDiffTime = diffTime;
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        if (mListener != null) {
            mListener.exceptionCaught(cause);
        }
    }

    @Override
    public void handleRecordData(byte[] recordData) {
//        if (mStatus == Status.IS_WORKING && mConnectedByUDP != null) {
//            mConnectedByUDP.send(ConnectedByUDP.MessageTag.VOICE, recordData);
//        }
        Log.d(TAG, "录音数据" + recordData.length);
        NetServerManager netServerManager = NetServerManager.getInstance();
        CacheRepository cacheRepository = CacheRepository.getInstance();
        ConnectedByUDP connectedByUDP;
        if (System.currentTimeMillis() - mPlayTime >= 10 * 1000) {
            //掉线了
            showTip("对方已经掉线");
            if (mListener != null) {
                mListener.exceptionCaught(new IOException());
            }
            stop();
        }
        if (voiceBuf.remaining() == recordData.length) {
            voiceBuf.put(recordData);
            SocketPacket socketPacket = MessageManager.voice(cacheRepository.getDeviceId(), getTalkWithId(), voiceBuf.array(), (int) getDiffTime());
            if (!socketPacket.isPacket()) {
                socketPacket.packet();
            }
            if (cacheRepository.isP2PConnectSuccess()) {
                connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
                if (connectedByUDP != null && connectedByUDP.isConnected()) {
                    connectedByUDP.sendPacket(socketPacket);
                } else {
                    netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
                }
            } else {
                netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
            }
            voiceBuf.position(0);
        } else if (voiceBuf.remaining() < recordData.length) {
            int pos = voiceBuf.position();
            byte[] voice = new byte[pos];
            voiceBuf.position(0);
            voiceBuf.get(voice);
            SocketPacket socketPacket = MessageManager.voice(cacheRepository.getDeviceId(), getTalkWithId(), voiceBuf.array(), (int) getDiffTime());
            if (!socketPacket.isPacket()) {
                socketPacket.packet();
            }

            if (cacheRepository.isP2PConnectSuccess()) {
                connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
                if (connectedByUDP != null && connectedByUDP.isConnected()) {
                    connectedByUDP.sendPacket(socketPacket);
                } else {
                    netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
                }
            } else {
                netServerManager.sendMessage(cacheRepository.getServerIp(), cacheRepository.getServerUdpPort(), socketPacket.getAllBuf());
            }

            voiceBuf.position(0);
            voiceBuf.put(recordData);
        } else {
            voiceBuf.put(recordData);
        }
        //  buf = null;//手动释放，数据量太大了
    }


    private void afxCheckUdpConnector() {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                checkUdpConnector();
            }
        });
    }

    private void checkUdpConnector() {
        showLog("检查网络状态");
        showLog("server IP =" + CacheRepository.getInstance().getServerIp());
        showLog("server tcp port =" + CacheRepository.getInstance().getServerPort());
        showLog("server udp port =" + CacheRepository.getInstance().getServerUdpPort());
        NetServerManager netServerManager = NetServerManager.getInstance();
        showLog("Local udp port =" + netServerManager.getUdpPort());
        CacheRepository cacheRepository = CacheRepository.getInstance();
        PushCallback callback = new PushCallback() {
            @Override
            public void timeout() {
                super.timeout();
            }

            @Override
            public synchronized void loadObject(Object obj) {
                super.loadObject(obj);
                if (obj instanceof Candidate) {
                    Candidate candidate = (Candidate) obj;
                    showLog("From " + candidate.getFrom());
                    showLog("Local " + candidate.getLocalIp() + ":" + candidate.getLocalPort());
                    showLog("Remote " + candidate.getRemoteIp() + ":" + candidate.getRemotePort());
                    showLog("Relay " + candidate.getRelayIp() + ":" + candidate.getRelayPort());
                    setDelayTime((long) (candidate.getDelayTime() * 1.0 / 2));
                    if (mListener != null) {
                        mListener.showDelay((long) (candidate.getDelayTime() * 1.0 / 2));
                    }
                    ConnectorManager.getInstance().add(candidate);
                }
            }
        };
       // callback.setResponseBinder(new BaseResponseBinder<Candidate>("candidate"));
        String callId = Tools.ramdom();
        callback.setTimeout(8000);
        callback.setId(callId);
        CallbackManager.getInstance().put(callback);
        String msg = MessageManager.udpTest(cacheRepository.getDeviceId(), callId, IPUtil.getLocalIPAddress(true), netServerManager.getUdpPort() + "");
        netServerManager.tryUdpTest(msg);
    }

    @Override
    public void afxBeCall(final String deviceId, final String name) {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                beCall(deviceId, name);
            }
        });
    }

    @Override
    public void beCall(String deviceId, String name) {
        checkNotNull(deviceId);
        if (mStatus == Status.LEISURE) {
            showLog("id = " + CacheRepository.getInstance().getDeviceId());
            showLog("be call =" + deviceId);
            setTalkWithName(name);
            if (mListener != null) {
                mListener.showName(name);
            }
            mTalkWith = deviceId;
            setStatus(Status.CALLED);
            setSpeakerphoneOn(false);
            String ringUri = CacheRepository.getInstance().getRingUri();
            boolean bVibrate = CacheRepository.getInstance().isPhoneVibrate();
            if (!CacheRepository.getInstance().isSilence()) {
                if (!Tools.isEmpty(ringUri)) {
                    ring(BaseApplication.getAppContext(), Uri.parse(ringUri));
                } else {
                    ring();
                }
            }
            if (bVibrate) {
                vibrate();
            }
            checkUdpConnector();
            String msg = MessageManager.replyCallTo(deviceId);
            SendMessageBroadcast.getInstance().sendMessage(msg);
        } else {
            showLog("错误状态, 当前" + mStatus);
        }
    }

//    /** The audio stream for phone calls */
//    public static final int STREAM_VOICE_CALL = AudioSystem.STREAM_VOICE_CALL;
//    /** The audio stream for system sounds */
//    public static final int STREAM_SYSTEM = AudioSystem.STREAM_SYSTEM;
//    /** The audio stream for the phone ring */
//    public static final int STREAM_RING = AudioSystem.STREAM_RING;
//    /** The audio stream for music playback */
//    public static final int STREAM_MUSIC = AudioSystem.STREAM_MUSIC;
//    /** The audio stream for alarms */
//    public static final int STREAM_ALARM = AudioSystem.STREAM_ALARM;
//    /** The audio stream for notifications */
//    public static final int STREAM_NOTIFICATION = AudioSystem.STREAM_NOTIFICATION;
//    /** @hide The audio stream for phone calls when connected to bluetooth */
//    public static final int STREAM_BLUETOOTH_SCO = AudioSystem.STREAM_BLUETOOTH_SCO;
//    /** @hide The audio stream for enforced system sounds in certain countries (e.g camera in Japan) */
//    public static final int STREAM_SYSTEM_ENFORCED = AudioSystem.STREAM_SYSTEM_ENFORCED;
//    /** The audio stream for DTMF Tones */
//    public static final int STREAM_DTMF = AudioSystem.STREAM_DTMF;
//    /** @hide The audio stream for text to speech (TTS) */
//    public static final int STREAM_TTS = AudioSystem.STREAM_TTS;

    @Override
    public void connectSuccess() {
        mMediaPlayer.reset();
        try {
            AssetManager assetManager = BaseApplication.getAppContext().getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("connect_success.mp3");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showLog("对方连接成功");
    }

    @Override
    public void oppBusy() {
        mMediaPlayer.reset();
        try {
            AssetManager assetManager = BaseApplication.getAppContext().getAssets();
            AssetFileDescriptor fileDescriptor = assetManager.openFd("busy.mp3");
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        showLog("对方正忙");
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(8000);
                    INSTANCE.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void afxCallTo(final String deviceId, final String name) {
        fixedThreadPool.submit(new Runnable() {
            @Override
            public void run() {
                callTo(deviceId, name);
            }
        });
    }

    @Override
    public synchronized void callTo(String deviceId, String name) {
        checkNotNull(deviceId);
        if (mStatus == Status.LEISURE) {
            showLog("id = " + CacheRepository.getInstance().getDeviceId());
            showLog("call to =" + deviceId);
            setTalkWithName(name);
            if (mListener != null) {
                mListener.showName(name);
            }
            mTalkWith = deviceId;
            setStatus(Status.CALLING);
            setSpeakerphoneOn(false);
            checkUdpConnector();

            //TODO 传输自己的名字
            User user = CacheRepository.getInstance().who();
            name = deviceId;
            if (user != null) {
                name = user.getName();
            }
            String msg = MessageManager.callTo(deviceId, name, user.getId());
            SendMessageBroadcast.getInstance().sendMessage(msg);
        } else {
            showLog("错误状态, 当前" + mStatus);
        }
    }

    @Override
    public void onHangUp() {
        stop();
        SendMessageBroadcast.getInstance().sendMessage(MessageManager.onHangUp(mTalkWith));
    }

    @Override
    public void onPickUp() {
        canTalk();
        SendMessageBroadcast.getInstance().sendMessage(MessageManager.onPickUp(mTalkWith));
    }

    @Override
    public void helpPickUp() {
        onPickUp();
    }

    @Override
    public void helpLoudSpeech() {
        setSpeakerphoneOn(true);
    }

    @Override
    public void onNetworkChange() {
        CacheRepository cacheRepository = CacheRepository.getInstance();
        NetServerManager netServerManager = NetServerManager.getInstance();
        ConnectedByUDP connectedByUDP = null;
        if (cacheRepository.isP2PConnectSuccess()) {
            connectedByUDP = netServerManager.getUDPConnectorById(mTalkWith);
            if (connectedByUDP != null && connectedByUDP.isConnected()) {
                if (mListener != null) {
                    mListener.showAddress(connectedByUDP.getAddress().getStringRemoteAddress());
                }
            } else {
                if (mListener != null) {
                    mListener.showAddress(cacheRepository.getServerIp() + ":" + cacheRepository.getServerUdpPort());
                }
            }
        } else {
            if (mListener != null) {
                mListener.showAddress(cacheRepository.getServerIp() + ":" + cacheRepository.getServerUdpPort());
            }
        }
    }

    @Override
    public void canTalk() {
        stopRing();
        mPlayTime = System.currentTimeMillis();
        if (recorder == null) {
            synchronized (TelePhone.class) {
                if (recorder == null) {
                    recorder = new SpeexTalkRecorder(TelePhone.this); // 创建录音对象
                    recorder.start(); // 开始录音
                    setStatus(Status.BUSY);
                }
            }
        }
        if (player == null) {
            synchronized (TelePhone.class) {
                if (player == null) {
                    player = new SpeexTalkPlayer();         // 创建播放器对象
                    player.setOnPlayerListener(this);
                    setStatus(Status.BUSY);
                    setSpeakerphoneOn(mbSpeakerOn);
                }
            }
        }
        showLog("开始通话");
    }

    public DeviceModel getTalkWithDevice() {
        return mTalkWithDevice;
    }

    public void setTalkWithDevice(DeviceModel mTalkWithDevice) {
        this.mTalkWithDevice = mTalkWithDevice;
    }

    public void stop() {
        synchronized (TelePhone.class) {
            stopRing();
            if (mStatus == Status.BUSY) {
                if (recorder != null) {
                    recorder.stop();    // 停止录音
                }
                if (player != null) {
                    player.stop();      // 停止播放
                }
                recorder = null;
                player = null;
            }
            setStatus(Status.LEISURE);
            mDelayTime = 0;
            mDiffTime = 0;
            showLog("停止通话");

            setTalkWithDevice(null);
            CacheRepository.getInstance().setP2PConnectSuccess(false);
            if (mListener != null) {
                mListener.onStop();
            }
            ConnectedByUDP connector = NetServerManager.getInstance().getUDPConnectorById(mTalkWith);
            if (connector != null) {
                connector.disconnect();
            }
            clearLogs();
            setSpeakerphoneOn(true);
        }
    }

    private AudioManager mAudioManager;

    @Override
    public void setSpeakerphoneOn(boolean on) {
        mbSpeakerOn = on;
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) BaseApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (on && !mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);//打开扬声器
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            if (mListener != null) {
                mListener.onSpeakerphoneChange(true);
            }
        } else if (!on) {
            mAudioManager.setSpeakerphoneOn(false);//关闭扬声器
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            if (mListener != null) {
                mListener.onSpeakerphoneChange(false);
            }
        }
    }

    @Override
    public boolean changeSpeakerphoneOn() {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) BaseApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(false);//关闭扬声器
            mAudioManager.setMode(AudioManager.MODE_IN_CALL);
            if (mListener != null) {
                mListener.onSpeakerphoneChange(false);
            }
            mbSpeakerOn = false;
            return false;
        } else {
            mAudioManager.setSpeakerphoneOn(true);//打开扬声器
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            if (mListener != null) {
                mListener.onSpeakerphoneChange(true);
            }
            mbSpeakerOn = true;
            return true;
        }
    }

    @Override
    public boolean isSpeakerphoneOn() {
        if (mAudioManager == null) {

            mAudioManager = (AudioManager) BaseApplication.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        }
        return mAudioManager.isSpeakerphoneOn();
    }

    public void showLog(String text) {
        if (mListener != null) {
            mListener.showLog(text);
        }
        LogBean logBean = new LogBean(text);
        logs.add(logBean);
    }

    public void clearLogs() {
        logs.clear();
    }

    @Override
    public void showTip(String text) {
        if (mListener != null) {
            mListener.showTip(text);
        }
    }

    public interface OnTelePhoneListener {

        void showTip(String text);

        void showLog(String text);

        void showName(String name);

        void showDelay(long delay);

        void showAddress(String address);

        void exceptionCaught(Throwable cause);

        void onChange(int status);

        void onSpeakerphoneChange(boolean on);

        void onStop();
    }

    public ArrayList<LogBean> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<LogBean> logs) {
        this.logs = logs;
    }


    public class LogBean {
        long time;
        String log;

        public LogBean() {
            time = System.currentTimeMillis();

        }

        public LogBean(String text) {
            time = System.currentTimeMillis();
            this.log = text;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getLog() {
            return log;
        }

        public void setLog(String log) {
            this.log = log;
        }
    }

    @Override
    public boolean isLeisure() {
        return getStatus() == Status.LEISURE;
    }

    @Override
    public boolean isCalling() {
        return getStatus() == Status.CALLING;
    }

    @Override
    public boolean beCalled() {
        return getStatus() == Status.CALLED;
    }

    @Override
    public boolean isBusy() {
        return getStatus() == Status.BUSY;
    }

    public class Status {
        public static final int LEISURE = 0;
        public static final int CALLING = 1;
        public static final int CALLED = 2;
        public static final int BUSY = 3;
        public static final int ERROR = 5;
    }
}
