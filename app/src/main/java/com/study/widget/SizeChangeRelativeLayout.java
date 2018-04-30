package com.study.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by yy on 2018/4/22.
 */
/*
* 键盘监听layout
* */
public class SizeChangeRelativeLayout extends RelativeLayout {
    private OnSizeChangeListener mOnSizeChangeListener;

    public void setOnSizeChangeListener(OnSizeChangeListener l) {
        mOnSizeChangeListener = l;
    }
    /*
    键盘监听回调接口
     */
    public interface OnSizeChangeListener {
        public void onLarge();//隐藏键盘
        public void onSmall();//显示键盘
    }
    public SizeChangeRelativeLayout(Context context) {
        super(context);
    }
    public SizeChangeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public SizeChangeRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    通过使用该view作为顶层view,当大小发生了变化，说明底部操作栏点击了聊天消息栏，键盘开始活动
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangeListener == null) {
            return;
        }
        if (h > oldh) {
            //画面变长，键盘隐藏
            mOnSizeChangeListener.onLarge();
        } else {
            //画面变短，键盘显示
            mOnSizeChangeListener.onSmall();
        }
    }

}
