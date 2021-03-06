package com.study.request;

import com.study.utils.request.BaseRequest;

import java.io.IOException;

/**
 * Created by yy on 2018/4/29.
 */
/*
心跳包请求类，主要是检测观众和主播是否异常退出
 */
public class HeartBeatRequest  extends BaseRequest {
    /*
  * 心跳包请求，参数action=heartBeat,与服务端对应
  * */
    private static final String Action = "http://interactiveliveapp.butterfly.mopaasapp.com/roomServlet?action=heartBeat";

    private static final String RequestParamKey_RoomId = "roomId";
    private static final String RequestParamKey_UserId = "userId";

    public String getUrl(int roomId, String userId) {
        return Action
                + "&" + RequestParamKey_RoomId + "=" + roomId
                + "&" + RequestParamKey_UserId + "=" + userId
                ;
    }
    @Override
    protected void onFail(IOException e) {
        //调用父类方法发送错误消息，最终会回调request设置的监听器的方法，
        // 监听器设置及具体实现由调用方实现
        sendFailMsg(-100, e.getMessage());
    }
    @Override
    protected void onResponseFail(int code) {
        //调用父类方法发送错误消息，最终会回调request设置的监听器的方法，
        // 监听器设置及具体实现由调用方实现
        sendFailMsg(code, "服务出现异常");
    }
    @Override
    protected void onResponseSuccess(String body) {
        //无需返回数据到客户端
    }
}
