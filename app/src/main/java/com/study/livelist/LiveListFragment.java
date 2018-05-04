package com.study.livelist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.interactiveliveapp.R;
import com.study.model.RoomInfo;
import com.study.utils.ImgUtils;
import com.study.utils.request.BaseRequest;
import com.study.watcher.WatcherActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yy on 2018/4/19.
 */

public class LiveListFragment extends Fragment {
    private ListView mLiveListView;//直播listview
    private LiveListAdapter mLiveListAdapter;//自定义直播列表适配器
    private SwipeRefreshLayout mSwipeRefreshLayout;//下拉刷新控件

    Toolbar titlebar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_live_list, container, false);

        findAllViews(view);
        setTitleBar();
        requestLiveList();

        return view;
    }
    private void setTitleBar() {
        titlebar.setTitle("直播列表");
        titlebar.setTitleTextColor(Color.WHITE);
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            ((AppCompatActivity) activity).setSupportActionBar(titlebar);
        }
    }
    private void findAllViews(View view) {

        titlebar = (Toolbar) view.findViewById(R.id.titlebar);
        mLiveListView = (ListView) view.findViewById(R.id.live_list);
        mLiveListAdapter = new LiveListAdapter(getActivity().getApplicationContext());
        mLiveListView.setAdapter(mLiveListAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout_list);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //下拉事件：请求服务器，获取直播列表
                requestLiveList();
            }
        });
    }
    private class LiveListAdapter extends BaseAdapter {
        private Context mContext;
        private List<RoomInfo> liveRooms = new ArrayList<RoomInfo>();//存储数据源
        public LiveListAdapter(Context context) {
            this.mContext = context;
        }
        public void removeAllRoomInfos() {
            liveRooms.clear();
        }
        public void addRoomInfos(List<RoomInfo> roomInfos) {
            if (roomInfos != null) {
                liveRooms.clear();
                liveRooms.addAll(roomInfos);
                notifyDataSetChanged();//通知listview刷新界面
            }
        }
        @Override
        public int getCount() {
            return liveRooms.size();
        }

        @Override
        public RoomInfo getItem(int position) {
            return liveRooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RoomHolder holder = null;
            //对重复加载布局进行了优化——convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_live_list, null);
                holder = new RoomHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (RoomHolder) convertView.getTag();
            }
            holder.bindData(liveRooms.get(position));
            return convertView;
        }
        //内部类用于对控件实例进行缓存
        private class RoomHolder {
            View itemView;
            TextView liveTitle;//直播标题
            ImageView liveCover;//直播封面
            ImageView hostAvatar;//主播头像
            TextView hostName;//主播名字
            TextView watchNums;//观看人数
            public RoomHolder(View view) {
                itemView = view;
                liveTitle = (TextView) view.findViewById(R.id.live_title);
                liveCover = (ImageView) view.findViewById(R.id.live_cover);
                hostName = (TextView) view.findViewById(R.id.host_name);
                hostAvatar = (ImageView) view.findViewById(R.id.host_avatar);
                watchNums = (TextView) view.findViewById(R.id.watch_nums);
            }
            //绑定数据到界面
            public void bindData(final RoomInfo roomInfo) {
                String userName = roomInfo.userName;
                if (TextUtils.isEmpty(userName)) {
                    userName = roomInfo.userId;
                }
                hostName.setText(userName);

                String liveTitleStr = roomInfo.liveTitle;
                if (TextUtils.isEmpty(liveTitleStr)) {
                    this.liveTitle.setText(userName + "的直播");
                } else {
                    this.liveTitle.setText(liveTitleStr);
                }
                String url = roomInfo.liveCover;
                if (TextUtils.isEmpty(url)) {
                    ImgUtils.load(R.drawable.default_cover, liveCover);
                } else {
                    ImgUtils.load(url, liveCover);
                }

                String avatar = roomInfo.userAvatar;
                if (TextUtils.isEmpty(avatar)) {
                    ImgUtils.loadRound(R.drawable.default_avatar, hostAvatar);
                } else {
                    ImgUtils.loadRound(avatar, hostAvatar);
                }

                int watchers = roomInfo.watcherNums;
                String watchText = watchers + "人\r\n正在看";
                watchNums.setText(watchText);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //跳转到主播界面
                        Intent intent = new Intent();
                        intent.setClass(mContext, WatcherActivity.class);
                        //传值——房间号，主播ID
                        intent.putExtra("roomId", roomInfo.roomId);
                        intent.putExtra("hostId", roomInfo.userId);
                        startActivity(intent);
                    }
                });
            }
        }

    }
    private void requestLiveList() {
        //请求前20个数据
        GetLiveListRequest liveListRequest = new GetLiveListRequest();
        liveListRequest.setOnResultListener(new BaseRequest.OnResultListener<List<RoomInfo>>() {
            @Override
            public void onSuccess(List<RoomInfo> roomInfos) {

                mLiveListAdapter.removeAllRoomInfos();//下拉刷新，先移除掉之前的room信息
                mLiveListAdapter.addRoomInfos(roomInfos);//再添加新的信息

                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFail(int code, String msg) {
                Toast.makeText(getActivity(), "请求列表失败：" + msg, Toast.LENGTH_SHORT).show();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
        //创建获取直播列表参数类
        GetLiveListRequest.LiveListParam param = new GetLiveListRequest.LiveListParam();
        param.pageIndex = 0;//从0开始，也就是第一页。
        String url = liveListRequest.getUrl(param);
        //发起获取直播列表请求
        liveListRequest.request(url);
    }
}
