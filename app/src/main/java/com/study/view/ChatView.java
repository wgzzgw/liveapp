package com.study.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interactiveliveapp.R;
import com.study.model.Constants;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVText;

import java.util.Date;

/**
 * Created by yy on 2018/4/22.
 */
/*
*  聊天操作栏view
* */
public class ChatView  extends LinearLayout {
    private CheckBox mSwitchChatType;//聊天消息类型，分为普通列表聊天和屏幕弹幕聊天
    private EditText mChatContent;//聊天信息
    private TextView mSend;//发送按钮
    private long prelongTim = 0;//定义上一次单击发送按钮的时间
    //发送消息监听器
    /*public interface OnChatSendListener {
        public void onChatSend(String content);
    }*/
    public interface OnChatSendListener {
        /*public void onChatSend(ILVText msg);//发送消息*/
        public void onChatSend(ILVCustomCmd  msg);//自定义消息发送
    }
    private OnChatSendListener mOnChatSendListener;

    public void setOnChatSendListener(OnChatSendListener l) {
        mOnChatSendListener = l;
    }

    public ChatView(Context context) {
        super(context);
        init();
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        /*
        * 设置该view的布局排列方式，内部空间的对齐方式
        * */
        /*
        *  android:gravity="center_vertical"
    android:orientation="horizontal"
        * */
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(Color.parseColor("#ccffffff"));
        //获取手机屏幕密度参数
        int paddingPx = (int) (getResources().getDisplayMetrics().density * 10 + 0.5f);
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        setBackgroundColor(Color.parseColor("#ccffffff"));

        LayoutInflater.from(getContext()).inflate(R.layout.view_chat, this, true);

        findAllViews();
    }
    private void findAllViews() {
        mSwitchChatType = (CheckBox) findViewById(R.id.switch_chat_type);
        mChatContent = (EditText) findViewById(R.id.chat_content_edit);
        mSend = (TextView) findViewById(R.id.chat_send);
        //消息类型开关监听
        mSwitchChatType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mChatContent.setHint("发送弹幕聊天消息");
                } else {
                    mChatContent.setHint("和大家聊点什么吧");
                }
            }
        });
        mSwitchChatType.setChecked(false);//默认消息类型为普通聊天
        mChatContent.setHint("和大家聊点什么吧");
        mSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //发送聊天消息，使用腾讯SDK
                if(prelongTim==0){
                    prelongTim = (new Date()).getTime();
                sendChatMsg();}
                else{
                    long curTime = (new Date()).getTime();//本地单击发送按钮的时间
                    if(curTime-prelongTim<8000){
                        prelongTim = (new Date()).getTime();//记录本次点击发送按钮的时间
                        sendChatMsg();
                    }else{
                        Toast.makeText(getContext(),"你发送的频率过快了！请稍后发送",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void sendChatMsg() {
        if (mOnChatSendListener != null) {
            //1.String content=mChatContent.getText().toString();
           // mOnChatSendListener.onChatSend(content);//设置消息内容

           /* 2.ILVText customCmd=new ILVText();
            customCmd.setText(mChatContent.getText().toString());
            mOnChatSendListener.onChatSend(customCmd);//设置消息内容*/
            //3.
            ILVCustomCmd customCmd = new ILVCustomCmd();
            customCmd.setType(ILVText.ILVTextType.eGroupMsg);//群组消息
            boolean isDanmu = mSwitchChatType.isChecked();
            if (isDanmu) {
                customCmd.setCmd(Constants.CMD_CHAT_MSG_DANMU);//弹幕类型
            } else {
                customCmd.setCmd(Constants.CMD_CHAT_MSG_LIST);//列表类型
            }
            customCmd.setParam(mChatContent.getText().toString());
            mOnChatSendListener.onChatSend(customCmd);//设置消息内容，回调出去
        }
    }
}
