package com.study.hostlive;

import com.study.ResponseObject;
import com.study.utils.request.BaseRequest;

import java.io.IOException;

/**
 * Created by yy on 2018/4/27.
 */

public class GetGiftRequest extends BaseRequest {
    /*
     * 获得礼物请求，参数action=getGift,与服务端对应
     * */
    private static final String Action =
            "http://interactiveliveapp.butterfly.mopaasapp.com/userServlet?action=getGift";
    private static final String RequestParamKey_UserId = "userId";//主播ID
    private static final int RequestParamKey_giftExp = -1;//礼物经验值
    /*
 * 内部类GetGiftParam，封装请求参数
 * */
    public static class GetGiftParam {
        public String userId;
        public int giftExp;
        public String toString(){
            return "&userId="+userId+
                    "&exp="+giftExp;
        }
    }
    /*
    * 返回请求url
    * */
    public String getUrl(GetGiftParam param) {
        return Action
                +param.toString();
    }
    /*
   * 重写父类方法，获取礼物异常处理方法
   * */
    @Override
    protected void onFail(IOException e) {
        //调用父类方法发送错误消息，最终会回调request设置的监听器的方法，
        // 监听器设置及具体实现由调用方实现
        sendFailMsg(-100, e.getMessage());
    }

    /*
    * 重写父类方法，请求获取服务器数据失败
    * */
    @Override
    protected void onResponseFail(int code) {
        //调用父类方法发送错误消息，最终会回调request设置的监听器的方法，
        // 监听器设置及具体实现由调用方实现
        sendFailMsg(code, "服务出现异常");
    }
    @Override
    protected void onResponseSuccess(String body) {
        //fromJson是Gson提供的一个方法。用来将一个Json数据转换为对象。
        // 调用方法是：new Gson().fromJson(Json_string,class)
        GetGiftResponseObj responseObject = gson.fromJson(body, GetGiftResponseObj.class);
        if (responseObject == null) {
            sendFailMsg(-101, "数据格式错误");
            return;
        }
        if (responseObject.code.equals(ResponseObject.CODE_SUCCESS)) {
            sendSuccMsg(responseObject.userInfo);
        } else if (responseObject.code.equals(ResponseObject.CODE_FAIL)) {
            sendFailMsg(Integer.valueOf(responseObject.errCode), responseObject.errMsg);
        }
    }
}
