package com.baige.telephone;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import com.speex.util.SpeexUtil;

/**
 * Created by Kevin on 2016/10/24.
 * 录音机
 */
public class SpeexTalkRecorder {

    private static final String TAG = SpeexTalkPlayer.class.getCanonicalName();

    private int DEFAULT_SAMPLERATEINHZ = 8000;                             // 采样频率
    private int DEFAULT_CHANNELCONFIG_IN = AudioFormat.CHANNEL_IN_MONO;    // 声道配置
    private int DEFAULT_AUDIOFORMAT = AudioConfig.audioFormat;             // 音频格式
    private int DEFAULT_AUDIOSOURCE = AudioConfig.audioSource;             // 音频来源
    /**
     * 音频帧数据长度
     *  通常一个音频帧为20ms内的音频数据，
     *  由于 码率 = 8K * 16bit * 1 = 108Kbps = 16KBps，
     *  所以 音频帧数据大小为 int size = 20ms * 16KBps = 320 Byte = 160 Short，
     *  即每个音频帧的数据大小为 320个字节，或者为160个Short。
     */
    private int audioShortArrayLength = 160;

    private RecorderThread recorderThread;  // 录音线程
    private AudioRecord recorder;           // 录音对象
    private boolean isRunning;              // 录音线程是否运行
    private boolean isWorking;              // 录音线程是否工作(录音)
    private SpeexUtil speex;                    // Speex音频编解码器

    private OnRecorderListener mListener;
    private int recordBufferSize;

    public SpeexTalkRecorder(OnRecorderListener recorderListener) {
        this.mListener = recorderListener;
        init();
    }

    /** 初始化 */
    private void init() {

        Log.d(TAG, "开始创建录音对象...");

        //1. 获取最小的缓冲区大小
        recordBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLERATEINHZ,
                DEFAULT_CHANNELCONFIG_IN, DEFAULT_AUDIOFORMAT);
        switch (recordBufferSize) {
            case AudioRecord.ERROR_BAD_VALUE:
                Log.d(TAG, "无效的音频参数");
                break;
            case AudioRecord.ERROR:
                Log.d(TAG, "不能够查询音频输入的性能");
                break;
            default:
                Log.d(TAG, "AudioRecord的音频缓冲区的最小尺寸(与本机硬件有关)：" + recordBufferSize);
                break;
        }

        //2. 创建AudioRecord实例
        recorder = new AudioRecord(DEFAULT_AUDIOSOURCE, DEFAULT_SAMPLERATEINHZ,
                DEFAULT_CHANNELCONFIG_IN, DEFAULT_AUDIOFORMAT, recordBufferSize * 4);
        switch (recorder.getState()) {
            case AudioRecord.STATE_INITIALIZED:
                Log.d(TAG, "AudioTrack实例初始化成功!");
                break;
            case AudioRecord.STATE_UNINITIALIZED:
                Log.d(TAG, "AudioTrack实例初始化失败!");
                break;
        }

        //3. 创建Speex编解码实例
        speex = SpeexUtil.getInstance();
        //4. 创建录音线程
        isRunning = true;
        isWorking = false;
        recorderThread = new RecorderThread();
    }

    /** 开始录音 */
    public void start() {

        if (isWorking) {
            if(mListener != null){
                mListener.showTip("正在录音中!");
            }
            return;
        }

        try {
            //5. 开始录音
            recorderThread.start();
            recorder.startRecording();
            isWorking = true;
        } catch (Exception e){
            e.printStackTrace();
            if(mListener != null){
                mListener.exceptionCaught(e);
            }
        }
    }

    /** 停止录音 */
    public void stop() {

        if (!isWorking) {
            if(mListener != null){
                mListener.showTip("已经停止录音!");
            }
        }

        //9. 停止录音
        try {

            recorder.stop();
            recorder.release();
            recorder = null;

            isWorking = false;
            recorderThread.interrupt();
            recorderThread.join(1000);  // 先停止录音线程，然后延时1s再停止播放器，防止程序崩溃

        }catch (Exception e) {
            e.printStackTrace();
            if(mListener != null){
                mListener.exceptionCaught(e);
            }
        }
    }

    /** 录音线程 */
    private class RecorderThread extends Thread {

        private short[] recordData = new short[audioShortArrayLength];          // 读取音频数据存放的数组 160 Short
        private byte[] encodedbytes = new byte[AudioConfig.SPEEX_DATA_SIZE];  // 编码之后的音频数据     20 Byte

        @Override
        public void run() {
            super.run();

            while (isRunning) {

                if (isWorking) {

                    //6. 读取语音信息
                    int readNumber = recorder.read(recordData, 0, recordData.length);
                    switch (readNumber) {
                        case AudioRecord.ERROR_INVALID_OPERATION:
                            Log.d(TAG, "读取语音信息...发现实例初始化失败！");
                            break;
                        case AudioRecord.ERROR_BAD_VALUE:
                            Log.d(TAG, "读取语音信息...发现参数无效！");
                            break;
                        default:
                            Log.d(TAG, "读取到的语音数据的长度：" + readNumber + " Shorts");

                            //7. 语音压缩-对音频数据编码
                            speex.encode(recordData,0,encodedbytes,readNumber);

                            //8. 处理音频数据
                            if(mListener != null){
                                mListener.handleRecordData(encodedbytes);
                            }
                            break;
                    }
                }
            }
        }
    }

    /** 获取录音数据接口 */
    public interface OnRecorderListener {
        void showTip(String text);
        void exceptionCaught(Throwable cause);
        void handleRecordData(byte[] recordData);
    }
}