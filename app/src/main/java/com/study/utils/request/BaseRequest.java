package com.study.utils.request;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yy on 2018/4/21.
 */
/*
* 请求基类
* */
public abstract  class BaseRequest {
    //创建一个OKhttpClient实例
    private final static OkHttpClient okClient = new OkHttpClient();
    //监听器，监听请求成功与失败
    private OnResultListener listener;

    protected final static Gson gson = new Gson();

    protected final int WHAT_FAIL = 0;//失败码
    protected final int WHAT_SUCC = 1;//成功码


    private Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == WHAT_FAIL) {
                if (listener != null) {
                    //调用方实现具体逻辑
                    listener.onFail(msg.arg1, (String) msg.obj);
                }
            } else if (what == WHAT_SUCC) {
                if (listener != null) {
                    //调用方实现具体逻辑
                    listener.onSuccess(msg.obj);
                }
            }
        }
    };
    public interface OnResultListener<T> {
        void onFail(int code, String msg);
        void onSuccess(T object);
    }
    /*
    * 发起请求，参数为请求目标url,方法为get
    * */
    public void request(String url) {
        //发起一条http请求，步骤1.创建一个request对象
        final Request request = new Request.Builder()
                .url(url)
                .build();
        //步骤2.调用OkHttpClient的newCall创建一个Call对象，并
        //调用它的enqueue方法来发送请求并获取服务器返回的数据
        //ps:enqueue与execute区别在于enqueue内部已开好了线程
        okClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                /*
                * 异常处理
                * */
                onFail(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*
                * 对响应信息做判断
                * */
                if (response.isSuccessful()) {
                    /*
                    * 请求获取服务器数据成功
                    * */
                    onResponseSuccess(response.body().string());
                } else {
                    /*
                    请求获取服务器数据失败
                     */
                    onResponseFail(response.code());
                }
            }
        });
    }
    public void setOnResultListener(OnResultListener l) {
        listener = l;
    }
    protected abstract void onFail(IOException e);
    protected abstract void onResponseFail(int code);
    protected abstract void onResponseSuccess(String body);
    /*
    发送失败消息
     */
    protected void sendFailMsg(int code, String reason) {
        Message msg = uiHandler.obtainMessage(WHAT_FAIL);
        msg.arg1 = code;
        msg.obj = reason;
        uiHandler.sendMessage(msg);
    }
    /*
    发送成功消息
     */
    protected <T> void sendSuccMsg(T data) {
        Message msg = uiHandler.obtainMessage(WHAT_SUCC);
        msg.obj = data;
        uiHandler.sendMessage(msg);
    }

}
