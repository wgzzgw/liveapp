package com.study.hostlive;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.MyApplication;
import com.example.interactiveliveapp.R;
import com.google.gson.Gson;
import com.study.editprofile.CustomProfile;
import com.study.model.ChatMsgInfo;
import com.study.model.Constants;
import com.study.model.GiftCmdInfo;
import com.study.model.GiftInfo;
import com.study.model.UserInfo;
import com.study.request.HeartBeatRequest;
import com.study.request.JoinRoomRequest;
import com.study.request.QuitRoomRequest;
import com.study.utils.request.BaseRequest;
import com.study.view.BottomControlView;
import com.study.view.ChatMsgListView;
import com.study.view.ChatView;
import com.study.view.DanmuView;
import com.study.view.GiftFullView;
import com.study.view.GiftRepeatView;
import com.study.view.TitleView;
import com.study.view.WatcherEnterView;
import com.study.widget.HostControlDialog;
import com.study.widget.SizeChangeRelativeLayout;
import com.tencent.TIMCallBack;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveConstants;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVLiveRoomOption;
import com.tencent.livesdk.ILVText;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.widget.HeartLayout;

public class HostLiveActivity extends AppCompatActivity {
    private WatcherEnterView mWatcherEnterView;//观众动画view
    private SizeChangeRelativeLayout mSizeChangeLayout;//布局最外层—主要用于监听键盘活动
    private TitleView mTitleView;//主播房间成员view
    private AVRootView avRootView;//视频显示根控件[统一显示]
    private int mRoomId;//房间号
    private HostControlState hostControlState;//控制开关类
    private BottomControlView mControlView;//底部操作栏view
    private ChatView mChatView;//聊天操作栏view
    private ChatMsgListView mChatListView;//聊天列表消息view
    private DanmuView mDanmuView;//弹幕容器view
    private GiftRepeatView giftRepeatView;//连发礼物动画存放容器view
    private GiftFullView giftFullView;//全屏礼物动画存放容器view
    private HeartLayout heartLayout;//心形点赞View
    private Timer heartTimer = new Timer();//心形礼物动画定时器
    private FlashlightHelper flashlightHelper;//闪光灯辅助类
   /* private Timer heartBeatTimer = new Timer();//心跳定时器*/
    /*private HeartBeatRequest mHeartBeatRequest = null;//心跳请求*/
    private HostControlDialog.OnControlClickListener controlClickListener = new HostControlDialog.OnControlClickListener() {
        @Override
        public void onBeautyClick() {
            //点击美颜
            boolean isBeautyOn = hostControlState.isBeautyOn();
            if (isBeautyOn) {
                //关闭美颜
                ILiveRoomManager.getInstance().enableBeauty(0);
                hostControlState.setBeautyOn(false);
            } else {
                //打开美颜
                ILiveRoomManager.getInstance().enableBeauty(50);
                hostControlState.setBeautyOn(true);
            }
        }
        @Override
        public void onFlashClick() {
            // 闪光灯
            boolean isFlashOn = flashlightHelper.isFlashLightOn();
            if(hostControlState.getCameraid()==ILiveConstants.FRONT_CAMERA){
                //前置摄像头没有闪光灯
                isFlashOn=false;
            }
            if (isFlashOn) {
                flashlightHelper.enableFlashLight(false);
            } else {
                flashlightHelper.enableFlashLight(true);
            }
        }
        @Override
        public void onVoiceClick() {
            //声音
            boolean isVoiceOn = hostControlState.isVoiceOn();
            if (isVoiceOn) {
                //静音
                ILiveRoomManager.getInstance().enableMic(false);
                hostControlState.setVoiceOn(false);
            } else {
                ILiveRoomManager.getInstance().enableMic(true);
                hostControlState.setVoiceOn(true);
            }
        }
        @Override
        public void onCameraClick() {
            //相机
            int cameraId = hostControlState.getCameraid();//默认前置摄像头
            if (cameraId == ILiveConstants.FRONT_CAMERA) {
                ILiveRoomManager.getInstance().switchCamera(ILiveConstants.BACK_CAMERA);
                hostControlState.setCameraid(ILiveConstants.BACK_CAMERA);
            } else if (cameraId == ILiveConstants.BACK_CAMERA) {
                ILiveRoomManager.getInstance().switchCamera(ILiveConstants.FRONT_CAMERA);
                //切换前置摄像头，关闭闪光灯
                flashlightHelper.enableFlashLight(false);
                hostControlState.setCameraid(ILiveConstants.FRONT_CAMERA);
            }
        }
        @Override
        public void onDialogDismiss() {
            //Dialog消失,设置箭头向上
            mControlView.setOperateOpen(false);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_live);

        findAllViews();
        createLive();//创建直播
        //setDefault();//设置底部view默认显示BottomControlView
    }

    /*private void setDefault() {
        mControlView.setVisibility(View.VISIBLE);
        mChatView.setVisibility(View.INVISIBLE);
    }*/
    private void findAllViews() {
        mWatcherEnterView = (WatcherEnterView) findViewById(R.id.watcher_enter);
        mTitleView = (TitleView) findViewById(R.id.title_view);
        mTitleView.setHost(MyApplication.getApplication().getSelfProfile());

        mSizeChangeLayout = (SizeChangeRelativeLayout) findViewById(R.id.size_change_layout);
        mSizeChangeLayout.setOnSizeChangeListener(new SizeChangeRelativeLayout.OnSizeChangeListener() {
            @Override
            public void onLarge() {
                //键盘隐藏
                mChatView.setVisibility(View.INVISIBLE);
                mControlView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSmall() {
                //键盘显示
            }
        });


        avRootView = (AVRootView) findViewById(R.id.live_view);
        //配置视频显示根控件于ILVLiveManager
        ILVLiveManager.getInstance().setAvVideoView(avRootView);

        mControlView = (BottomControlView) findViewById(R.id.control_view);
        mControlView.setIsHost(true);
        //设置底部操作栏回调监听
        mControlView.setOnControlListener(new BottomControlView.OnControlListener() {
            @Override
            public void onChatClick() {
                //点击了聊天按钮，显示聊天操作栏
                mChatView.setVisibility(View.VISIBLE);
                mControlView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCloseClick() {
                // 点击了关闭按钮，关闭直播
                quitLive();
            }

            @Override
            public void onGiftClick() {
                //主播界面，不能发送礼物
            }

            @Override
            public void onOptionClick(View view) {
                //显示主播操作对话框
                boolean beautyOn = hostControlState.isBeautyOn();
                boolean flashOn =flashlightHelper.isFlashLightOn();
                boolean voiceOn = hostControlState.isVoiceOn();

                HostControlDialog hostControlDialog = new HostControlDialog(HostLiveActivity.this);

                hostControlDialog.setOnControlClickListener(controlClickListener);
                //更新view信息
                hostControlDialog.updateView(beautyOn, flashOn, voiceOn);
                hostControlDialog.show(view);//显示主播操作对话框
            }
        });
        mChatView = (ChatView) findViewById(R.id.chat_view);
      /*  mChatView.setOnChatSendListener(new ChatView.OnChatSendListener(){
            @Override
            public void onChatSend(final ILVText msg) {
               //回调发送按钮事件具体实现
                //发送消息
                ILVText.ILVTextType type = ILVText.ILVTextType.eGroupMsg;//群组消息
                ILVText iliveText = new ILVText(type, "",  String.valueOf(ILVLiveConstants.ILVLIVE_CMD_ENTER));
                iliveText.setText(msg.getText());
                //发送消息
                ILVLiveManager.getInstance().sendText(iliveText, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        *//*Toast.makeText(HostLiveActivity.this, "发送成功!", Toast.LENGTH_SHORT).show();*//*
                        ChatMsgInfo chatMsgInfo=new ChatMsgInfo();
                        chatMsgInfo.setText(msg.getText());
                        TIMUserProfile selfProfile= MyApplication.getApplication().getSelfProfile();
                        chatMsgInfo.setSenderId(selfProfile.getIdentifier());
                        String sendName=selfProfile.getNickName();
                        if(TextUtils.isEmpty(sendName)){
                            sendName=selfProfile.getIdentifier();
                        }
                        chatMsgInfo.setSenderName(sendName);
                        *//*chatMsgInfo.setAvatar(selfProfile.getFaceUrl());*//*
                        mChatListView.addMsgInfo(chatMsgInfo);
                    }
                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Toast.makeText(HostLiveActivity.this, "发送消息失败"+errCode+":"+errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/
        mChatView.setOnChatSendListener(new ChatView.OnChatSendListener() {
            @Override
            public void onChatSend(final ILVCustomCmd customCmd) {
                //回调发送按钮事件具体实现
                //发送自定义消息
                customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());//设置目标ID——获取IM聊天室id
                ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack<TIMMessage>() {
                    @Override
                    public void onSuccess(TIMMessage data) {
                        if (customCmd.getCmd() == Constants.CMD_CHAT_MSG_LIST) {
                            //如果是列表类型的消息，发送给列表显示
                            String chatContent = customCmd.getParam();
                            String userId = MyApplication.getApplication().getSelfProfile().getIdentifier();
                            String avatar = MyApplication.getApplication().getSelfProfile().getFaceUrl();
                            ChatMsgInfo info = ChatMsgInfo.createListInfo(chatContent, userId, avatar);
                            mChatListView.addMsgInfo(info);
                        } else if (customCmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU) {
                            //如果是弹幕类型的消息，发送给列表屏幕显示
                            String chatContent = customCmd.getParam();
                            String userId = MyApplication.getApplication().getSelfProfile().getIdentifier();
                            String avatar = MyApplication.getApplication().getSelfProfile().getFaceUrl();
                            ChatMsgInfo info = ChatMsgInfo.createListInfo(chatContent, userId, avatar);
                            mChatListView.addMsgInfo(info);

                            String name = MyApplication.getApplication().getSelfProfile().getNickName();
                            if (TextUtils.isEmpty(name)) {
                                name = userId;
                            }
                            ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(chatContent, userId, avatar, name);
                            mDanmuView.addMsgInfo(danmuInfo);
                        }
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Toast.makeText(HostLiveActivity.this, "发送消息失败" + errCode + ":" + errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        mChatListView = (ChatMsgListView) findViewById(R.id.chat_list);
        mDanmuView = (DanmuView) findViewById(R.id.danmu_view);
        giftRepeatView = (GiftRepeatView) findViewById(R.id.gift_repeat_view);
        giftFullView = (GiftFullView) findViewById(R.id.gift_full_view);
        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
    }

    private void createLive() {
        mRoomId = getIntent().getIntExtra("roomId", -1);

        if (mRoomId < 0) {
            Toast.makeText(getApplicationContext(), "房间号不正确", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }
        //直播配类，如配置消息的接收
        ILVLiveConfig liveConfig = MyApplication.getApplication().getLiveConfig();
        liveConfig.setLiveMsgListener(new ILVLiveConfig.ILVLiveMsgListener() {
            @Override
            //参数二：此ID为此消息的发送者ID
            public void onNewCustomMsg(ILVCustomCmd cmd, String id, TIMUserProfile userProfile) {
                //接收到自定义消息
                if (cmd.getCmd() == Constants.CMD_CHAT_MSG_LIST) {
                    //接收到列表消息
                    String content = cmd.getParam();
                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
                    mChatListView.addMsgInfo(info);
                } else if (cmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU) {
                    //接收到弹幕消息，显示在列表和屏幕
                    String content = cmd.getParam();
                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
                    mChatListView.addMsgInfo(info);

                    String name = userProfile.getNickName();
                    if (TextUtils.isEmpty(name)) {
                        name = userProfile.getIdentifier();
                    }
                    ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(content, id, userProfile.getFaceUrl(), name);
                    mDanmuView.addMsgInfo(danmuInfo);
                }else if (cmd.getCmd() == Constants.CMD_CHAT_GIFT) {
                    //界面显示礼物动画。
                    GiftCmdInfo giftCmdInfo = new Gson().fromJson(cmd.getParam(), GiftCmdInfo.class);
                    int giftId = giftCmdInfo.giftId;//此ID用来获取GiftInfo
                    String repeatId = giftCmdInfo.repeatId;
                    GiftInfo giftInfo = GiftInfo.getGiftById(giftId);
                    if (giftInfo == null) {
                        return;
                    }
                    GetGiftRequest getGiftRequest=new GetGiftRequest();
                    getGiftRequest.setOnResultListener(new BaseRequest.OnResultListener<UserInfo>() {
                        @Override
                        public void onSuccess(UserInfo userInfo) {
                            //更新IM的信息,参数一：key
                            //更新等级信息
                            TIMFriendshipManager.getInstance().setCustomInfo(CustomProfile.CUSTOM_LEVEL,
                                    (userInfo.level + "").getBytes(), new TIMCallBack() {
                                        @Override
                                        public void onError(int i, String s) {

                                        }

                                        @Override
                                        public void onSuccess() {
                                            getSelfInfo();
                                        }
                                    });
                            //更新送出票数
                            TIMFriendshipManager.getInstance().setCustomInfo(CustomProfile.CUSTOM_GET,
                                    (userInfo.getNums + "").getBytes(), new TIMCallBack() {
                                        @Override
                                        public void onError(int i, String s) {

                                        }

                                        @Override
                                        public void onSuccess() {
                                            getSelfInfo();
                                        }
                                    });
                        }
                        @Override
                        public void onFail(int code, String msg) {
                        }
                    });
                    //创建获取礼物参数类
                    GetGiftRequest.GetGiftParam param = new GetGiftRequest.GetGiftParam();
                    param.giftExp=giftInfo.expValue;
                    param.userId= MyApplication.getApplication().getSelfProfile().getIdentifier();
                    String url =getGiftRequest.getUrl(param);
                    //发起获取礼物请求
                    getGiftRequest.request(url);
                    if(giftInfo.giftId==GiftInfo.Gift_Heart.giftId){
                        //说明是心形礼物
                        heartLayout.addHeart(getRandomColor());
                    }else if (giftInfo.type == GiftInfo.Type.ContinueGift) {
                        giftRepeatView.showGift(giftInfo,repeatId, userProfile);
                    } else if (giftInfo.type == GiftInfo.Type.FullScreenGift) {
                        //全屏礼物
                        giftFullView.showGift(giftInfo, userProfile);
                    }
                }//以下cmd为腾讯SDK封装好
                else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_ENTER) {
                    //有用户进入直播
                    mTitleView.addWatcher(userProfile);
                    mWatcherEnterView.showWatcherEnter(userProfile);
                } else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_LEAVE) {
                    //有用户离开消息
                    mTitleView.removeWatcher(userProfile);
                }

            }

            @Override
            public void onNewTextMsg(final ILVText text, String SenderId, TIMUserProfile userProfile) {
                //接收到文本消息
                //通过ID获取用户的信息
            /*    List<String> ids =new ArrayList<String>();
                ids.add(SenderId);
                TIMFriendshipManager.getInstance().getUsersProfile(ids,new TIMValueCallBack<List<TIMUserProfile>>() {
                            @Override
                            public void onError(int i, String s) {
                            }

                            @Override
                            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                                TIMUserProfile timUserProfile=timUserProfiles.get(0);
                                ChatMsgInfo chatMsgInfo=new ChatMsgInfo();
                                chatMsgInfo.setText(text.getText());
                                chatMsgInfo.setSenderId(timUserProfile.getIdentifier());
                                String sendName=timUserProfile.getNickName();
                                if(TextUtils.isEmpty(sendName)){
                                    sendName=timUserProfile.getIdentifier();
                                }
                                chatMsgInfo.setSenderName(sendName);
                                mChatListView.addMsgInfo(chatMsgInfo);
                            }
                        });*/
            }

            @Override
            public void onNewOtherMsg(TIMMessage message) {
                //接收到其他消息
            }
        });
        //创建房间配置项
        ILVLiveRoomOption hostOption = new ILVLiveRoomOption(ILiveLoginManager.getInstance().getMyUserId()).
                controlRole("LiveMaster")//角色设置
                .autoFocus(true)//设置是否自动对焦用户视频
                .autoMic(hostControlState.isVoiceOn())//设置进入房间后是否自动打开Mic
                .authBits(AVRoomMulti.AUTH_BITS_DEFAULT)//设置通话能力权限位
                .cameraId(hostControlState.getCameraid())//设置默认摄像头id
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);//设置视频接收模式，是否开始半自动接收

        //创建房间
        ILVLiveManager.getInstance().createRoom(mRoomId, hostOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //开始心形动画
                startHeartAnim();
                //调用后台接口，更新房间信息
                JoinRoomRequest joinRoomRequest=new JoinRoomRequest();
                JoinRoomRequest.JoinRoomParam joinRoomParam=new JoinRoomRequest.JoinRoomParam();
                joinRoomParam.roomId=mRoomId+"";
                joinRoomParam.userId=MyApplication.getApplication().getSelfProfile().getIdentifier();
                //发起加入房间请求
                String requestUrl = joinRoomRequest.getUrl(joinRoomParam);
                joinRoomRequest.request(requestUrl);

                //开始发送心跳
                startHeartBeat();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //失败的情况下，退出界面
                Toast.makeText(HostLiveActivity.this, "创建直播失败！", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void startHeartAnim() {
        heartTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                heartLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        heartLayout.addHeart(getRandomColor());
                    }
                });
            }
        }, 0, 1000); //1秒钟
    }
    private void startHeartBeat() {
        MyApplication.getApplication().startHeartBeat(mRoomId);
      /*  heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //发送心跳包
                if (mHeartBeatRequest == null) {
                    mHeartBeatRequest = new HeartBeatRequest();
                }
                String roomId = mRoomId + "";
                String userId = MyApplication.getApplication().getSelfProfile().getIdentifier();
                String url = mHeartBeatRequest.getUrl(roomId, userId);
                mHeartBeatRequest.request(url);
            }
        }, 0, 4000); //4秒钟 。服务器是10秒钟去检测一次。*/
    }
    private Random heartRandom = new Random();
    private int getRandomColor() {
        return Color.rgb(heartRandom.nextInt(255), heartRandom.nextInt(255), heartRandom.nextInt(255));
    }
    @Override
    protected void onPause() {
        super.onPause();
        ILVLiveManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVLiveManager.getInstance().onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        quitLive();
        heartTimer.cancel();//防止内存泄漏
       /* heartBeatTimer.cancel();//同上*/
        MyApplication.getApplication().stopHeartBeat();
    }
    @Override
    public void onBackPressed() {
        quitLive();//按back键后退出直播
    }
    //退出直播
    private void quitLive() {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setType(ILVText.ILVTextType.eGroupMsg);
        customCmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_LEAVE);
        customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
        //发送退出直播房间消息
        ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //退出房间
                ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        Toast.makeText(getApplicationContext(), "退出直播房间成功", Toast.LENGTH_SHORT).show();
                        logout();
                    }
                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Toast.makeText(getApplicationContext(), "退出直播房间失败" + errCode + ":" + errMsg, Toast.LENGTH_SHORT).show();
                        logout();
                    }
                });
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
            }
        });
        //发送退出消息给服务器
        QuitRoomRequest request = new QuitRoomRequest();
        String roomId = mRoomId +"";
        String userId = MyApplication.getApplication().getSelfProfile().getIdentifier();
        String url = request.getUrl(roomId, userId);
        request.request(url);
    }
    private void logout() {
        finish();
    }
    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(HostLiveActivity.this, "获取信息失败:" + s, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                MyApplication.getApplication().setSelfProfile(timUserProfile);
            }
        });
    }
}
