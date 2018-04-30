package com.study.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.interactiveliveapp.R;
import com.study.model.ChatMsgInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yy on 2018/4/23.
 */
/*
弹幕容器，存放四个弹幕子view(最多)
 */
public class DanmuView  extends LinearLayout {
    private static final String TAG = DanmuView.class.getSimpleName();

    private DanmuItemView item0, item1, item2, item3;//弹幕子View
    private List<ChatMsgInfo> msgInfoList = new LinkedList<ChatMsgInfo>();//缓存没有可用弹幕子view时数据的存放
    //LinkedList保证顺序
    private  DanmuItemView.OnAvaliableListener avaliableListener = new DanmuItemView.OnAvaliableListener() {
        @Override
        public void onAvaliable() {
            //有可用的itemview
            //从msgList中获取之前缓存下来的消息，然后显示出来。
            ChatMsgInfo chatMsgInfo=null;
            synchronized (this){
                chatMsgInfo = msgInfoList.remove(0);
            }
            if(msgInfoList.size() > 0) {
                //解决同步问题，synchronized
                Log.d(TAG,"显示缓存消息");
                addMsgInfo(chatMsgInfo);
            }
        }
    };

    public DanmuView(Context context) {
        super(context);
        init();
    }

    public DanmuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DanmuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_danmu, this, true);
        findAllViews();
        //android:orientation="vertical"
        setOrientation(LinearLayout.VERTICAL);
    }
    private void findAllViews() {
        item0 = (DanmuItemView) findViewById(R.id.danmu0);
        item1 = (DanmuItemView) findViewById(R.id.danmu1);
        item2 = (DanmuItemView) findViewById(R.id.danmu2);
        item3 = (DanmuItemView) findViewById(R.id.danmu3);
       /* //隐藏
        item0.setVisibility(INVISIBLE);
        item1.setVisibility(INVISIBLE);
        item2.setVisibility(INVISIBLE);
        item3.setVisibility(INVISIBLE);
*/
    }


    public void addMsgInfo(ChatMsgInfo danmuInfo) {
        synchronized (this){//解决pull和add冲突同步问题，synchronized
            DanmuItemView avaliableItemView = getAvaliableItemView();
            if (avaliableItemView == null) {
                //说明没有可用的itemView，缓存弹幕消息，等待有可用的item的时候，去显示
                msgInfoList.add(danmuInfo);
                }
             else {
                //说明有可用的itemView
                avaliableItemView.showMsgInfo(danmuInfo);
            }}
    }
    //复用弹幕view
    private DanmuItemView getAvaliableItemView() {
        //获取可用的item view
        if (item0.isAvaliable()) {
            return item0;
        }
        if (item1.isAvaliable()) {
            return item1;
        }
        if (item2.isAvaliable()) {
            return item2;
        }
        if (item3.isAvaliable()) {
            return item3;
        }
        return null;
    }
}
