package com.study.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.study.model.GiftInfo;
import com.tencent.TIMUserProfile;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yy on 2018/4/26.
 */
/*
全屏礼物view容器
 */
public class GiftFullView extends RelativeLayout {
    private PorcheView mPorcheView;//保时捷view
    private boolean isAvaliable = false;//容器是否可用
    //缓存全屏礼物动画显示
    private class GiftUserInfo{
        GiftInfo giftInfo;
        TIMUserProfile userProfile;
    }
    private List<GiftUserInfo> giftUserInfoList = new LinkedList<GiftUserInfo>();
    public GiftFullView(Context context) {
        super(context);
        init();
    }
    public GiftFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GiftFullView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        isAvaliable = true;
    }
    //显示动画
    public void showGift(GiftInfo giftInfo, TIMUserProfile userProfile) {
        if (giftInfo == null || giftInfo.type != GiftInfo.Type.FullScreenGift) {
            return;
        }
        if (isAvaliable) {
            //可用容器
            isAvaliable = false;
            if (giftInfo.giftId == GiftInfo.Gift_BaoShiJie.giftId) {
                showPorcheView(userProfile);//保时捷动画显示
            } else {
                //其他的全屏礼物
            }
        } else {
            //缓存
            GiftUserInfo giftUserInfo = new GiftUserInfo();
            giftUserInfo.giftInfo = giftInfo;
            giftUserInfo.userProfile = userProfile;
            giftUserInfoList.add(giftUserInfo);
        }
    }
        private void showPorcheView(final TIMUserProfile userProfile) {
            if (mPorcheView == null) {
                mPorcheView = new PorcheView(getContext());
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
                //@param child the child view to add
                // @param params the layout parameters to set on the child
                addView(mPorcheView, rlp);
                mPorcheView.setOnAvaliableListener(new PorcheView.OnAvaliableListener() {
                    @Override
                    public void onAvaliable() {
                        isAvaliable = true;
                        int size = giftUserInfoList.size();
                        if(size > 0){
                            GiftUserInfo giftUserInfo= giftUserInfoList.remove(0);
                            GiftInfo giftInfo = giftUserInfo.giftInfo;
                            TIMUserProfile userProfile1 = giftUserInfo.userProfile;
                            showGift(giftInfo,userProfile1);
                        }
                    }
                });
            }
            mPorcheView.show(userProfile);
        }
}
