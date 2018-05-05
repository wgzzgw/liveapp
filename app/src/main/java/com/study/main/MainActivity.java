package com.study.main;

import android.content.Intent;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.Toast;

import com.example.interactiveliveapp.R;
import com.study.createroom.CreateLiveActivity;
import com.study.editprofile.EditProfileFragment;
import com.study.livelist.LiveListFragment;

import org.w3c.dom.Text;

/*
* APP主界面
* */
public class MainActivity extends AppCompatActivity {
    //容纳fragment的layout
    private FrameLayout mContainer;
    //切换fragment
    private FragmentTabHost mTabHost;
    private long mExitTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findAllViews();
        setTabs();
    }
    private void findAllViews() {
        mContainer = (FrameLayout) findViewById(R.id.fragment_container);
        mTabHost = (FragmentTabHost) findViewById(R.id.fragment_tabhost);
    }
    private void setTabs() {
        mTabHost.setup(this, getSupportFragmentManager(), R.id.fragment_container);

        //添加fragment.
        {
            //setIndicator设置指示器，指示当前图片选中与未选中的view
            TabHost.TabSpec profileTab = mTabHost.newTabSpec("livelist").setIndicator(getIndicator(R.drawable.tab_livelist));
            mTabHost.addTab(profileTab, LiveListFragment.class, null);
            //设置没有分割线
            mTabHost.getTabWidget().setDividerDrawable(null);
        }

        {
            TabHost.TabSpec profileTab = mTabHost.newTabSpec("createlive").setIndicator(getIndicator(R.drawable.tab_publish_live));
            mTabHost.addTab(profileTab, null, null);
            mTabHost.getTabWidget().setDividerDrawable(null);
        }

        {
            TabHost.TabSpec profileTab = mTabHost.newTabSpec("profile").setIndicator(getIndicator(R.drawable.tab_profile));
            mTabHost.addTab(profileTab, EditProfileFragment.class, null);
            mTabHost.getTabWidget().setDividerDrawable(null);
        }

        mTabHost.getTabWidget().getChildTabViewAt(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //跳转到创建直播的页面。
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CreateLiveActivity.class);
                startActivity(intent);
            }
        });
    }
/*
* 返回对应tab view
* */
    private View getIndicator(int resId) {
        View tabView = LayoutInflater.from(this).inflate(R.layout.view_indicator, null);
        ImageView tabImg = (ImageView) tabView.findViewById(R.id.tab_icon);
        tabImg.setImageResource(resId);
        return tabView;
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if ((System.currentTimeMillis() - mExitTime) > 2000) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            mExitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}
