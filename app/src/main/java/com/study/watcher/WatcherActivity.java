package com.study.watcher;

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
import com.study.view.GiftRepeatView;
import com.study.view.GiftFullView;
import com.study.view.TitleView;
import com.study.view.WatcherEnterView;
import com.study.widget.GiftSelectDialog;
import com.study.widget.SizeChangeRelativeLayout;
import com.tencent.TIMCallBack;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMMessage;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.ilivesdk.ILiveCallBack;
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import tyrantgit.widget.HeartLayout;

public class WatcherActivity extends AppCompatActivity {
    private SizeChangeRelativeLayout mSizeChangeLayout;//布局最外层—主要用于监听键盘活动
    private AVRootView avRootView;//视频显示根控件[统一显示]
    private int mRoomId;//房间号
    private String hostId;//主播ID
    private BottomControlView mControlView;//底部操作栏view
    private ChatView mChatView;//聊天操作栏view
    private ChatMsgListView mChatListView;//聊天列表消息view
    private DanmuView mDanmuView;//弹幕容器view
    private GiftSelectDialog giftSelectDialog;//礼物选择对话框
    private GiftRepeatView giftRepeatView;//连发礼物动画存放容器view
    private GiftFullView giftFullView;//全屏礼物动画存放容器view
    private HeartLayout heartLayout;//心形点赞View
    private Timer heartTimer = new Timer();//心形礼物动画定时器
    private TitleView titleView;//观众观看直播成员view
    private WatcherEnterView mWatcherEnterView;//观众进入房间动画view
    /*private HeartBeatRequest mHeartBeatRequest = null;//心跳请求
    private Timer heartBeatTimer = new Timer();//心跳定时器*/
    private void sendGift(final ILVCustomCmd customCmd){
        customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
        //调用腾讯SDK将礼物消息发送出去，目标为ILiveRoomManager.getInstance().getIMGroupId()
        ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack<TIMMessage>() {
            @Override
            public void onSuccess(TIMMessage data) {
                if (customCmd.getCmd() == Constants.CMD_CHAT_GIFT) {
                    //界面显示礼物动画。
                    GiftCmdInfo giftCmdInfo = new Gson().fromJson(customCmd.getParam(), GiftCmdInfo.class);//实现从Json相关对象到java实体
                    int giftId = giftCmdInfo.giftId;
                    String repeatId = giftCmdInfo.repeatId;
                    GiftInfo giftInfo = GiftInfo.getGiftById(giftId);//得到发送的礼物
                    if (giftInfo == null) {
                        return;
                    }
                    SendGiftRequest sendGiftRequest=new SendGiftRequest();
                    sendGiftRequest.setOnResultListener(new BaseRequest.OnResultListener<UserInfo>() {
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
                            TIMFriendshipManager.getInstance().setCustomInfo(CustomProfile.CUSTOM_SEND,
                                    (userInfo.sendNums + "").getBytes(), new TIMCallBack() {
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
                    //创建发送礼物参数类
                    SendGiftRequest.SendGiftParam param = new SendGiftRequest.SendGiftParam();
                    param.giftExp=giftInfo.expValue;
                    param.userId= MyApplication.getApplication().getSelfProfile().getIdentifier();
                    String url =sendGiftRequest.getUrl(param);
                    //发起发送礼物请求
                    sendGiftRequest.request(url);
                    if(giftInfo.giftId==GiftInfo.Gift_Heart.giftId){
                        //说明是心形礼物
                        heartLayout.addHeart(getRandomColor());
                    }else if (giftInfo.type == GiftInfo.Type.ContinueGift) {
                        //显示动画，参数二为赠送者即账号者ID
                        giftRepeatView.showGift(giftInfo, repeatId,MyApplication.getApplication().getSelfProfile());
                    } else if (giftInfo.type == GiftInfo.Type.FullScreenGift) {
                        //全屏礼物
                        giftFullView.showGift(giftInfo, MyApplication.getApplication().getSelfProfile());
                    }
                }
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(WatcherActivity.this,"发送礼物失败"+errCode+"："+errMsg,Toast.LENGTH_SHORT).show();
            }
        });
    }
    private GiftSelectDialog.OnGiftSendListener giftSendListener = new GiftSelectDialog.OnGiftSendListener() {
                            @Override
                            public void onGiftSendClick(final ILVCustomCmd customCmd) {
                              sendGift(customCmd);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watcher);

        findAllViews();
        joinRoom();//观众加入房间
    }
    private void findAllViews() {
        mWatcherEnterView = (WatcherEnterView) findViewById(R.id.watcher_enter);
        titleView = (TitleView) findViewById(R.id.title_view);
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
        mControlView = (BottomControlView) findViewById(R.id.control_view);
        mControlView.setIsHost(false);//不是主播界面，显示礼物按钮
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
                quitRoom();
                finish();
            }

            @Override
            public void onGiftClick() {
                //显示礼物九宫格
                if (giftSelectDialog == null) {
                    giftSelectDialog = new GiftSelectDialog(WatcherActivity.this);
                    giftSelectDialog.setGiftSendListener(giftSendListener);
                }
                mControlView.setVisibility(View.INVISIBLE);
                giftSelectDialog.show();
                giftSelectDialog.setGiftDialogCloseListener(new GiftSelectDialog.GiftDialogCloseListener() {
                    @Override
                    public void onClose() {
                        giftSelectDialog.hide();
                        mControlView.setVisibility(View.VISIBLE);
                    }
                });
                giftSelectDialog.setGiftSendListener(giftSendListener);
            }
            @Override
            public void onOptionClick(View view) {
                //操作点击，观众不需要处理
            }
        });
        mChatView = (ChatView) findViewById(R.id.chat_view);
       /* mChatView.setOnChatSendListener(new ChatView.OnChatSendListener(){
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
                        Toast.makeText(WatcherActivity.this, "发送消息失败"+errCode+":"+errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/
        mChatView.setOnChatSendListener(new ChatView.OnChatSendListener() {
            @Override
            public void onChatSend(final ILVCustomCmd customCmd) {
                //回调发送按钮事件具体实现
                //发送消息
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
                        }else if (customCmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU) {
                            //如果是弹幕类型的消息，发送给列表和屏幕显示
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
                    }

                });
            }
        });
        mChatListView = (ChatMsgListView) findViewById(R.id.chat_list);
        avRootView= (AVRootView) findViewById(R.id.live_view);
        //配置视频显示根控件于ILVLiveManager
        ILVLiveManager.getInstance().setAvVideoView(avRootView);
        mDanmuView = (DanmuView) findViewById(R.id.danmu_view);
        giftRepeatView = (GiftRepeatView) findViewById(R.id.gift_repeat_view);
        giftFullView = (GiftFullView) findViewById(R.id.gift_full_view);
        heartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        heartLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //发送心形礼物
                ILVCustomCmd giftCmd = new ILVCustomCmd();
                giftCmd.setType(ILVText.ILVTextType.eGroupMsg);
                giftCmd.setCmd(Constants.CMD_CHAT_GIFT);
                giftCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
                GiftCmdInfo giftCmdInfo = new GiftCmdInfo();
                giftCmdInfo.giftId = GiftInfo.Gift_Heart.giftId;
                giftCmd.setParam(new Gson().toJson(giftCmdInfo));
                //发送消息
               sendGift(giftCmd);
            }
        });
    }
    private void joinRoom() {
        mRoomId = getIntent().getIntExtra("roomId", -1);
        hostId = getIntent().getStringExtra("hostId");
        if (mRoomId < 0) {
            Toast.makeText(getApplicationContext(), "房间号不正确", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (TextUtils.isEmpty(hostId)) {
            return;
        }
        //直播配类，如配置消息的接收
        ILVLiveConfig liveConfig = MyApplication.getApplication().getLiveConfig();
        liveConfig.setLiveMsgListener(new ILVLiveConfig.ILVLiveMsgListener() {
            @Override
          /* @param text        内容
            @param SenderId    发送者id
            @param userProfile 托管在IM的用户资料*/
            public void onNewCustomMsg(ILVCustomCmd cmd, String id, TIMUserProfile userProfile) {
                //接收到自定义消息
                if (cmd.getCmd() == Constants.CMD_CHAT_MSG_LIST) {
                    //接收到列表消息
                    String content = cmd.getParam();
                    ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
                    mChatListView.addMsgInfo(info);
                }else if (cmd.getCmd() == Constants.CMD_CHAT_MSG_DANMU) {
                    //接收到弹幕消息,显示在列表和屏幕上
                        String content = cmd.getParam();
                        ChatMsgInfo info = ChatMsgInfo.createListInfo(content, id, userProfile.getFaceUrl());
                        mChatListView.addMsgInfo(info);

                        String name = userProfile.getNickName();
                        if (TextUtils.isEmpty(name)) {
                            name = userProfile.getIdentifier();
                        }
                        ChatMsgInfo danmuInfo = ChatMsgInfo.createDanmuInfo(content, id, userProfile.getFaceUrl(), name);
                        mDanmuView.addMsgInfo(danmuInfo);
                    } else if (cmd.getCmd() == Constants.CMD_CHAT_GIFT) {
                    //界面显示礼物动画。
                    GiftCmdInfo giftCmdInfo = new Gson().fromJson(cmd.getParam(), GiftCmdInfo.class);
                    int giftId = giftCmdInfo.giftId;
                    String repeatId = giftCmdInfo.repeatId;
                    GiftInfo giftInfo = GiftInfo.getGiftById(giftId);
                    if (giftInfo == null) {
                        return;
                    }
                    if(giftInfo.giftId==GiftInfo.Gift_Heart.giftId){
                        //说明是心形礼物
                        heartLayout.addHeart(getRandomColor());
                    }else if (giftInfo.type == GiftInfo.Type.ContinueGift) {
                        giftRepeatView.showGift(giftInfo,repeatId,userProfile);
                    } else if (giftInfo.type == GiftInfo.Type.FullScreenGift) {
                        //全屏礼物
                            giftFullView.showGift(giftInfo, userProfile);
                    }
                }else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_LEAVE) {
                    //有用户离开消息
                    if (hostId.equals(userProfile.getIdentifier())) {
                        //主播退出直播，
                        quitRoom();
                    } else {
                        //观众退出直播
                        titleView.removeWatcher(userProfile);
                    }
                } else if (cmd.getCmd() == ILVLiveConstants.ILVLIVE_CMD_ENTER) {
                    titleView.addWatcher(userProfile);
                    mWatcherEnterView.showWatcherEnter(userProfile);
                }
            }
            @Override
            public void onNewTextMsg(final ILVText text, String SenderId, TIMUserProfile userProfile) {
                //接收到文本消息
                //通过ID获取用户的信息
              /*  List<String> ids =new ArrayList<String>();
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


        //加入房间配置项
        ILVLiveRoomOption memberOption = new ILVLiveRoomOption(hostId)
                .autoCamera(false) //是否自动打开摄像头
                .controlRole("Guest") //角色设置
                .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM |
                        AVRoomMulti.AUTH_BITS_RECV_AUDIO |
                        AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO |
                        AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO) //权限设置
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO) //是否开始半自动接收
                .autoMic(false);//是否自动打开mic
        //加入房间
        ILVLiveManager.getInstance().joinRoom(mRoomId, memberOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //开始心形动画
                startHeartAnim();
                //同时发送进入直播的消息。
                sendEnterRoomMsg();
                //显示主播的头像
                updateTitleView();
                //调用后台接口，更新房间信息
                JoinRoomRequest joinRoomRequest=new JoinRoomRequest();
                JoinRoomRequest.JoinRoomParam joinRoomParam=new JoinRoomRequest.JoinRoomParam();
                joinRoomParam.roomId=mRoomId+"";
                joinRoomParam.userId=MyApplication.getApplication().getSelfProfile().getIdentifier();
                //发起加入房间请求
                String requestUrl = joinRoomRequest.getUrl(joinRoomParam);
                joinRoomRequest.request(requestUrl);

                //开始心跳包
                startHeartBeat();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(WatcherActivity.this,"直播已结束",Toast.LENGTH_SHORT).show();
                quitRoom();
            }
        });
    }
    private void startHeartBeat() {
     /*   heartBeatTimer.scheduleAtFixedRate(new TimerTask() {
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
        MyApplication.getApplication().startHeartBeat(mRoomId);
    }
    private void sendEnterRoomMsg() {
        ILVCustomCmd customCmd = new ILVCustomCmd();
        customCmd.setType(ILVText.ILVTextType.eGroupMsg);
        customCmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_ENTER);
        customCmd.setDestId(ILiveRoomManager.getInstance().getIMGroupId());
        //发送消息——自己进入直播房间
        ILVLiveManager.getInstance().sendCustomCmd(customCmd, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
            }
        });
    }
    //更新观众端主播的信息和已经存在此房间的其它观众
    private void updateTitleView() {
        List<String> list = new ArrayList<String>();
        list.add(hostId);
        TIMFriendshipManager.getInstance().getUsersProfile(list, new TIMValueCallBack<List<TIMUserProfile>>() {
            @Override
            public void onError(int i, String s) {
                //失败：
                titleView.setHost(null);
            }
            @Override
            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                //只有一个主播的信息
                titleView.setHost(timUserProfiles.get(0));
            }
        });
        // 添加自己的头像到titleView上。
        titleView.addWatcher(MyApplication.getApplication().getSelfProfile());
        //请求已经加入房间的成员信息
        GetWatcherRequest watcherRequest = new GetWatcherRequest();
        watcherRequest.setOnResultListener(new BaseRequest.OnResultListener<Set<String>>() {
            @Override
            public void onFail(int code, String msg) {
            }
            @Override
            public void onSuccess(Set<String> watchers) {
                if (watchers == null) {
                    return;
                }
                List<String> watcherList = new ArrayList<String>();
                watcherList.addAll(watchers);
                TIMFriendshipManager.getInstance().getUsersProfile(watcherList, new TIMValueCallBack<List<TIMUserProfile>>() {
                    @Override
                    public void onError(int i, String s) {
                        //失败：
                    }
                    @Override
                    public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                        //添加已经在房间的观众信息
                        titleView.addWatchers(timUserProfiles);
                    }
                });
            }
        });
        String watcherRequestUrl = watcherRequest.getUrl(mRoomId + "");
        watcherRequest.request(watcherRequestUrl);
    }
    private void startHeartAnim() {
        //用于安排指定的任务进行重复的固定速率执行，在指定的延迟后开始。
        /*task--这是被调度的任务。
        delay--这是以毫秒为单位的延迟之前的任务执行。
        period--这是在连续执行任务之间的毫秒的时间。*/
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
    private Random heartRandom = new Random();
    //随机产生心形礼物动画颜色
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
        heartTimer.cancel();//防止内存泄漏
        quitRoom();
       /* heartBeatTimer.cancel();//同上*/
        MyApplication.getApplication().stopHeartBeat();
    }
    //按back键退出房间
    @Override
    public void onBackPressed() {
        quitRoom();
    }
    private void quitRoom() {
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
                        Toast.makeText(getApplicationContext(),"退出直播房间成功",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Toast.makeText(getApplicationContext(),"退出直播房间失败"+errCode+":"+errMsg,Toast.LENGTH_SHORT).show();
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
        String roomId = mRoomId + "";
        String userId = MyApplication.getApplication().getSelfProfile().getIdentifier();
        String url = request.getUrl(roomId, userId);
        request.request(url);
        logout();
    }
    private void logout() {
        finish();
    }
    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(WatcherActivity.this, "获取信息失败:" + s, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                MyApplication.getApplication().setSelfProfile(timUserProfile);
            }
        });
    }
}
