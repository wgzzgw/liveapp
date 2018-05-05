package com.study.createroom;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.MyApplication;
import com.example.interactiveliveapp.R;
import com.study.hostlive.HostLiveActivity;
import com.study.utils.ImgUtils;
import com.study.utils.PicChooserHelper;
import com.study.utils.request.BaseRequest;
import com.study.model.RoomInfo;
import com.tencent.TIMUserProfile;

/**
 * Created by yy on 2018/4/19.
 */
/*
* 创建直播准备工作界面
* */
public class CreateLiveActivity extends AppCompatActivity {
    private Toolbar titlebar;
    private View mSetCoverView;//设置直播封面view
    private ImageView mCoverImg;//view中的背景封面
    private TextView mCoverTipTxt;//view中的设置话语提示
    private EditText mTitleEt;//输入标题
    private TextView mCreateRoomBtn;//开始直播按钮
    private TextView mRoomNoText;//房间号
    private PicChooserHelper mPicChooserHelper;//选择封面图片工具类
    private String coverUrl = null;//用于存储选择图片后的url
    /*
    * 创建按钮监听
    * */
    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.create) {
                //创建直播
                requestCreateRoom();
            } else if (id == R.id.set_cover) {
                //选择封面图片
                choosePic();
            }
        }
    };
    //创建直播
    private void requestCreateRoom() {
         //请求服务器，获取新的roomId
        // 请求涉及网络请求，使用okhttp库

        //创建直播房间参数类
        CreateRoomRequest.CreateRoomParam param = new CreateRoomRequest.CreateRoomParam();
        TIMUserProfile selfProfile = MyApplication.getApplication().getSelfProfile();
       param.userId = selfProfile.getIdentifier();
        param.userAvatar = selfProfile.getFaceUrl();
        String nickName = selfProfile.getNickName();
        param.userName = TextUtils.isEmpty(nickName) ? selfProfile.getIdentifier() : nickName;
        param.liveTitle = mTitleEt.getText().toString();
        param.liveCover = coverUrl;
        //创建房间
        CreateRoomRequest request = new CreateRoomRequest();
        request.setOnResultListener(new BaseRequest.OnResultListener<RoomInfo>() {
            @Override
            public void onFail(int code, String msg) {
                Toast.makeText(CreateLiveActivity.this, "请求失败：" + msg, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(RoomInfo roomInfo) {
                Toast.makeText(CreateLiveActivity.this, "请求成功：" + roomInfo, Toast.LENGTH_SHORT).show();
                //跳转到直播界面
                Intent intent = new Intent();
                intent.setClass(CreateLiveActivity.this, HostLiveActivity.class);
                //传值 —房间号
                intent.putExtra("roomId", roomInfo.roomId);
                startActivity(intent);

                finish();
            }
        });

        //发起创建直播请求
        String requestUrl = request.getUrl(param);
        request.request(requestUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        findAllViews();
        setListeners();
        setupTitlebar();
    }



    private void findAllViews() {
        mSetCoverView = findViewById(R.id.set_cover);
        mCoverImg = (ImageView) findViewById(R.id.cover);
        mCoverTipTxt = (TextView) findViewById(R.id.tv_pic_tip);
        mTitleEt = (EditText) findViewById(R.id.title);
        mCreateRoomBtn = (TextView) findViewById(R.id.create);
        mRoomNoText = (TextView) findViewById(R.id.room_no);
    }
    private void setListeners() {
        mSetCoverView.setOnClickListener(clickListener);
        mCreateRoomBtn.setOnClickListener(clickListener);
    }
    private void setupTitlebar() {
        titlebar = (Toolbar) findViewById(R.id.titlebar);
        titlebar.setTitle("开始我的直播");
        titlebar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(titlebar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titlebar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void choosePic() {
        if (mPicChooserHelper == null) {
            /*
            * 参数二：设置图片类型为封面
            * */
            mPicChooserHelper = new PicChooserHelper(this, PicChooserHelper.PicType.Cover);
            mPicChooserHelper.setOnChooseResultListener(new PicChooserHelper.OnChooseResultListener() {
                @Override
                public void onSuccess(String url) {
                    //获取图片成功
                    updateCover(url);
                }

                @Override
                public void onFail(String msg) {
                    //获取图片失败
                    Toast.makeText(CreateLiveActivity.this, "选择失败：" + msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
        mPicChooserHelper.showPicChooserDialog();
    }
    //更新封面
    private void updateCover(String url) {
        coverUrl = url;
        ImgUtils.load(url, mCoverImg);
        /*
        * 隐藏tiptextview
        * */
        mCoverTipTxt.setVisibility(View.GONE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mPicChooserHelper != null) {
            mPicChooserHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
