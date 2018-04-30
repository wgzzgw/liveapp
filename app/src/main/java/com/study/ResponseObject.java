package com.study;

/**
 * Created by yy on 2018/4/21.
 */
/*
* 接收存放服务端返回数据—基类
* */
public class ResponseObject {
    public static final String CODE_SUCCESS = "1";
    public static final String CODE_FAIL = "0";

    public String code;//存放返回码：成功与否
    public String errCode;//存放具体错误码
    public String errMsg;//存放具体错误信息

}
