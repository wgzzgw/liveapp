package com.study.view;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.tencent.TIMUserProfile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yy on 2018/4/29.
 */
/*
观众进入动画view
 */
public class WatcherEnterView extends RelativeLayout {
    private TextView userNameView;//用户名字
    private ImageView splashView;

    private Animation viewAnimation;
    private Animation nameAnimation;
    private Animation splashAnimation;

    private List<TIMUserProfile> watcherProfile = new LinkedList<TIMUserProfile>();//缓存用户进入房间动画
    private boolean isAvaliable = true;
    public WatcherEnterView(Context context) {
        super(context);
        init();
    }
    public WatcherEnterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public WatcherEnterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        setBackgroundResource(R.drawable.watcher_enter_bkg);
        LayoutInflater.from(getContext()).inflate(R.layout.view_watcher_enter, this, true);
        findAllViews();
        //加载动画
        loadAnim();
        setVisibility(INVISIBLE);
    }
    private void findAllViews() {
        userNameView = (TextView) findViewById(R.id.user_name);
        splashView = (ImageView) findViewById(R.id.splash);
    }
    private void loadAnim() {
        viewAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.watcher_enter_view);
        nameAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.watcher_enter_name);
        splashAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.watcher_enter_splash);
        viewAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                WatcherEnterView.this.setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                userNameView.post(new Runnable() {
                    @Override
                    public void run() {
                        userNameView.startAnimation(nameAnimation);
                    }
                });
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        nameAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                userNameView.setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                splashView.post(new Runnable() {
                    @Override
                    public void run() {
                        splashView.startAnimation(splashAnimation);
                    }
                });
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        splashAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                splashView.setVisibility(VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                // 判断有无下一个用户进入。
                isAvaliable = true;
                if (watcherProfile.size() > 0) {
                    showWatcherEnter(watcherProfile.remove(0));
                } else {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WatcherEnterView.this.setVisibility(INVISIBLE);
                        }
                    },2000);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    public void showWatcherEnter(TIMUserProfile userProfile) {
        if (isAvaliable) {
            String name = userProfile.getNickName();
            if (TextUtils.isEmpty(name)) {
                name = userProfile.getIdentifier();
            }

            SpannableStringBuilder nameBuilder = new SpannableStringBuilder("");
            SpannableString nameSpanStr = new SpannableString(name);
            int nameStartIndex = 0;
            int nameEndIndex = name.length();
            //文本颜色:ForegroundColorSpan
            nameSpanStr.setSpan(new ForegroundColorSpan(Color.parseColor("#DD11DD")),
                    nameStartIndex, nameEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            nameBuilder.append(nameSpanStr);

            SpannableString tipSpanStr = new SpannableString("进入房间");
            int tipStartIndex = 0;
            int tipEndIndex = tipSpanStr.length();
            tipSpanStr.setSpan(new ForegroundColorSpan(Color.parseColor("#FFFFFF")),
                    tipStartIndex, tipEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            nameBuilder.append(tipSpanStr);

            userNameView.setText(nameBuilder);
            setVisibility(INVISIBLE);
            userNameView.setVisibility(INVISIBLE);
            splashView.setVisibility(INVISIBLE);
            WatcherEnterView.this.post(new Runnable() {
                @Override
                public void run() {
                    WatcherEnterView.this.startAnimation(viewAnimation);
                }
            });
        } else {
            watcherProfile.add(userProfile);
        }
    }
}
