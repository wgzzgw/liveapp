package com.study.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.interactiveliveapp.R;

/**
 * Created by yy on 2018/4/22.
 */
/*
*  底部操作栏view
* */
public class BottomControlView extends RelativeLayout {
    private ImageView optionView;//主播操作View
    private ImageView giftView;//观众礼物View

    public void setOperateOpen(boolean open) {
        if(!open) {
            optionView.setImageResource(R.drawable.icon_op_open);
        }else {
            optionView.setImageResource(R.drawable.icon_op_close);
        }
    }
    /*
    回调监听器，具体逻辑写在调用方
     */
    public interface OnControlListener {
        //聊天监听
        public void onChatClick();
        //关闭直播监听
        public void onCloseClick();
        //礼物功能监听
        public void onGiftClick();
        //操作礼物监听
        public void onOptionClick(View view);
    }
    private OnControlListener mOnControlListener;

    public void setOnControlListener(OnControlListener l) {
        mOnControlListener = l;
    }
    //点击监听器
    private OnClickListener clickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            //The view that was clicked.
            if (view.getId() == R.id.chat) {
                // 显示聊天操作栏
                if (mOnControlListener != null) {
                    mOnControlListener.onChatClick();
                }
            } else if (view.getId() == R.id.close) {
                // 关闭直播
                if (mOnControlListener != null) {
                    mOnControlListener.onCloseClick();
                }
            } else if (view.getId() == R.id.gift) {
                // 显示礼物选择九宫格
                if (mOnControlListener != null) {
                    mOnControlListener.onGiftClick();
                }
            } else if (view.getId() == R.id.option) {
                if (mOnControlListener != null) {
                    //把主播操作view传出去
                    mOnControlListener.onOptionClick(view);
                    setOperateOpen(true);
                }
            }
        }
    };
    public BottomControlView(Context context) {
        super(context);
        init();
    }
    public BottomControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public BottomControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_bottom_control, this, true);
        findAllViews();
    }
    private void findAllViews() {
        //设置聊天事件点击监听
        findViewById(R.id.chat).setOnClickListener(clickListener);
        //设置关闭直播事件点击监听
        findViewById(R.id.close).setOnClickListener(clickListener);
        giftView = (ImageView) findViewById(R.id.gift);
        giftView.setOnClickListener(clickListener);
        optionView = (ImageView) findViewById(R.id.option);
        optionView.setOnClickListener(clickListener);
    }
    public void setIsHost(boolean isHost) {
        if (isHost) {
            giftView.setVisibility(INVISIBLE);
            optionView.setVisibility(VISIBLE);
        } else {
            optionView.setVisibility(INVISIBLE);
            giftView.setVisibility(VISIBLE);
        }
    }
    }
