package com.study.hostlive;



import android.hardware.Camera;

import com.tencent.ilivesdk.core.ILiveLoginManager;

/**
 * Created by yy on 2018/4/28.
 */
public class FlashlightHelper {
    /*
    是否允许闪光灯
     */
    public boolean enableFlashLight(boolean enable) {
        Object obj = ILiveLoginManager.getInstance().getAVConext().getVideoCtrl().getCamera();
        if (obj == null || !(obj instanceof Camera)) {
            return false;
        }
        Camera camera = (Camera) obj;
        //摄像头的参数
        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null) {
            return false;
        }
        if (enable) {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        try {
            camera.setParameters(parameters);
        }catch (Exception e){
            return false;
        }
        return true;
    }
/*
闪光灯是否开启
 */
    public boolean isFlashLightOn() {
        Object obj = ILiveLoginManager.getInstance().getAVConext().getVideoCtrl().getCamera();
        if (obj == null || !(obj instanceof Camera)) {
            return false;
        }

        Camera camera = (Camera) obj;

        Camera.Parameters parameters = camera.getParameters();
        if (parameters == null) {
            return false;
        }

        if (Camera.Parameters.FLASH_MODE_TORCH.equals( parameters.getFlashMode())) {
            return true;
        }
        return false;
    }
}
