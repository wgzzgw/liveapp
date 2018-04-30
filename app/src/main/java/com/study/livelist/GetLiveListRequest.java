package com.study.livelist;

import com.study.ResponseObject;
import com.study.utils.request.BaseRequest;

import java.io.IOException;

/**
 * Created by yy on 2018/4/21.
 */

public class GetLiveListRequest extends BaseRequest {
    /*
    * 获取直播列表请求，参数action=getList,与服务端对应
    * */
    private static final String HOST = "http://interactiveliveapp.butterfly.mopaasapp.com/roomServlet?action=getList";
    /*
    * 内部类LiveListParam，封装请求参数
    * */
    public static class LiveListParam {
        public int pageIndex;//页下标

        public String toUrlParam() {
            return "&pageIndex=" + pageIndex;
        }
    }
    /*
  * 返回请求url
  * */
    public String getUrl(LiveListParam param) {
        return HOST + param.toUrlParam();
    }
    /*
   * 重写父类方法，获取直播列表异常处理方法
   * */
    @Override
    protected void onFail(IOException e) {
        sendFailMsg(-100,e.toString());
    }
    /*
    * 重写父类方法，请求获取服务器数据失败
    * */
    @Override
    protected void onResponseFail(int code) {
        sendFailMsg(code,"服务器异常");
    }
    @Override
    protected void onResponseSuccess(String body) {
        //fromJson是Gson提供的一个方法。用来将一个Json数据转换为对象。
        // 调用方法是：new Gson().fromJson(Json_string,class)
        LiveListResponseObj liveListresponseObject = gson.fromJson(body, LiveListResponseObj.class);
        if (liveListresponseObject == null) {
            sendFailMsg(-101, "数据格式错误");
            return;
        }
        if (liveListresponseObject.code.equals(ResponseObject.CODE_SUCCESS)) {
            sendSuccMsg(liveListresponseObject.data);
        } else if (liveListresponseObject.code.equals(ResponseObject.CODE_FAIL)) {
            sendFailMsg(Integer.valueOf(liveListresponseObject.errCode), liveListresponseObject.errMsg);
        }
    }
}
