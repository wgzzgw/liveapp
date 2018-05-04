package com.study.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.MyApplication;
import com.example.interactiveliveapp.R;
import com.study.editprofile.EditProfileActivity;
import com.study.main.MainActivity;
import com.study.register.RegisterActivity;
import com.study.utils.ImgUtils;
import com.tencent.TIMFriendshipManager;
import com.tencent.TIMUserProfile;
import com.tencent.TIMValueCallBack;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.core.ILiveLoginManager;

public class LoginActivity extends AppCompatActivity {
    private EditText mAccountEdt;//输入账号
    private EditText mPasswordEdt;//输入密码
    private Button mLoginBtn;//登录按钮
    private Button mRegisterBtn;//注册按钮
    private ImageView lgimage;//app登录界面图片
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findAllViews();//获取控件实例
        setListeners();
        ImgUtils.loadRound(R.drawable.loginimage,lgimage);
    }
    private void findAllViews() {
        mAccountEdt = (EditText) findViewById(R.id.account);
        mPasswordEdt = (EditText) findViewById(R.id.password);
        mLoginBtn = (Button) findViewById(R.id.login);
        mRegisterBtn = (Button) findViewById(R.id.register);
        lgimage=(ImageView)findViewById(R.id.loginimage);
    }
    private void setListeners() {
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //登录操作
                login();
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //注册的操作
                register();
            }
        });
    }

    private void login() {
        final String accountStr = mAccountEdt.getText().toString();
        String passwordStr = mPasswordEdt.getText().toString();

        //输入为空的检测
        if(TextUtils.isEmpty(accountStr)||TextUtils.isEmpty(passwordStr)){
            Toast.makeText(this,"用户名或密码不能为空！",Toast.LENGTH_SHORT).show();
            return ;
        }
        //调用腾讯IM登录,tls登录，登录为异步过程，通过回调函数返回是否成功，成功后方能进行后续操作。
        ILiveLoginManager.getInstance().tlsLogin(accountStr, passwordStr, new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                //tls登陆成功
                loginLive(accountStr, data);//跳转到ilive登录
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                //登录失败
                Toast.makeText(LoginActivity.this, "tls登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });

    }
    //ilive登录
    private void loginLive(String accountStr, String data) {
        // 参数一：identifier为用户名，参数二：userSig 为用户登录凭证
        ILiveLoginManager.getInstance().iLiveLogin(accountStr, data, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                //最终登录成功
                Toast.makeText(LoginActivity.this, "登录成功！", Toast.LENGTH_SHORT).show();
                //判断是否第一次登陆
                SharedPreferences sp=getSharedPreferences("FirstLogin",MODE_PRIVATE);
                boolean isFirst=sp.getBoolean("firstLogin",true);
                if(isFirst){
                //跳转到修改信息界面
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, EditProfileActivity.class);
                startActivity(intent);
                }else{
                    //跳转到主界面
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                //获取用户信息
                getSelfInfo();

                finish();
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
                //错误码 code 和错误描述 desc，可用于定位请求失败原因
                //登录失败
                Toast.makeText(LoginActivity.this, "iLive登录失败：" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void register() {
        //注册新用户，跳转到注册页面。
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
    private void getSelfInfo() {
        TIMFriendshipManager.getInstance().getSelfProfile(new TIMValueCallBack<TIMUserProfile>() {
            @Override
            public void onError(int i, String s) {
                Toast.makeText(LoginActivity.this, "获取信息失败：" + s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(TIMUserProfile timUserProfile) {
                //获取自己信息成功，保存timUserProfile下来，调用MyApplication.setSelfProfile
                MyApplication.getApplication().setSelfProfile(timUserProfile);
            }
        });
    }
}
