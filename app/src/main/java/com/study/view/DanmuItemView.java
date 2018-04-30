package com.study.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.model.ChatMsgInfo;
import com.study.utils.ImgUtils;

/**
 * Created by yy on 2018/4/23.
 */
/*
* 弹幕子view
* */
public class DanmuItemView extends RelativeLayout {
    private static final String TAG = DanmuItemView.class.getSimpleName();


    private ImageView mSenderAvatar;//发送者的头像
    private TextView mSenderName;//发送者名字
    private TextView mChatContent;//发送者的内容
    private TranslateAnimation translateAnim = null;//创建view动画第二步：获得TranslateAnimation
    private OnAvaliableListener onAvaliableListener;//弹幕view状态变化监听器
    public DanmuItemView(Context context) {
        super(context);
        init();
    }

    public DanmuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_danmu_item, this, true);
        findAllViews();
        //android:gravity="center_vertical"
        setGravity(Gravity.CENTER_VERTICAL);
        setVisibility(INVISIBLE);
        //创建view动画第三步
        //创建动画，水平位移
        translateAnim = (TranslateAnimation) AnimationUtils.loadAnimation(getContext(), R.anim.danmu_item_enter);
        translateAnim.setAnimationListener(animatorListener);

    }
    private Animation.AnimationListener animatorListener = new Animation.AnimationListener() {
        @Override
        //通知动画开始
        public void onAnimationStart(Animation animator) {
            Log.d(TAG,DanmuItemView.this + " onAnimationStart VISIBLE");
            setVisibility(VISIBLE);
        }
        //通知动画结束
        @Override
        public void onAnimationEnd(Animation animator) {
            Log.d(TAG,DanmuItemView.this + " onAnimationEnd INVISIBLE");
            setVisibility(INVISIBLE);
            if (onAvaliableListener != null) {
                onAvaliableListener.onAvaliable();
            }
        }
        @Override
        public void onAnimationRepeat(Animation animator) {
        }
    };
    private void findAllViews() {
        mSenderAvatar = (ImageView) findViewById(R.id.user_avatar);
        mSenderName = (TextView) findViewById(R.id.user_name);
        mChatContent = (TextView) findViewById(R.id.chat_content);
    }
    /*
    弹幕子view显示
     */
    public void showMsgInfo(ChatMsgInfo danmuInfo) {
        String avatar = danmuInfo.getAvatar();
        if (TextUtils.isEmpty(avatar)) {
            ImgUtils.loadRound(R.drawable.default_avatar, mSenderAvatar);
        } else {
            ImgUtils.loadRound(avatar, mSenderAvatar);
        }
        mSenderName.setText(danmuInfo.getSenderName());
        mChatContent.setText(danmuInfo.getContent());
        //以上代码把弹幕消息绑定到了弹幕子view

        //启动动画
        //在动画监听里面做处理，调用post保证在动画结束之后再start
        //解决start之后直接end的情况。
        //view的post方法，运行在UI线程中，也就是主线程中。
        post(new Runnable() {
            @Override
            public void run() {
                //创建view动画第四步
                DanmuItemView.this.startAnimation(translateAnim);
            }
        });
    }
    /*
    是否可用——true表明此view不可见，可复用
     */
public boolean isAvaliable(){
    return getVisibility()!=VISIBLE;
}


    public void setOnAvaliableListener(OnAvaliableListener l) {
        onAvaliableListener = l;
    }

    public interface OnAvaliableListener {
        public void onAvaliable();//具体内容调用方写
    }
}
