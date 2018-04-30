package com.study.register;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.MyApplication;
import com.example.interactiveliveapp.R;
import com.study.editprofile.EditProfileActivity;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class RegisterActivity extends AppCompatActivity {
    private Toolbar mTitlebar;

    private EditText mAccountEdt;//注册账号
    private EditText mPasswordEdt;//注册密码
    private EditText mConfirmPasswordEt;//确认密码

    private Button mRegisterBtn;//注册按钮


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        findAllViews();
        setListeners();
        setTitleBar();

    }

    private void findAllViews() {
        mTitlebar = (Toolbar) findViewById(R.id.titlebar);
        mAccountEdt = (EditText) findViewById(R.id.account);
        mPasswordEdt = (EditText) findViewById(R.id.password);
        mConfirmPasswordEt = (EditText) findViewById(R.id.confirm_password);
        mRegisterBtn = (Button) findViewById(R.id.register);

    }
    private void setListeners() {
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注册
                register();
            }
        });
    }
    private void setTitleBar() {
        mTitlebar.setTitle("注册新用户");
        mTitlebar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(mTitlebar);
    }
    private void register() {
        String accountStr = mAccountEdt.getText().toString();
        String passwordStr = mPasswordEdt.getText().toString();
        String confirmPswStr = mConfirmPasswordEt.getText().toString();
        //检测是否为空
        if (TextUtils.isEmpty(accountStr) ||
                TextUtils.isEmpty(passwordStr) ||
                TextUtils.isEmpty(confirmPswStr)) {
            Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        //检测两次密码是否一致
        if (!passwordStr.equals(confirmPswStr)) {
            Toast.makeText(this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        //检测输入字符是否合法
        if(accountStr.length()<8||
                passwordStr.length()<8||
                confirmPswStr.length()<8){
            Toast.makeText(this,"用户名或密码长度不够！",Toast.LENGTH_SHORT).show();
            return ;
        }
        //注册账号
        ILiveLoginManager.getInstance().tlsRegister(accountStr, passwordStr, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //注册成功
                Toast.makeText(RegisterActivity.this, "注册账号成功，正在登录", Toast.LENGTH_SHORT).show();
                //登录一下
                login();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //注册失败
                Toast.makeText(RegisterActivity.this, "注册账号失败，错误码：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void login() {
        final String accountStr = mAccountEdt.getText().toString();
        String passwordStr = mPasswordEdt.getText().toString();

        //调用腾讯IM登录，tls登录
        ILiveLoginManager.getInstance().tlsLogin(accountStr, passwordStr, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                //登陆成功
                loginLive(accountStr, data);
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(RegisterActivity.this, "tls登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginLive(String accountStr, String data) {
        ILiveLoginManager.getInstance().iLiveLogin(accountStr, data, new ILiveCallBack() {

            @Override
            public void onSuccess(Object data) {
                //最终登录成功
                //跳转到修改用户信息界面。
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this,EditProfileActivity.class);
                startActivity(intent);
                //获取初始数据
                getSelfInfo();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(RegisterActivity.this, "iLive登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(RegisterActivity.this, "获取信息失败：" + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功
                MyApplication.getApplication().setSelfProfile(timUserProfile);
            }
        });
    }

}