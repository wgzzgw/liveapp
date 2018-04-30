package com.study.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.interactiveliveapp.R;
import com.study.model.GiftInfo;
import com.tencent.TIMUserProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yy on 2018/4/24.
 */
/*
显示连发礼物动画的view容器
 */
public class GiftRepeatView extends LinearLayout {
    private GiftRepeatItemView item0, item1;//存放两个礼物连发显示view
    //缓存连发礼物动画显示
    private class GiftSenderAndInfo {
        public GiftInfo giftInfo;
        public String repeatId;
        public TIMUserProfile senderProfile;
    }
    private List<GiftSenderAndInfo> giftSenderAndInfoList = new LinkedList<GiftSenderAndInfo>();
    public GiftRepeatView(Context context) {
        super(context);
        init();
    }
    public GiftRepeatView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public GiftRepeatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_gift_repeat, this, true);
        findAllViews();
        /*
        *  android:gravity="bottom"
    android:orientation="vertical"
        * */
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.BOTTOM);
        item0.setVisibility(INVISIBLE);
        item1.setVisibility(INVISIBLE);
    }
    private void findAllViews() {
        item0 = (GiftRepeatItemView) findViewById(R.id.item0);
        item1 = (GiftRepeatItemView) findViewById(R.id.item1);
        item0.setOnGiftItemAvaliableListener(avaliableListener);
        item1.setOnGiftItemAvaliableListener(avaliableListener);
    }

    private GiftRepeatItemView.OnGiftItemAvaliableListener avaliableListener = new GiftRepeatItemView.OnGiftItemAvaliableListener() {
        @Override
        public void onAvaliable() {
            if (giftSenderAndInfoList.size() > 0) {
                GiftSenderAndInfo firstinfo = giftSenderAndInfoList.remove(0);
                showGift(firstinfo.giftInfo, firstinfo.repeatId, firstinfo.senderProfile);
                //找出缓存中和第一个礼物相同的连发礼物
                List<GiftSenderAndInfo> leftSameInfos = new ArrayList<GiftSenderAndInfo>();
                for (GiftSenderAndInfo info : giftSenderAndInfoList) {
                    if (info.senderProfile.getIdentifier().equals(firstinfo.senderProfile.getIdentifier())
                            &&info.repeatId.equals(firstinfo.repeatId)
                            &&info.giftInfo.giftId==firstinfo.giftInfo.giftId){
                        //三者同时满足，说明是同一个连发礼物
                        leftSameInfos.add(info);
                    }
                }
                giftSenderAndInfoList.removeAll(leftSameInfos);
                for(GiftSenderAndInfo sameInfo:leftSameInfos){
                    showGift(sameInfo.giftInfo, sameInfo.repeatId, sameInfo.senderProfile);
                }
            }
        }
    };
    /*
    显示动画
     */
    public void showGift(GiftInfo giftInfo,  String repeatId, TIMUserProfile profile) {
        GiftRepeatItemView avaliableView = getAvaliableView(giftInfo, repeatId,profile);
        if (avaliableView == null) {
            //无可用的view，缓存
            GiftSenderAndInfo info = new GiftSenderAndInfo();
            info.giftInfo = giftInfo;
            info.senderProfile = profile;
            info.repeatId = repeatId;
            giftSenderAndInfoList.add(info);
        } else {
            avaliableView.showGift(giftInfo, repeatId,profile);
        }
    }

    //检测是否有可用的GiftRepeatItemView
    private GiftRepeatItemView getAvaliableView(GiftInfo giftInfo, String repeatId,TIMUserProfile profile) {
        if (item0.isAvaliable(giftInfo,repeatId, profile)) {
            return item0;
        }
        if (item1.isAvaliable(giftInfo,repeatId, profile)) {
            return item1;
        }
        if (item0.getVisibility() == INVISIBLE) {
            return item0;
        }
        if (item1.getVisibility() == INVISIBLE) {
            return item1;
        }
        return null;
    }
}
