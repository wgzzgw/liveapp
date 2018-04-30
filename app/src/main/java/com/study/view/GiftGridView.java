package com.study.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.model.GiftInfo;
import com.study.utils.ImgUtils;

import java.util.ArrayList;
import java.util.List;

import static com.study.model.GiftInfo.Gift_Empty;

/**
 * Created by yy on 2018/4/24.
 */
/*
礼物九宫格控件
 */
public class GiftGridView  extends GridView {
    private static final String TAG = GiftGridView.class.getSimpleName();
    private GridAdapter gridAdapter;
    private List<GiftInfo> giftInfoList = new ArrayList<GiftInfo>();//数据源
    private GiftInfo selectGiftInfo;//选中礼物

    public GiftGridView(Context context) {
        super(context);
        init();
    }

    public GiftGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GiftGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //Set the number of columns in the grid
    private void init() {
        setNumColumns(4);//4列
        gridAdapter = new GridAdapter();
        //Sets the data behind this GridView.
        setAdapter(gridAdapter);
    }
    //对外提供设置数据源接口
    public void setGiftInfoList(List<GiftInfo> giftInfos) {
        giftInfoList.clear();
        giftInfoList.addAll(giftInfos);
        gridAdapter.notifyDataSetChanged();
    }
    public int getGridViewHeight() {
        //获取高度：adapter item 的高度 * 行数
        View item = gridAdapter.getView(0, null, this);
        item.measure(0, 0);
        int height = item.getMeasuredHeight();
        return height * 2;
    }

    public void setSelectGiftInfo(GiftInfo selectGiftInfo) {
        this.selectGiftInfo = selectGiftInfo;
    }
    public void notifyDataSetChanged() {
        gridAdapter.notifyDataSetChanged();
    }
    private class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return giftInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return giftInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GiftHolder holder = null;
            //对重复加载布局进行了优化——convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_gift_item, parent, false);
                holder = new GiftHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (GiftHolder) convertView.getTag();
            }
            GiftInfo giftInfo = giftInfoList.get(position);
            holder.bindData(giftInfo);
            return convertView;
        }
        //内部类用于对控件实例进行缓存
        private class GiftHolder {
            private View view;//以下控件的父view
            private ImageView giftImg;//礼物图片
            private TextView giftExp;//礼物经验值
            private TextView giftName;//礼物名字
            private ImageView giftSelect;//礼物选择显示
            public GiftHolder(View view) {
                this.view = view;
                giftImg = (ImageView) view.findViewById(R.id.gift_img);
                giftExp = (TextView) view.findViewById(R.id.gift_exp);
                giftName = (TextView) view.findViewById(R.id.gift_name);
                giftSelect = (ImageView) view.findViewById(R.id.gift_select);
            }
            //绑定数据到界面
            public void bindData(final GiftInfo giftInfo) {
                ImgUtils.load(giftInfo.giftResId, giftImg);
                if (giftInfo != Gift_Empty) {
                    //若礼物信息不为空，才进行数据绑定
                    giftExp.setText(giftInfo.expValue + "经验值");
                    giftName.setText(giftInfo.name);
                    if (giftInfo == selectGiftInfo) {
                        giftSelect.setImageResource(R.drawable.gift_selected);
                    } else {
                        if (giftInfo.type == GiftInfo.Type.ContinueGift) {
                            giftSelect.setImageResource(R.drawable.gift_repeat);
                        } else if (giftInfo.type == GiftInfo.Type.FullScreenGift) {
                            giftSelect.setImageResource(R.drawable.gift_none);
                        }
                    }
                } else {
                    giftExp.setText("");
                    giftName.setText("");
                    giftSelect.setImageResource(R.drawable.gift_none);
                }
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (giftInfo == Gift_Empty) {
                            return;
                        }
                        if (giftInfo == selectGiftInfo) {
                            if (mOnGiftItemClickListener != null) {
                                mOnGiftItemClickListener.onClick(null);
                            }
                        } else {
                            if (mOnGiftItemClickListener != null) {
                                mOnGiftItemClickListener.onClick(giftInfo);
                            }
                        }

                    }
                });
            }
            }
        }
    private OnGiftItemClickListener mOnGiftItemClickListener;//礼物选择点击回调事件，具体实现由GiftSelectDialog实现
    public void setOnGiftItemClickListener(OnGiftItemClickListener l) {
        mOnGiftItemClickListener = l;
    }
    public interface OnGiftItemClickListener {
        void onClick(GiftInfo giftInfo);
    }

}

