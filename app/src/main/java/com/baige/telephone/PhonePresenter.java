package com.baige.telephone;



import com.baige.connect.ConnectedByUDP;
import com.baige.connect.NetServerManager;
import com.baige.data.entity.FriendView;
import com.baige.data.source.Repository;
import com.baige.data.source.cache.CacheRepository;


import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by baige on 2017/10/29.
 */

public class PhonePresenter implements PhoneContract.Presenter {

    private final static String TAG = PhonePresenter.class.getCanonicalName();

    private PhoneFragment mPhonefragment;

    private Repository mRepository;

    private FriendView mFriendView;


    public PhonePresenter(Repository repository, PhoneFragment phoneFragment) {
        mRepository = checkNotNull(repository);
        mPhonefragment = checkNotNull(phoneFragment);
        mPhonefragment.setPresenter(this);
    }

    public FriendView getFriendView() {
        return mFriendView;
    }

    public void setFriendView(FriendView friendView) {
        this.mFriendView = friendView;
    }

    @Override
    public void start() {
        initPhoneLayout();
        TelePhone.getInstance().setOnTelePhoneListener(mTelePhoneListener);
    }

    private void initPhoneLayout() {
        TelePhone telePhone = TelePhone.getInstance();

        if(mFriendView != null){
            mPhonefragment.showFriend(mFriendView);
        }
        mPhonefragment.showDelayTime(telePhone.getDelayTime());
//        mPhonefragment.showName(telePhone.getTalkWithName());

        if(CacheRepository.getInstance().isP2PConnectSuccess()){
            ConnectedByUDP connectedByUDP = NetServerManager.getInstance().getUDPConnectorById(telePhone.getTalkWithId());
            if(connectedByUDP != null && connectedByUDP.isConnected()){
                mPhonefragment.showAddress(connectedByUDP.getAddress().getStringRemoteAddress());
            }else{
                mPhonefragment.showAddress(CacheRepository.getInstance().getServerIp()+":"+CacheRepository.getInstance().getServerUdpPort());
            }
        }else{
            mPhonefragment.showAddress(CacheRepository.getInstance().getServerIp()+":"+CacheRepository.getInstance().getServerUdpPort());
        }

        ArrayList<TelePhone.LogBean> logs = (ArrayList<TelePhone.LogBean>) telePhone.getLogs().clone();
        for (TelePhone.LogBean logBean : logs){
            mPhonefragment.showLog(logBean);
        }

        switch (telePhone.getStatus()) {
            case TelePhone.Status.LEISURE:
                mPhonefragment.showStatus("空闲");
                mPhonefragment.showProgress(false);
                break;
            case TelePhone.Status.CALLING:
                mPhonefragment.hidePickUpBtn();
                mPhonefragment.showProgress(true);
                mPhonefragment.showStatus("正在呼叫");
                break;
            case TelePhone.Status.CALLED:
                mPhonefragment.showStatus("被呼叫中");
                mPhonefragment.showProgress(false);

                break;
            case TelePhone.Status.BUSY:

                mPhonefragment.hidePickUpBtn();
                mPhonefragment.showStatus("通话中");
                mPhonefragment.showProgress(false);
                break;
            case TelePhone.Status.ERROR:
                mPhonefragment.showStatus("错误");
                mPhonefragment.showProgress(false);
                break;
        }
    }


    @Override
    public void stop() {

    }

    @Override
    public void onHangUp() {
        mPhonefragment.showLog("您已挂断电话");
        TelePhone.getInstance().onHangUp();
        CacheRepository.getInstance().setP2PConnectSuccess(false);
    }


    @Override
    public void onPickUp() {
        //TODO 远程服务器执行接听指令，正确连接之后修改状态
        mPhonefragment.showLog("您已接听电话");
        TelePhone.getInstance().onPickUp();
    }

    private TelePhone.OnTelePhoneListener mTelePhoneListener = new TelePhone.OnTelePhoneListener() {

        @Override
        public void showTip(String text) {
            mPhonefragment.showTip(text);
        }

        @Override
        public void showLog(String text) {
            mPhonefragment.showLog(text);
        }

        @Override
        public void showName(String name) {
            mPhonefragment.showName(name);
        }

        @Override
        public void showAddress(String address) {
            mPhonefragment.showAddress(address);
        }

        @Override
        public void exceptionCaught(Throwable cause) {
            mPhonefragment.showLog("异常:" + cause.getMessage());
        }

        @Override
        public void showDelay(long delay) {
            mPhonefragment.showDelayTime(delay);
        }
        public void onStop(){
            mPhonefragment.close();
            TelePhone.getInstance().setOnTelePhoneListener(null);
        }

        @Override
        public void onChange(int status) {
            switch (status) {
                case TelePhone.Status.LEISURE:
                    mPhonefragment.showStatus("空闲");
                    mPhonefragment.showProgress(false);
                    break;
                case TelePhone.Status.CALLING:
                    mPhonefragment.hidePickUpBtn();
                    mPhonefragment.showProgress(true);
                    mPhonefragment.showStatus("正在呼叫");
                    break;
                case TelePhone.Status.CALLED:
                    mPhonefragment.showStatus("被呼叫中");
                    mPhonefragment.showProgress(false);

                    break;
                case TelePhone.Status.BUSY:

                    mPhonefragment.hidePickUpBtn();
                    mPhonefragment.showStatus("通话中");
                    mPhonefragment.showProgress(false);
                    break;
                case TelePhone.Status.ERROR:
                    mPhonefragment.showStatus("错误");
                    mPhonefragment.showProgress(false);
                    break;
            }
        }

        @Override
        public void onSpeakerphoneChange(boolean on) {
//            if(on){
//                mPhonefragment.showTip("打开免提");
//            }else{
//                mPhonefragment.showTip("关闭免提");
//            }
            mPhonefragment.setSpeakerphoneOn(on);
        }
    };

}
