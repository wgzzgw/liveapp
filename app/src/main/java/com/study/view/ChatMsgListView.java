package com.study.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.model.ChatMsgInfo;
import com.study.utils.ImgUtils;
import com.study.widget.UserInfoDialog;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yy on 2018/4/22.
 */
/*
*  聊天消息列表view
* */
public class ChatMsgListView extends RelativeLayout {
    private ListView mChatMsgListView;
    private ChatMsgAdapter mChatMsgAdapter;
    public ChatMsgListView(Context context) {
        super(context);
        init();
    }
    public ChatMsgListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ChatMsgListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_chat_msg_list, this, true);
        findAllViews();
    }
    private void findAllViews() {
        mChatMsgListView = (ListView) findViewById(R.id.chat_msg_list);
        mChatMsgAdapter = new ChatMsgAdapter();
        mChatMsgListView.setAdapter(mChatMsgAdapter);
        //设置item点击事件
        mChatMsgListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatMsgInfo msgInfo = mChatMsgAdapter.getItem(position);//获取到此消息关联的信息
                showUserInfoDialog(msgInfo.getSenderId());//显示userinfo dialog
            }
        });
    }
    private void showUserInfoDialog(String senderId) {
        List<String> ids = new ArrayList<String>();
        ids.add(senderId);
        TIMFriendshipManager.getInstance().getUsersProfile(ids, new TIMValueCallBack<List<TIMUserProfile>>() {
            @Override
            public void onError(int i, String s) {
            }

            @Override
            public void onSuccess(List<TIMUserProfile> timUserProfiles) {
                Context context = ChatMsgListView.this.getContext();
                if(context instanceof Activity) {
                    UserInfoDialog userInfoDialog = new UserInfoDialog((Activity) context, timUserProfiles.get(0));
                    userInfoDialog.show();
                }
            }
        });
    }
    //添加单条消息
    public void addMsgInfo(ChatMsgInfo info) {
        if (info != null) {
            mChatMsgAdapter.addMsgInfo(info);
            //滚动到最后一条消息
            mChatMsgListView.smoothScrollToPosition(mChatMsgAdapter.getCount());
        }
    }
    //添加多条消息
    public void addMsgInfos(List<ChatMsgInfo> infos) {
        if (infos != null) {
            mChatMsgAdapter.addMsgInfos(infos);
            //滚动到最后一条消息
            mChatMsgListView.smoothScrollToPosition(mChatMsgAdapter.getCount());
        }
    }

    /*
    内部适配器类
     */
    private class ChatMsgAdapter extends BaseAdapter {
        private List<ChatMsgInfo> mChatMsgInfos = new ArrayList<ChatMsgInfo>();//消息数据源
        //添加单条消息
        public void addMsgInfo(ChatMsgInfo info) {
            if (info != null) {
                mChatMsgInfos.add(info);
                notifyDataSetChanged();
            }
        }
        //批量添加消息
        public void addMsgInfos(List<ChatMsgInfo> infos) {
            if (infos != null) {
                mChatMsgInfos.addAll(infos);
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return mChatMsgInfos.size();
        }

        @Override
        public ChatMsgInfo getItem(int i) {
            return mChatMsgInfos.get(i);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            //对重复加载布局进行了优化——convertView
            ChatMsgHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.view_chat_msg_list_item, null);
                holder = new ChatMsgHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ChatMsgHolder) convertView.getTag();
            }
            holder.bindData(mChatMsgInfos.get(i));
            return convertView;
        }
    }

    //内部类holder用于对控件实例进行缓存
        private class ChatMsgHolder {
            private ImageView avatar;//发送者头像
            private TextView content;//发送者内容

            private ChatMsgInfo chatMsgInfo;//封装信息类

            public ChatMsgHolder(View itemView) {
                avatar = (ImageView) itemView.findViewById(R.id.sender_avatar);
                content = (TextView) itemView.findViewById(R.id.chat_content);
            }
            //绑定数据到界面
            public void bindData(ChatMsgInfo info) {
                chatMsgInfo = info;
                String avatarUrl = info.getAvatar();
                if (TextUtils.isEmpty(avatarUrl)) {
                    ImgUtils.loadRound(R.drawable.default_avatar, avatar);
                } else {
                    ImgUtils.loadRound(avatarUrl, avatar);
                }
                content.setText(info.getContent());
                //不同的level显示不同的level icon
           /* Sets the Drawables (if any) to appear to the left of, above, to the
           right of, and below the text. Use 0 if you do not want a Drawable there.
           The Drawables' bounds will be set to their intrinsic bounds.*/
                /*content.setCompoundDrawablesWithIntrinsicBounds(R.drawable.**,0,0,0);*/
            }
        }
        }

