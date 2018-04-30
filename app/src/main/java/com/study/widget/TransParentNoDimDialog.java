package com.study.widget;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.interactiveliveapp.R;

/**
 * Created by yy on 2018/4/23.
 */
/*
对话框基类
 */
public class TransParentNoDimDialog {
    protected Activity activity;//所属activity
    protected Dialog dialog;
    public TransParentNoDimDialog(Activity activity) {
        this.activity = activity;
        dialog = new Dialog(activity, R.style.dialog_nodim);
        /*参数二：a style resource describing the theme to use for the
         window, or {@code 0} to use the default dialog theme*/
    }
    //对话框设置布局
    public void setContentView(View view) {
        dialog.setContentView(view);
    }
    //对话框展现
    public void show() {
        dialog.show();
    }
    //对话框隐藏
    public void hide() {
        dialog.hide();
    }
    //对话框设置宽高
    public void setWidthAndHeight(int width, int height) {
        Window win = dialog.getWindow();
        //对话框配置类
        WindowManager.LayoutParams params = win.getAttributes();
        if (params != null) {
            params.width = width;
            params.height = height;
            win.setAttributes(params);
        }
    }
}
