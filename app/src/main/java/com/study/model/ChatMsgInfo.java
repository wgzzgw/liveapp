package com.study.model;

/**
 * Created by yy on 2018/4/22.
 */
/*
* 聊天列表——信息封装类
* */
public class ChatMsgInfo {
    public static final int MSGTYPE_LIST = 0;//聊天类型——列表
    public static final int MSGTYPE_DANMU = 1;//聊天类型——弹幕

    private int msgType = MSGTYPE_LIST;//默认聊天类型为列表

    private String text;//聊天的内容
    private String senderId;//发送者的id-区分唯一用户
    private String avatar;//发送者的头像
    private String senderName;//发送者名字

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getContent() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getAvatar() {
        return avatar;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    //创建列表消息
    public static ChatMsgInfo createListInfo(String text, String userId, String avatar) {
        ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
        chatMsgInfo.msgType = MSGTYPE_LIST;
        chatMsgInfo.text = text;
        chatMsgInfo.senderId = userId;
        chatMsgInfo.avatar = avatar;
        chatMsgInfo.senderName = "";

        return chatMsgInfo;
    }
    //创建弹幕消息
    public static ChatMsgInfo createDanmuInfo(String text, String userId, String avatar, String name) {
        ChatMsgInfo chatMsgInfo = new ChatMsgInfo();
        chatMsgInfo.msgType = MSGTYPE_LIST;
        chatMsgInfo.text = text;
        chatMsgInfo.senderId = userId;
        chatMsgInfo.avatar = avatar;
        chatMsgInfo.senderName = name;

        return chatMsgInfo;
    }

}
