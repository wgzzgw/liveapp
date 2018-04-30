package com.study.view;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interactiveliveapp.R;
import com.study.utils.ImgUtils;
import com.study.widget.UserInfoDialog;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yy on 2018/4/28.
 */
/*
主播房间成员view
 */
public class TitleView extends LinearLayout {
    private ImageView hostAvatarImgView;//主播头像
    private TextView watchersNumView;//观看人数
    private int watcherNum = 0;
    private RecyclerView watcherListView;//观众列表
    private WatcherAdapter watcherAdapter;
    private String hostId; //主播id
    //private TIMUserProfile timUserProfile;//缓存主播信息，节省网络请求开销，也有弊端，信息得不到及时更新
    public TitleView(Context context) {
        super(context);
        init();
    }
    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public TitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater.from(getContext()).inflate(R.layout.view_title, this, true);
        findAllViews();
    }
    private void findAllViews() {
        hostAvatarImgView = (ImageView) findViewById(R.id.host_avatar);
        watchersNumView = (TextView) findViewById(R.id.watchers_num);
        hostAvatarImgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击头像，显示主播详情对话框
                showUserInfoDialog(hostId);
            }
        });
        watcherListView = (RecyclerView) findViewById(R.id.watch_list);
        //setHasFixedSize 的作用就是确保尺寸是通过用户输入从而确保RecyclerView的尺寸是一个常数。
        // RecyclerView 的Item宽或者高不会变。每一个Item添加或者删除都不会变。
        watcherListView.setHasFixedSize(true);
        //水平滚动
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        watcherListView.setLayoutManager(layoutManager);
        //设置adapter
        watcherAdapter = new WatcherAdapter(getContext());
        watcherListView.setAdapter(watcherAdapter);
    }
    //向外提供添加主播头像
    public void setHost(TIMUserProfile userProfile) {
       /* timUserProfile=userProfile;//缓存主播信息*/
        if(userProfile == null){
            ImgUtils.loadRound(R.drawable.default_avatar, hostAvatarImgView);
        }else {
            hostId = userProfile.getIdentifier();
            String avatarUrl = userProfile.getFaceUrl();
            if (TextUtils.isEmpty(avatarUrl)) {
                ImgUtils.loadRound(R.drawable.default_avatar, hostAvatarImgView);
            } else {
                ImgUtils.loadRound(avatarUrl, hostAvatarImgView);
            }
        }
    }
    //向外提供添加单个观众
    public void addWatcher(TIMUserProfile userProfile) {
        if (userProfile != null) {
            watcherAdapter.addWatcher(userProfile);
            watcherNum++;
            watchersNumView.setText("观众:" + watcherNum);
        }
    }
    //向外提供添加多个观众
    public void addWatchers(List<TIMUserProfile> userProfileList){
        if(userProfileList != null){
            watcherAdapter.addWatchers(userProfileList);
            watcherNum+= userProfileList.size();
            watchersNumView.setText("观众:" + watcherNum);
        }
    }
    //向外提供删除单个观众
    public void removeWatcher(TIMUserProfile userProfile) {
        if (userProfile != null) {
            watcherAdapter.removeWatcher(userProfile);
            watcherNum--;
            watchersNumView.setText("观众:" + watcherNum);
        }
    }
    /*防止观众异常退出而循环删除
    public void removeWatcher(TIMUserProfile userProfile) {
    if(userProfile==null){return ;}
        List<TIMUserProfile> removeWatcher=new ArrayList<TIMUserProfile>();
        for(TIMUserProfile watcher:watcherList){
            if(watcher.getIdentifier().equals(userProfile.getIdentifier())){
                removeWatcher.add(watcher);
            }
        }
        watcherList.removeAll(removeWatcher);
        watcherAdapter.notifyDataSetChanged();
    }*/
    public class WatcherAdapter extends RecyclerView.Adapter{
        private Context mContext;
        //观众数据源
        private List<TIMUserProfile> watcherList = new ArrayList<TIMUserProfile>();
        private Map<String , TIMUserProfile> watcherMap = new HashMap<String , TIMUserProfile>();
        WatcherAdapter(Context context) {
            mContext = context;
        }
        //向外提供添加数据源
        public void addWatchers(List<TIMUserProfile> userProfileList){
            if(userProfileList == null){
                return;
            }
            for(TIMUserProfile userProfile : userProfileList){
                if (userProfile != null) {
                    //Map.containsKey判断Map集合对象中是否包含指定的键名。
                    // 如果Map集合中包含指定的键名，则返回true，否则返回false
                    boolean inWatcher = watcherMap.containsKey(userProfile.getIdentifier());
                    if(!inWatcher) {
                        watcherList.add(userProfile);
                        watcherMap.put(userProfile.getIdentifier(), userProfile);
                    }
                }
            }
            notifyDataSetChanged();
        }
        //向外提供添加单条数据
        public void addWatcher(TIMUserProfile userProfile) {
            if (userProfile != null) {
                boolean inWatcher = watcherMap.containsKey(userProfile.getIdentifier());
                if(!inWatcher) {
                    watcherList.add(userProfile);
                    watcherMap.put(userProfile.getIdentifier(), userProfile);
                    notifyDataSetChanged();
                }
            }
        }
        //向外提供删除单挑数据
        public void removeWatcher(TIMUserProfile userProfile) {
            if (userProfile == null) {
                return;
            }
            TIMUserProfile targetUser = watcherMap.get(userProfile.getIdentifier());
            if(targetUser != null) {
                watcherList.remove(targetUser);
                watcherMap.remove(targetUser.getIdentifier());
                notifyDataSetChanged();
            }
        }
        @Override
        public int getItemCount() {
            return watcherList.size();
        }
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.adapter_watcher, parent, false);
            WatcherHolder holder = new WatcherHolder(itemView);
            return holder;
        }
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof WatcherHolder) {
                ((WatcherHolder) holder).bindData(watcherList.get(position));
            }
        }
        /*
        内部类用于对控件实例进行缓存
         */
        private class WatcherHolder extends RecyclerView.ViewHolder {

            private ImageView avatarImg;//观众头像
            public WatcherHolder(View itemView) {
                super(itemView);
                avatarImg = (ImageView) itemView.findViewById(R.id.user_avatar);
            }
            //绑定数据到界面
            public void bindData(final TIMUserProfile userProfile) {
                String avatarUrl = userProfile.getFaceUrl();
                if (TextUtils.isEmpty(avatarUrl)) {
                    ImgUtils.loadRound(R.drawable.default_avatar, avatarImg);
                } else {
                    ImgUtils.loadRound(avatarUrl, avatarImg);
                }
                avatarImg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //显示观众的信息
                      showUserInfoDialog(userProfile.getIdentifier());
                    }
                });
            }
        }
    }

    private void showUserInfoDialog(String senderId) {
        List<String> ids = new ArrayList<String>();
        ids.add(senderId);
        TIMFriendshipManager.getInstance().getUsersProfile(ids, new TIMValueCallBack<List<TIMUserProfile>>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(TitleView.this.getContext(), "请求用户信息失败", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                Context context = TitleView.this.getContext();
                if(context instanceof Activity) {
                    UserInfoDialog userInfoDialog = new UserInfoDialog((Activity) context, timUserProfiles.get(0));
                    userInfoDialog.show();
                }
            }
        });
    }
}
