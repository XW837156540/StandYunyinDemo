package com.standyunyindemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by admin on 2018/3/2.
 */

public class OnLineUtils implements EventListener {

    private EventManager asr;
    private OnLineCallBack onLineCallBack;

    public OnLineUtils (Context context, OnLineCallBack onLineCallBack){
        asr = EventManagerFactory.create(context,"asr");
        asr.registerListener(this);

        this.onLineCallBack = onLineCallBack;
        initPermission(context);
    }

    /*
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission(Context context) {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(context, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context, toApplyList.toArray(tmpList), 123);
        }

    }


    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        if (params != null && !params.isEmpty()) {

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {

                try {
                    JSONObject jsonObject = new JSONObject(params);
                    String resultType = jsonObject.getString("result_type");
                    if (resultType.equals("final_result")){
                        String finalResult = jsonObject.getString("best_result");

                        onLineCallBack.onSuccess(finalResult);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    public interface OnLineCallBack{
        void onSuccess(String result);
    }

    /**
     * 开始识别
     */
    public void start(){
        Map<String,Object> params = new LinkedHashMap<>();
        String event = SpeechConstant.ASR_START;

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME,false);
        String json = new JSONObject(params).toString();

        asr.send(event,json,null,0,0);
    }

    /**
     * 停止识别
     */
    public void stop() {
        asr.send(SpeechConstant.ASR_STOP,null,null,0,0);
    }
}
