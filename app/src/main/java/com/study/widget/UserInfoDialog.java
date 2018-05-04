package com.study.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.editprofile.CustomProfile;
import com.study.utils.ImgUtils;
import com.tencent.TIMUserProfile;

import java.util.Map;

/**
 * Created by yy on 2018/4/29.
 */
/*
用户详细信息对话框
 */
public class UserInfoDialog  extends TransParentDialog {
    private TIMUserProfile userInfo;//用户信息封装类

    private ImageView user_close;//关闭对话框
    private ImageView user_avatar;//用户头像
    private TextView user_name;//用户名字
    private ImageView user_gender;//用户性别
    private TextView user_level;//用户等级
    private TextView user_id;//用户ID
    private TextView user_renzhen;//用户认证
    private TextView user_sign;//用户签名
    private TextView user_songchu;//用户送出票数
    private TextView user_bopiao;//用户获得票数

    public UserInfoDialog(Activity activity, TIMUserProfile userInfo) {
        super(activity);
        this.userInfo = userInfo;

        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_userinfo, null);
        setContentView(view);

        findAllViews(view);
        bindDataToViews();
        //调用父类方法设置dialog宽为屏幕的80%，高为WRAP_CONTENT
        setWidthAndHeight(activity.getWindow().getDecorView().getWidth() * 90 / 100, WindowManager.LayoutParams.WRAP_CONTENT);
    }
    private void findAllViews(View view) {
        user_close = (ImageView) view.findViewById(R.id.user_close);
        user_avatar = (ImageView) view.findViewById(R.id.user_avatar);
        user_name = (TextView) view.findViewById(R.id.user_name);
        user_gender = (ImageView) view.findViewById(R.id.user_gender);
        user_level = (TextView) view.findViewById(R.id.user_level);
        user_id = (TextView) view.findViewById(R.id.user_id);
        user_renzhen = (TextView) view.findViewById(R.id.user_renzhen);
        user_sign = (TextView) view.findViewById(R.id.user_sign);
        user_songchu = (TextView) view.findViewById(R.id.user_songchu);
        user_bopiao = (TextView) view.findViewById(R.id.user_bopiao);

        user_close.setOnClickListener(clickListener);
        user_avatar.setOnClickListener(clickListener);
    }
    //绑定数据到view
    private void bindDataToViews() {

        String avatarUrl = userInfo.getFaceUrl();
        if (TextUtils.isEmpty(avatarUrl)) {
            ImgUtils.loadRound(R.drawable.default_avatar, user_avatar);
        } else {
            ImgUtils.loadRound(avatarUrl, user_avatar);
        }

        String nickName = userInfo.getNickName();
        if(TextUtils.isEmpty(nickName)){
            nickName = "用户";
        }
        user_name.setText(nickName);

        long genderValue = userInfo.getGender().getValue();
        user_gender.setImageResource(genderValue == 1 ? R.drawable.ic_male : R.drawable.ic_female);

        user_id.setText("ID：" + userInfo.getIdentifier());

        String sign = userInfo.getSelfSignature();
        user_sign.setText(TextUtils.isEmpty(sign) ? "Ta好像忘记写签名了..." : sign);

        Map<String, byte[]> customInfo = userInfo.getCustomInfo();//获取自定义字段map集合

        String rezhen = getValue(customInfo, CustomProfile.CUSTOM_RENZHENG, "未知");
        user_renzhen.setText(rezhen);
        int sendNum = Integer.valueOf(getValue(customInfo, CustomProfile.CUSTOM_SEND, "0"));
        user_songchu.setText("送出：" + formatLargNum(sendNum));
        int getNum = Integer.valueOf(getValue(customInfo,CustomProfile.CUSTOM_GET,"0"));
        user_bopiao.setText("播票：" + formatLargNum(getNum));
        String level = getValue(customInfo, CustomProfile.CUSTOM_LEVEL, "0");
        user_level.setText(level);
    }
    /*
    根据key获取值，不存在则返回默认值
     */
    private String getValue(Map<String, byte[]> customInfo, String key, String defaultValue) {
        if (customInfo != null) {
            byte[] valueBytes = customInfo.get(key);
            if (valueBytes != null) {
                return new String(valueBytes);
            }
        }
        return defaultValue;
    }
    private String formatLargNum(int num) {
        float wan = num * 1.0f / 10000;
        if (wan < 1) {
            return "" + num;
        } else {
            return new java.text.DecimalFormat("#.00").format(wan) + "万";
        }
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == user_close) {
                //关闭对话框
                hideDialog();
            }
            if(v==user_avatar){
                //跳转到主播作品展示页面，主要录制视频
            }
        }
    };
    private void hideDialog() {
        hide();
    }
}
