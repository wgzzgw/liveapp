package com.study.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.model.GiftInfo;
import com.study.utils.ImgUtils;
import com.tencent.TIMUserProfile;

/**
 * Created by yy on 2018/4/24.
 */
/*
显示连发礼物动画的view的子itemview
 */
public class GiftRepeatItemView extends LinearLayout {
    private ImageView user_header;//赠送者头像
    private TextView user_name;//赠送者名字
    private TextView gift_name;//礼物名字
    private ImageView gift_img;//礼物图片
    private TextView gift_num;//礼物数量

    private Animation viewInAnim;//view整体出现的动画
    private Animation imgViewInAnim;//头像的动画
    private Animation textScaleAnim;//文字动画

    private int giftId = -1;//礼物ID
    private String userId = "";//赠送者ID
    private String repeatId = "";//记录礼物连发ID，与giftselectdialog计时器有关
    private int totalNum = 0;//赠送礼物总数
    private int leftNum = 0;//缓存还需要显示的礼物数量
    public GiftRepeatItemView(Context context) {
        super(context);
        init();
    }
    public GiftRepeatItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GiftRepeatItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_gift_repeat_item, this, true);
        findAllViews();
        initAnim();
    }

    private void initAnim() {
        /**
         * Loads an {@link Animation} object from a resource
         *
         * @param context Application context used to access resources
         * @param id The resource id of the animation to load
         * @return The animation object reference by the specified id
         * @throws NotFoundException when the animation cannot be loaded
         */
        //创建view动画第二步：获得Animation
        viewInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.repeat_gift_view_in);
        imgViewInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.repeat_gift_img_view_in);
        textScaleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.repeat_gift_num_scale);
        //创建view动画第三步，设置监听器
        viewInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            //通知动画开始
            public void onAnimationStart(Animation animation) {
                setVisibility(VISIBLE);
                //动画开始礼物图片和数量是隐藏的
                gift_img.setVisibility(INVISIBLE);
                gift_num.setVisibility(INVISIBLE);
            }
            //通知动画结束
            @Override
            public void onAnimationEnd(Animation animation) {
                //主线程运行
                post(new Runnable() {
                    @Override
                    public void run() {
                        //在view显示完成之后，再进行img的动画
                        gift_img.startAnimation(imgViewInAnim);
                    }
                });
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        imgViewInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            //通知动画开始
            public void onAnimationStart(Animation animation) {
                gift_img.setVisibility(VISIBLE);
            }
            @Override
            //通知动画结束
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        //在view显示完成之后，再进行img的动画
                        gift_num.startAnimation(textScaleAnim);
                    }
                });
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        textScaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            //通知动画开始
            public void onAnimationStart(Animation animation) {
                gift_num.setText("x" + totalNum);
                gift_num.setVisibility(VISIBLE);
            }
            @Override
            //通知动画结束
            public void onAnimationEnd(Animation animation) {
                //用于判断是否有缓存礼物需要显示，用于处理view还没INVISIBLE时候
                if (leftNum > 0) {
                    //还有礼物需要显示
                    leftNum--;
                    totalNum++;
                    post(new Runnable() {
                        @Override
                        public void run() {
                            gift_num.startAnimation(textScaleAnim);
                        }
                    });
                } else {
                    setVisibility(INVISIBLE);
                    if (listener != null) {
                        giftId=-1;
                        leftNum=0;
                        totalNum=0;
                        //回调监听，去依次顺序执行缓存的礼物动画
                        listener.onAvaliable();
                    }
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
    private void findAllViews() {
        user_header = (ImageView) findViewById(R.id.user_header);
        user_name = (TextView) findViewById(R.id.user_name);
        gift_name = (TextView) findViewById(R.id.gift_name);
        gift_img = (ImageView) findViewById(R.id.gift_img);
        gift_num = (TextView) findViewById(R.id.gift_num);
    }
    //显示礼物动画
    public void showGift(GiftInfo giftInfo,  String repeatId,TIMUserProfile profile) {
        giftId = giftInfo.giftId;//记录礼物的ID，以便复用达到可连发的效果
        userId = profile.getIdentifier();//同上
        this.repeatId = repeatId;
        //若此view不被占用
        if (getVisibility() == INVISIBLE) {
            totalNum = 1;
            //所有动画结束之后
            String avatarUrl = profile.getFaceUrl();
            if (TextUtils.isEmpty(avatarUrl)) {
                ImgUtils.loadRound(R.drawable.default_avatar, user_header);
            } else {
                ImgUtils.loadRound(avatarUrl, user_header);
            }
            String nickName = profile.getNickName();
            if (TextUtils.isEmpty(nickName)) {
                nickName = profile.getIdentifier();
            }
            user_name.setText(nickName);
            gift_name.setText("送出一个" + giftInfo.name);
            ImgUtils.load(giftInfo.giftResId, gift_img);
            gift_num.setText("x" + 1);
            post(new Runnable() {
                @Override
                public void run() {
                    startAnimation(viewInAnim);//开启第一次赠送礼物入场动画
                }
            });
        } else {
            //需要记录下还需要显示多少次礼物。也就是数字的变化。
            //连击情况下，会走此句代码，并且此时GiftRepeatItemView可见
            leftNum++;
        }
    }
    //判断自身可见时，某赠送礼物的显示可用此view，profile为礼物赠送者
    //匹配对不对应
    public boolean isAvaliable(GiftInfo giftInfo,String repeatId, TIMUserProfile profile) {
        boolean sameGift = giftId == giftInfo.giftId;
        boolean sameRepeat = this.repeatId.equals(repeatId);
        boolean sameUser = this.userId.equals(profile.getIdentifier());
        boolean canContinue = giftInfo.type == GiftInfo.Type.ContinueGift;
        return sameGift && sameRepeat&&sameUser && canContinue;

    /*    if(getVisibility()==VISIBLE) {
            if (repeatId.equals(this.repeatId) &&
                    userId.equals(profile.getIdentifier()) &&
                    giftId == giftInfo.giftId) {
                return true;
            }
        }
            return false;
    }*/
    }
    /*
    复用连发监听器
     */
    public interface OnGiftItemAvaliableListener {
        void onAvaliable();
    }
    private OnGiftItemAvaliableListener listener;
    public void setOnGiftItemAvaliableListener(OnGiftItemAvaliableListener l) {
        listener = l;
    }

}
