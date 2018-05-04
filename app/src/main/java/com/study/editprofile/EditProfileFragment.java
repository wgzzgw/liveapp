package com.study.editprofile;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.MyApplication;
import com.example.interactiveliveapp.R;
import com.study.main.MainActivity;
import com.study.utils.ImgUtils;
import com.study.utils.PicChooserHelper;
import com.tencent.TIMCallBack;
import com.tencent.TIMFriendGenderType;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class EditProfileFragment extends Fragment {

    private Toolbar mTitlebar;
    private ProfileEdit mNickNameEdt;//名称一栏
    private ProfileEdit mGenderEdt;//性别一栏
    private ProfileEdit mSignEdt;//签名一栏
    private ProfileEdit mRenzhengEdt;//认证一栏
    private ProfileEdit mLocationEdt;//地区一栏
    private ProfileTextView mIdView;//ID一栏
    private ProfileTextView mLevelView;//等级一栏
    private ProfileTextView mGetNumsView;//获得票数一栏
    private ProfileTextView mSendNumsView;//送出票数一栏
    private Button mCompleteBtn;//完成编辑按钮
    private TIMUserProfile mUserProfile;//用户信息
    private ImageView mAvatarImg;//用户头像
    private View mAvatarView;//头像view
    private PicChooserHelper mPicChooserHelper;//图片选择工具类
    public EditProfileFragment() {
        // Required empty public constructor
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        findAllViews(mainView);
        setListeners();
        setTitleBar();
        setIconKey();//设置字段和icon
        getSelfInfo();
        //修改第一次登录标志为false
        SharedPreferences sp=getActivity().getSharedPreferences("FirstLogin",MODE_PRIVATE);
        SharedPreferences.Editor se=sp.edit();
        se.putBoolean("firstLogin",false);
        se.commit();
        return mainView;
    }

    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), "获取信息失败:" + i+s, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                mUserProfile = timUserProfile;
                updateViews(timUserProfile);
            }
        });

    }
    private void findAllViews(View view) {
        mTitlebar = (Toolbar) view.findViewById(R.id.title_bar);
        mNickNameEdt = (ProfileEdit) view.findViewById(R.id.nick_name);
        mGenderEdt = (ProfileEdit) view.findViewById(R.id.gender);
        mSignEdt = (ProfileEdit) view.findViewById(R.id.sign);
        mRenzhengEdt = (ProfileEdit) view.findViewById(R.id.renzheng);
        mLocationEdt = (ProfileEdit) view.findViewById(R.id.location);
        mIdView = (ProfileTextView) view.findViewById(R.id.id);
        mLevelView = (ProfileTextView) view.findViewById(R.id.level);
        mGetNumsView = (ProfileTextView) view.findViewById(R.id.get_nums);
        mSendNumsView = (ProfileTextView) view.findViewById(R.id.send_nums);
        mCompleteBtn = (Button) view.findViewById(R.id.complete);
        mAvatarView = view.findViewById(R.id.avatar);
        mAvatarImg = (ImageView) view.findViewById(R.id.avatar_img);
    }
    private void setIconKey() {
        mNickNameEdt.set(R.drawable.ic_info_nickname, "昵称", "");
        mGenderEdt.set(R.drawable.ic_info_gender, "性别", "");
        mSignEdt.set(R.drawable.ic_info_sign, "签名", "无");
        mRenzhengEdt.set(R.drawable.ic_info_renzhen, "认证", "未知");
        mLocationEdt.set(R.drawable.ic_info_location, "地区", "未知");
        mIdView.set(R.drawable.ic_info_id, "ID", "");
        mLevelView.set(R.drawable.ic_info_level, "等级", "0");
        mGetNumsView.set(R.drawable.ic_info_get, "获得票数", "0");
        mSendNumsView.set(R.drawable.ic_info_send, "送出票数", "0");
    }
    private void setListeners() {
        mAvatarView.setOnClickListener(clickListener);
        mNickNameEdt.setOnClickListener(clickListener);
        mGenderEdt.setOnClickListener(clickListener);
        mSignEdt.setOnClickListener(clickListener);
        mRenzhengEdt.setOnClickListener(clickListener);
        mLocationEdt.setOnClickListener(clickListener);
        mCompleteBtn.setOnClickListener(clickListener);
    }
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.avatar) {
                //修改头像
                choosePic();
            } else if (id == R.id.nick_name) {
                //修改昵称
                showEditNickNameDialog();
            } else if (id == R.id.gender) {
                //修改性别
                showEditGenderDialog();
            } else if (id == R.id.sign) {
                //修改签名
                showEditSignDialog();
            } else if (id == R.id.renzheng) {
                //修改认证
                showEditRenzhengDialog();
            } else if (id == R.id.location) {
                //修改位置
                showEditLocationDialog();
            } else if (id == R.id.complete) {
                //完成，点击跳转到主界面
                Intent intent = new Intent();
                intent.setClass(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        }
    };
    private void setTitleBar() {
        mTitlebar.setTitle("编辑个人信息");
        mTitlebar.setTitleTextColor(Color.WHITE);
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).setSupportActionBar(mTitlebar);
        }
    }
    private void updateViews(TIMUserProfile timUserProfile) {
        //更新界面
        String faceUrl = timUserProfile.getFaceUrl();//获取头像url
        if (TextUtils.isEmpty(faceUrl)) {
            ImgUtils.loadRound(R.drawable.default_avatar, mAvatarImg);
        } else {
            ImgUtils.loadRound(faceUrl, mAvatarImg);
        }
        mNickNameEdt.updateValue(timUserProfile.getNickName());
        long genderValue = timUserProfile.getGender().getValue();
        String genderStr = genderValue == 1 ? "男" : "女";
        mGenderEdt.updateValue(genderStr);
        mSignEdt.updateValue(timUserProfile.getSelfSignature());
        mLocationEdt.updateValue(timUserProfile.getLocation());
        mIdView.updateValue(timUserProfile.getIdentifier());
        //自定义字段值更新
        Map<String, byte[]> customInfo = timUserProfile.getCustomInfo();
        mRenzhengEdt.updateValue(getValue(customInfo, CustomProfile.CUSTOM_RENZHENG, "未知"));
        mLevelView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_LEVEL, "0"));
        mGetNumsView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_GET, "0"));
        mSendNumsView.updateValue(getValue(customInfo, CustomProfile.CUSTOM_SEND, "0"));
    }
    private String getValue(Map<String, byte[]> customInfo, String key, String defaultValue) {
        if (customInfo != null) {
            byte[] valueBytes = customInfo.get(key);
            if (valueBytes != null) {
                return new String(valueBytes);
            }
        }
        return defaultValue;
    }
    private void showEditLocationDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setLocation(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新地区失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新地区成功，更新界面
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("地区", R.drawable.ic_info_location, mLocationEdt.getValue());
    }
    private void showEditSignDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setSelfSignature(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新签名失败：" + s, Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onSuccess() {
                        //更新签名成功，更新界面
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("签名", R.drawable.ic_info_sign, mSignEdt.getValue());
    }
    private void showEditRenzhengDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setCustomInfo(CustomProfile.CUSTOM_RENZHENG, content.getBytes(), new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新认证失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新认证成功，更新界面
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("认证", R.drawable.ic_info_renzhen, mRenzhengEdt.getValue());
    }
    private void showEditNickNameDialog() {
        EditStrProfileDialog dialog = new EditStrProfileDialog(getActivity());
        dialog.setOnOKListener(new EditStrProfileDialog.OnOKListener() {
            @Override
            public void onOk(String title, final String content) {
                TIMFriendshipManager.getInstance().setNickName(content, new TIMCallBack() {
                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新昵称失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新昵称成功，更新界面
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show("昵称", R.drawable.ic_info_nickname, mNickNameEdt.getValue());
    }
    private void showEditGenderDialog() {
        EditGenderDialog dialog = new EditGenderDialog(getActivity());
        dialog.setOnChangeGenderListener(new EditGenderDialog.OnChangeGenderListener() {
            @Override
            public void onChangeGender(boolean isMale) {
                TIMFriendGenderType gender = isMale ? TIMFriendGenderType.Male : TIMFriendGenderType.Female;
                TIMFriendshipManager.getInstance().setGender(gender, new TIMCallBack() {

                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getActivity(), "更新性别失败：" + s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess() {
                        //更新性别成功，更新界面
                        getSelfInfo();
                    }
                });
            }
        });
        dialog.show(mGenderEdt.getValue().equals("男"));
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mPicChooserHelper != null) {
            mPicChooserHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void choosePic() {
        if (mPicChooserHelper == null) {
            /*
            * 参数二：设置图片类型为头像
            * */
            mPicChooserHelper = new PicChooserHelper(this, PicChooserHelper.PicType.Avatar);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    updateAvatar(url);
                }

                @Override
                public void onFail(String msg) {
                    Toast.makeText(getActivity(), "选择失败：" + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }
    private void updateAvatar(String url) {
        /*
        * 上传图片成功到七牛云，更新TIM头像的信息
        * */
        TIMFriendshipManager.getInstance().setFaceUrl(url, new TIMCallBack() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(getActivity(), "头像更新失败：" + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                //更新头像成功，更新界面
                getSelfInfo();
            }
        });
    }
}
