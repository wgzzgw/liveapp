package com.study.widget;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.example.interactiveliveapp.R;
import com.google.gson.Gson;
import com.study.model.Constants;
import com.study.model.GiftCmdInfo;
import com.study.model.GiftInfo;
import com.study.view.GiftGridView;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yy on 2018/4/23.
 */
/*
礼物选择对话框
 */
public class GiftSelectDialog extends TransParentNoDimDialog {
    private static final String TAG = "gift";
    private ViewPager giftPager;//存放多页礼物
    private GiftPagerAdapter giftAdapter;//viewpager适配器
    private ImageView indicatorOne;//指示器第一页
    private ImageView indicatorTwo;//指示器第二页
    private Button sendBtn;//发送按钮
    private GiftInfo selectGiftInfo = null;//已选中的礼物
    private List<GiftGridView> pageViews = new ArrayList<GiftGridView>();//此处为两个九宫格view
    private static List<GiftInfo> giftInfos = new ArrayList<GiftInfo>();//所有礼物
    private View closeGiftView;//关闭礼物对话框按钮
    private GiftDialogCloseListener giftDialogCloseListener;//礼物关闭监听器，回调处理关闭礼物对话框
    private String repeatId = "";//连发礼物标识符
    private static final int WHAT_UPDATE_TIME = 0;
    private static final int WHAT_MINUTES_TIME = 1;
    private int leftTime = 10;//连发剩余时间10S
    private Handler sendRepeatTimer = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (WHAT_UPDATE_TIME == what) {
                sendBtn.setText("发送(" + leftTime + "s)");
                //0.02秒后发送消息更新计时器
                sendRepeatTimer.sendEmptyMessageDelayed(WHAT_MINUTES_TIME, 200);
            } else if (WHAT_MINUTES_TIME == what) {
                leftTime--;
                if (leftTime > 0) {
                    sendBtn.setText("发送(" + leftTime + "s)");
                    sendRepeatTimer.sendEmptyMessageDelayed(WHAT_MINUTES_TIME, 200);
                } else {
                    //连续发送的周期已经结束
                    sendBtn.setText("发送");
                    repeatId = "";
                }
            }
        }
    };
    //停掉上一次发送计时器
    private void stopTimer() {
        sendRepeatTimer.removeMessages(WHAT_UPDATE_TIME);
        sendRepeatTimer.removeMessages(WHAT_MINUTES_TIME);
        sendBtn.setText("发送");
        leftTime = 10;
    }
    private void restartTimer() {
        stopTimer();
        sendRepeatTimer.sendEmptyMessage(WHAT_UPDATE_TIME);//发送消息，通知更新计时器
    }
    public interface GiftDialogCloseListener {
        public void onClose();//关闭礼物对话框，显示底部操作栏
    }
    public void setGiftDialogCloseListener(GiftDialogCloseListener l){
        giftDialogCloseListener=l;
    }
    static {
        giftInfos.add(GiftInfo.getGiftById(1));
        giftInfos.add(GiftInfo.getGiftById(2));
        giftInfos.add(GiftInfo.getGiftById(3));
        giftInfos.add(GiftInfo.getGiftById(4));
        giftInfos.add(GiftInfo.getGiftById(5));
        giftInfos.add(GiftInfo.getGiftById(6));
        giftInfos.add(GiftInfo.getGiftById(7));
        giftInfos.add(GiftInfo.getGiftById(8));
        giftInfos.add(GiftInfo.getGiftById(9));
    }

    public GiftSelectDialog(Activity activity) {
        super(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_gift_select, null, false);
        setContentView(view);
        setWidthAndHeight(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        findAllViews(view);
    }
    private OnGiftSendListener onGiftSendListener;//礼物发送回调接口，具体实现调用方实现
    public interface OnGiftSendListener {
        void onGiftSendClick(ILVCustomCmd customCmd);
    }
    public void setGiftSendListener(OnGiftSendListener l) {
        onGiftSendListener = l;
    }
    private void findAllViews(View view) {
        giftPager = (ViewPager) view.findViewById(R.id.gift_pager);
        indicatorOne = (ImageView) view.findViewById(R.id.indicator_one);
        indicatorTwo = (ImageView) view.findViewById(R.id.indicator_two);
        sendBtn = (Button) view.findViewById(R.id.send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //礼物的发送事件。selectGiftInfo。
                if (TextUtils.isEmpty(repeatId)) {
                    repeatId = System.currentTimeMillis() + "";
                }
                if (onGiftSendListener != null) {
                    ILVCustomCmd giftCmd = new ILVCustomCmd();
                    giftCmd.setType(ILVText.ILVTextType.eGroupMsg);
                    giftCmd.setCmd(Constants.CMD_CHAT_GIFT);
                    /*giftCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());*/
                    GiftCmdInfo giftCmdInfo = new GiftCmdInfo();
                    giftCmdInfo.giftId = selectGiftInfo.giftId;
                    giftCmdInfo.repeatId = repeatId;
                    giftCmd.setParam(new Gson().toJson(giftCmdInfo));
                    onGiftSendListener.onGiftSendClick(giftCmd);//回调
                    if(selectGiftInfo.type == GiftInfo.Type.ContinueGift) {
                        restartTimer();//重置计时器，超过此时间不可连发
                    }
                }
            }
        });
        giftAdapter = new GiftPagerAdapter();
        giftPager.setAdapter(giftAdapter);
        //Add a listener that will be invoked whenever the page changes or is incrementally
       //scrolled.
       giftPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    indicatorOne.setImageResource(R.drawable.ind_s);
                    indicatorTwo.setImageResource(R.drawable.ind_uns);
                } else if (position == 1) {
                    indicatorOne.setImageResource(R.drawable.ind_uns);
                    indicatorTwo.setImageResource(R.drawable.ind_s);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        indicatorOne.setImageResource(R.drawable.ind_s);//默认亮指示器指向第一页
        indicatorTwo.setImageResource(R.drawable.ind_uns);
        sendBtn.setVisibility(View.INVISIBLE);//隐藏发送按钮，等select礼物触发显示
        closeGiftView=view.findViewById(R.id.giftclose);
        closeGiftView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                giftDialogCloseListener.onClose();//回调关闭处理方法，调用方具体实现
            }
        });
    }

    private class GiftPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;//获得viewpager中有多少个view
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        //instantiateItem(): ①将给定位置的view添加到ViewGroup(容器)中,创建并显示出来
        // ②返回一个代表新增页面的Object(key),通常都是直接返回view本身
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //构建item view
            final GiftGridView itemView = new GiftGridView(activity);
            itemView.setOnGiftItemClickListener(new GiftGridView.OnGiftItemClickListener() {
                @Override
                public void onClick(GiftInfo giftInfo) {
                    stopTimer();
                    repeatId = "";
                    //选择礼物
                    selectGiftInfo = giftInfo;
                    if (selectGiftInfo != null) {
                        sendBtn.setVisibility(View.VISIBLE);
                    } else {
                        sendBtn.setVisibility(View.INVISIBLE);
                    }
                    for (GiftGridView giftGridView : pageViews) {
                        //把两页的选中礼物都更新.因为两个子view的selectGiftInfo已是不同对象
                        giftGridView.setSelectGiftInfo(selectGiftInfo);
                        giftGridView.notifyDataSetChanged();
                        //抛出刷新,防止第一页选中后，跳到第二页继续选中礼物，第一页礼物选中按钮未取消
                    }
                }
            });
            //确定当前页面所展示的gift的list
            int startindex=position * 8;
            int endIndex = (position + 1) * 8;
            int emptyNum = 0;//空白的区域，需要用Gift_Empty填充的个数
            //最后一页的边界处理
            if (endIndex > giftInfos.size()) {
                emptyNum = endIndex - giftInfos.size();
                endIndex = giftInfos.size();
            }
            List<GiftInfo> targetInfos = giftInfos.subList( startindex, endIndex);
            //超出边界的，用空填充。保证每个页面都有item
            for (int i = 0; i < emptyNum; i++) {
                targetInfos.add(GiftInfo.Gift_Empty);
            }
            //设置数据源于itemView
            itemView.setGiftInfoList(targetInfos);
            int gridViewHeight = itemView.getGridViewHeight();//获取itemview wrap高度*2
            container.addView(itemView);//使itemview显示到Viewpager
            pageViews.add(itemView);
            //设置viewpager容器的高度，即为itemView的wrap高度*2
            ViewGroup.LayoutParams layoutParams = container.getLayoutParams();
            layoutParams.height = gridViewHeight;
            container.setLayoutParams(layoutParams);
            return itemView;
        }
        //移除一个给定位置的页面。适配器有责任从容器中删除这个视图。
        // 这是为了确保在finishUpdate(viewGroup)返回时视图能够被移除
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //销毁view
            container.removeView((View) object);
            /*pageViews.remove(position);*/
            pageViews.remove(object);
        }
    }
    //显示dialog
    @Override
    public void show() {
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        /** Push object to the bottom of its container, not changing its size. */
        lp.gravity = Gravity.BOTTOM;
        dialog.getWindow().setAttributes(lp);
        super.show();
    }

}
