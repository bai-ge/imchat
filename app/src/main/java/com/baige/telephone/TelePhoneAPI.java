package com.baige.telephone;


/**
 * Created by baige on 2017/12/24.
 */

public interface TelePhoneAPI {

    //状态
    boolean isLeisure();
    boolean isCalling();
    boolean beCalled();
    boolean isBusy();

    //TCP发出呼叫命令
    void afxCallTo(String deviceId, String name);

    void callTo(String deviceId, String name);

    void afxBeCall(String deviceId, String name);

    void beCall(String deviceId, String name);

    void connectSuccess();//连接成功

    void oppBusy();//对方正在通话中

    //包括TCP发送指令 （界面使用这里）
    void onHangUp();

    void onPickUp();

    //远程控制
    void helpPickUp();

    void helpLoudSpeech();

    boolean changeSpeakerphoneOn();

    void setSpeakerphoneOn(boolean on);

    boolean isSpeakerphoneOn();

    void onNetworkChange();

    //（消息接收器用这里）
    void canTalk();

    void stop();
}
