package com.baige.callback;


import java.util.List;

/**
 * Created by baige on 2018/5/18.
 */

public class PushCallback extends BaseCallback{


    private AbstractResponseBinder mResponseBinder;


    public void setResponseBinder(AbstractResponseBinder responseBinder) {
        this.mResponseBinder = responseBinder;
    }

    public void response(String json) { // 可重写
        if(mResponseBinder != null){
            mResponseBinder.parse(json, this);
        }
    }



    public void onFinish() {

    }


    public void success() {

    }


    public void fail() {

    }


    public void unknown() {

    }


    public void notFind() {

    }


    public void typeConvert() {

    }


    public void exist() {

    }


    public void isBlank() {

    }


    public void invalid() {

    }

    @Override
    public void timeout() {

    }

    public void loadObject(Object obj){

    }

    public void loadList(List<Object> list){

    }

    public void loadFail(){

    }


}
