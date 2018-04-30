package com.study.editprofile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.interactiveliveapp.R;
import com.study.widget.TransParentDialog;

/**
 * Created by yy on 2018/4/17.
 */

public class EditStrProfileDialog extends TransParentDialog {
    private TextView titleView;
    private EditText contentView;
    private OnOKListener onOKListener;
    private String mTitle;
    /*
    dialog按钮回调接口
     */
    public interface OnOKListener {
        void onOk(String title, String content);
    }
    public void setOnOKListener(OnOKListener l) {
        onOKListener = l;
    }
    public EditStrProfileDialog(Activity activity) {
        super(activity);
        //加载dialog布局
        View mainView = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_str_profile, null, false);
        titleView = (TextView) mainView.findViewById(R.id.title);
        contentView = (EditText) mainView.findViewById(R.id.content);
        //调用父类方法设置布局
        setContentView(mainView);
        //调用父类方法设置dialog宽为屏幕的80%，高为WRAP_CONTENT
        setWidthAndHeight(activity.getWindow().getDecorView().getWidth() * 80 / 100,
                WindowManager.LayoutParams.WRAP_CONTENT);
        mainView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取修改内容
                String content = contentView.getText().toString();
                if (onOKListener != null) {
                    onOKListener.onOk(mTitle, content);
                }
                hide();
            }
        });

}
    /*
    重写父类show方法，外部调用以修改dialog适应场景
     */
    public void show(String title, int resId, String defaultContent) {
        mTitle = title;
        titleView.setText("请输入" + title);
        contentView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
        contentView.setText(defaultContent);
        show();
    }
}
