package com.study.editprofile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;

/**
 * Created by yy on 2018/4/17.
 */

public class ProfileEdit extends LinearLayout {
    private ImageView mIconView;//信息栏图标
    private TextView mKeyView;//信息栏标识
    private TextView mValueView;//信息栏信息值
    private ImageView mRightArrowView;//信息栏箭头

    public ProfileEdit(Context context) {
        super(context);
        init();
    }

    public ProfileEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProfileEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_profile_edit, this
                , true);
        findAllViews();
    }
    //获取控件实例
    private void findAllViews() {
        mIconView = (ImageView) findViewById(R.id.profile_icon);
        mKeyView = (TextView) findViewById(R.id.profile_key);
        mValueView = (TextView) findViewById(R.id.profile_value);
        mRightArrowView = (ImageView) findViewById(R.id.right_arrow);
    }
    //设置信息栏数据
    public void set(int iconResId, String key, String value) {
        mIconView.setImageResource(iconResId);
        mKeyView.setText(key);
        mValueView.setText(value);
    }
    //更新信息栏信息值
    public void updateValue(String value) {
        mValueView.setText(value);
    }
    //获取信息栏信息值
    public String getValue() {
        return mValueView.getText().toString();
    }
    //隐藏箭头，变成只读信息，不可编辑
    protected void disableEdit() {
        mRightArrowView.setVisibility(GONE);
    }

}
