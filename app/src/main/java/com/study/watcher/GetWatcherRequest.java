package com.study.watcher;

import com.study.ResponseObject;
import com.study.utils.request.BaseRequest;

import java.io.IOException;

/**
 * Created by yy on 2018/4/30.
 */
/*
获取观众列表请求类，主要是取出已经存在此房间的观众
 */
public class GetWatcherRequest extends BaseRequest {
    /*
* 观众列表请求，参数action=getWatcher,与服务端对应
* */
    private static final String HOST = "http://interactiveliveapp.butterfly.mopaasapp.com/roomServlet?action=getWatcher";

    public String getUrl(int roomId) {
        return HOST + "&=roomId" + roomId;
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
        WatcherResponseObj watcherResponseObj = gson.fromJson(body, WatcherResponseObj.class);
        if (watcherResponseObj == null) {
            sendFailMsg(-101, "数据格式错误");
            return;
        }
        if (watcherResponseObj.code.equals(ResponseObject.CODE_SUCCESS)) {
            sendSuccMsg(watcherResponseObj.data);
        } else if (watcherResponseObj.code.equals(ResponseObject.CODE_FAIL)) {
            sendFailMsg(Integer.valueOf(watcherResponseObj.errCode), watcherResponseObj.errMsg);
        }
    }
}
