package com;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.study.editprofile.CustomProfile;
import com.study.request.HeartBeatRequest;
import com.study.utils.QnUploadHelper;
import com.tencent.TIMFriendshipSettings;
import com.tencent.TIMManager;
import com.tencent.TIMUserProfile;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yy on 2018/4/17.
 */

public class MyApplication extends Application {
    private ILVLiveConfig mLiveConfig;//直播配置类
    private static MyApplication app;
    private static Context appContext;
    private TIMUserProfile mSelfProfile;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        appContext = getApplicationContext();

        //初始化腾讯直播SDK，参数二：腾讯云对应SdkAppId，参数三：腾讯云账号集成体系accountType
        ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400083930, 25371);
        //用户信息字段的配置
        List<String> customInfos = new ArrayList<String>();
        customInfos.add(CustomProfile.CUSTOM_RENZHENG);
        customInfos.add(CustomProfile.CUSTOM_LEVEL);
        customInfos.add(CustomProfile.CUSTOM_GET);
        customInfos.add(CustomProfile.CUSTOM_SEND);
        TIMManager.getInstance().initFriendshipSettings(CustomProfile.allBaseInfo, customInfos);
        //初始化直播场景
        mLiveConfig = new ILVLiveConfig();
        ILVLiveManager.getInstance().init(mLiveConfig);
        //初始化七牛云
        QnUploadHelper.init("sS8WL_Q-GoCmGK2W9N74pNiqE-dJxIVEHG45WQCy",
                "1Vz-rd3j78oDvNlniTVoEcAveAB2aCSxu3uwWLER",
                "http://p7h60wv6m.bkt.clouddn.com/",
                "zhibotupian"
                );
        LeakCanary.install(this);//内存泄漏检测工具安装
    }

    public static MyApplication getApplication() {
        return app;
    }
    public static Context getContext() {
        return appContext;
    }
    //设置用户信息，TIMUserProfile为sdk中给定的信息类
    public void setSelfProfile(TIMUserProfile userProfile) {
        mSelfProfile = userProfile;
    }
    //获取用户信息
    public TIMUserProfile getSelfProfile() {
        return mSelfProfile;
    }
    public ILVLiveConfig getLiveConfig() {
        return mLiveConfig;
    }
    private Timer heartBeatTimer;//心跳定时器
    //应用异常退出时会执行此方法
    public void onTerminate(){
        stopHeartBeat();
        super.onTerminate();}
    public void startHeartBeat(final int mRoomId){
        heartBeatTimer=new Timer();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
             HeartBeatRequest mHeartBeatRequest = null;//心跳请求
                        //发送心跳包
                        if (mHeartBeatRequest == null) {
                            mHeartBeatRequest = new HeartBeatRequest();
                        }
                        String roomId = mRoomId + "";
                        String userId = getSelfProfile().getIdentifier();
                        String url = mHeartBeatRequest.getUrl(roomId, userId);
                        mHeartBeatRequest.request(url);
                    }
        };
        heartBeatTimer.scheduleAtFixedRate(task, 0, 5000); //5秒钟 。服务器是10秒钟去检测一次。
    }
    public void stopHeartBeat() {
        heartBeatTimer.cancel();
    }
}
