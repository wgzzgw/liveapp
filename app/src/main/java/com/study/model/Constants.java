package com.study.model;

import com.tencent.livesdk.ILVLiveConstants;

/**
 * Created by yy on 2018/4/22.
 */
/*
*常量
* */
public class Constants {
    //自定义发送列表聊天
    public static final int CMD_CHAT_MSG_LIST = ILVLiveConstants.ILVLIVE_CMD_CUSTOM_LOW_LIMIT + 1;//自定义消息段下限
    //自定义发送弹幕聊天
    public static final int CMD_CHAT_MSG_DANMU = ILVLiveConstants.ILVLIVE_CMD_CUSTOM_LOW_LIMIT + 2;//自定义消息段下限
    //自定义发送礼物
    public static final int CMD_CHAT_GIFT = ILVLiveConstants.ILVLIVE_CMD_CUSTOM_LOW_LIMIT + 3;//自定义消息段下限
}
