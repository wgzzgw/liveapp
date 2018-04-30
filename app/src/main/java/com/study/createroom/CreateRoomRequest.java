package com.study.createroom;

import com.study.ResponseObject;
import com.study.utils.request.BaseRequest;

import java.io.IOException;

/**
 * Created by yy on 2018/4/21.
 */

public class CreateRoomRequest extends BaseRequest {
    /*
    * 创建直播房间请求，参数action=create,与服务端对应
    * */
    private static final String Action =
            "http://interactiveliveapp.butterfly.mopaasapp.com/roomServlet?action=create";
    private static final String RequestParamKey_UserId = "userId";//主播ID
    private static final String RequestParamKey_UserAvatar = "userAvatar";//主播头像
    private static final String RequestParamKey_UserName = "userName";//主播昵称
    private static final String RequestParamKey_LiveTitle = "liveTitle";//直播标题
    private static final String RequestParamKey_LiveCover = "liveCover";//直播封面
    /*
    * 内部类CreateRoomParam，封装请求参数
    * */
    public static class CreateRoomParam {
        public String userId;
        public String userAvatar;
        public String userName;
        public String liveTitle;
        public String liveCover;
    }
    /*
    * 返回请求url
    * */
    public String getUrl(CreateRoomParam param) {
        return Action
                + "&" + RequestParamKey_UserId + "=" + param.userId
                + "&" + RequestParamKey_UserAvatar + "=" + param.userAvatar
                + "&" + RequestParamKey_UserName + "=" + param.userName
                + "&" + RequestParamKey_LiveTitle + "=" + param.liveTitle
                + "&" + RequestParamKey_LiveCover + "=" + param.liveCover
                ;
    }
    /*
    * 重写父类方法，创建房间异常处理方法
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
        RoomInfoResponseObj responseObject = gson.fromJson(body, RoomInfoResponseObj.class);
        if (responseObject == null) {
            sendFailMsg(-101, "数据格式错误");
            return;
        }
        if (responseObject.code.equals(ResponseObject.CODE_SUCCESS)) {
            sendSuccMsg(responseObject.data);
        } else if (responseObject.code.equals(ResponseObject.CODE_FAIL)) {
            sendFailMsg(Integer.valueOf(responseObject.errCode), responseObject.errMsg);
        }
    }

}
