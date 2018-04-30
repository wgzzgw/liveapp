package com.study.hostlive;

import com.tencent.ilivesdk.ILiveConstants;

/**
 * Created by yy on 2018/4/21.
 */
/*
控制状态
 */
public class HostControlState {
    private boolean isBeautyOn = false;//默认美颜关闭
    private boolean isFlashOn = false;//默认闪光灯关闭
    private boolean isVoiceOn = true;//默认音响开启
    private int cameraid = ILiveConstants.FRONT_CAMERA;//默认前置摄像头

    public boolean isBeautyOn() {
        return isBeautyOn;
    }

    public void setBeautyOn(boolean beautyOn) {
        isBeautyOn = beautyOn;
    }

    public boolean isFlashOn() {
        return isFlashOn;
    }

    public void setFlashOn(boolean flashOn) {
        isFlashOn = flashOn;
    }

    public boolean isVoiceOn() {
        return isVoiceOn;
    }

    public void setVoiceOn(boolean voiceOn) {
        isVoiceOn = voiceOn;
    }

    public int getCameraid() {
        return cameraid;
    }

    public void setCameraid(int cameraid) {
        this.cameraid = cameraid;
    }
}
