package com.study.view;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.model.GiftInfo;
import com.study.utils.ImgUtils;
import com.tencent.TIMUserProfile;

/**
 * Created by yy on 2018/4/26.
 */
/*
保时捷View
 */
public class PorcheView extends LinearLayout {
    private TextView senderName;//赠送者名字
    private ImageView senderAvatar;//赠送者头像
    private TextView giftName;//礼物名字
    private ImageView wheel_b;//车后轱辘
    private ImageView wheel_f;//车前轱辘
    private AnimationDrawable drawb;//车后轱辘动画
    private AnimationDrawable drawf;//车前轱辘动画
    private Animation inAnim;//进场动画
    private Animation outAnim;//退场动画
    private boolean avaliable = false;//可用
    public PorcheView(Context context) {
        super(context);
        init();
    }
    public PorcheView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public PorcheView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_porche, this, true);
        //R.layout.view_porche顶层标签为merge,需设置以下几个属性
        //android:orientation="vertical"
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        findAllViews();
        drawb = (AnimationDrawable) wheel_b.getDrawable();//创建帧动画第三步：取得背景的Drawable对象...
        drawf = (AnimationDrawable) wheel_f.getDrawable();//创建帧动画第三步：取得背景的Drawable对象...
        drawb.setOneShot(false);//设置执行次数，循环显示
        drawf.setOneShot(false);//设置执行次数，循环显示

        setVisibility(INVISIBLE);
        avaliable = true;
    }

    private void findAllViews() {
        senderName = (TextView) findViewById(R.id.sender_name);
        senderAvatar = (ImageView) findViewById(R.id.sender_avatar);
        giftName=(TextView)findViewById(R.id.gift_name);
        wheel_b = (ImageView) findViewById(R.id.wheel_back);
        wheel_f=(ImageView)findViewById(R.id.wheel_front);
    }
    //绑定数据到PorcheView
    private void fillUserInfo(TIMUserProfile userProfile) {
        String avatarUrl = userProfile.getFaceUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            ImgUtils.loadRound(R.drawable.default_avatar, senderAvatar);
        } else {
            ImgUtils.loadRound(avatarUrl, senderAvatar);
        }
        String name = userProfile.getNickName();
        if (TextUtils.isEmpty(name)) {
            name = userProfile.getIdentifier();
        }
        senderName.setText(name);
        giftName.setText("送了一辆"+GiftInfo.Gift_BaoShiJie.name);
    }
    private boolean needShowAnim = false;
    private boolean layouted = false;//保证onlayout执行完，getWidth和getLeft才有值
    //显示保时捷动画
    public void show(TIMUserProfile userProfile) {
        fillUserInfo(userProfile);
        if (layouted) {
            //若onlayout已执行完，启动动画
            startAnim();
        } else {
            needShowAnim = true;
        }
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        layouted = true;
        if (needShowAnim) {
            startAnim();
        }
    }
    private void startAnim() {
        avaliable = false;
        int width = getWidth();//Return the width of the your view.
        int left = getLeft();//Left position of this view relative to its parent.
        //Animation.RELATIVE_TO_SELF：相对于自身
        inAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0 - (width + left) * 1.0f / width, //from X
                Animation.RELATIVE_TO_SELF, 0,//to X
                Animation.RELATIVE_TO_SELF, -1,//fromY
                Animation.RELATIVE_TO_SELF, 0 //to Y
        );
        inAnim.setDuration(2000);//2秒
        inAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            //通知动画开始，保时捷view可见
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
                //转动轱辘，创建帧动画第四步
                drawb.start();
                drawf.start();
            }
            //通知动画结束，进行退场动画
            @Override
            public void onAnimationEnd(Animation animation) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startAnimation(outAnim);
                    }
                }, 3000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        outAnim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, //from X
                Animation.RELATIVE_TO_SELF, (width + left) * 1.0f / width,//to X
                Animation.RELATIVE_TO_SELF, 0,//fromY
                Animation.RELATIVE_TO_SELF, 1 //to Y
        );
        outAnim.setDuration(2000);//2秒
        outAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                //退场结束，保时捷view不可见
                setVisibility(INVISIBLE);
                //帧动画结束
                drawb.stop();
                drawf.stop();
                //还原初始数据
                needShowAnim = false;
                avaliable = true;
                if (onAvaliableListener != null) {
                    //动画结束，回调监听方法，去除缓存数据进行下一次动画展示
                    onAvaliableListener.onAvaliable();
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        post(new Runnable() {
            @Override
            public void run() {
                startAnimation(inAnim);//入点
            }
        });
    }
    //用于缓存监听
    private OnAvaliableListener onAvaliableListener;
    public void setOnAvaliableListener(OnAvaliableListener l) {
        onAvaliableListener = l;
    }
    public interface OnAvaliableListener {
        void onAvaliable();
    }
}
