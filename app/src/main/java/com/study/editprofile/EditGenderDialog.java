package com.study.editprofile;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;

import com.example.interactiveliveapp.R;
import com.study.widget.TransParentDialog;

/**
 * Created by yy on 2018/4/18.
 */

public class EditGenderDialog  extends TransParentDialog {
    /*
    单选按钮
     */
    private RadioButton maleView;
    private RadioButton femaleView;
    private OnChangeGenderListener onChangeGenderListener;
    /*
   dialog按钮回调接口
    */
    public interface OnChangeGenderListener {
        void onChangeGender(boolean isMale);
    }
    public void setOnChangeGenderListener(OnChangeGenderListener l) {
        onChangeGenderListener = l;
    }
    public EditGenderDialog(Activity activity) {
        super(activity);
        //加载dialog布局
        View mainView = LayoutInflater.from(activity).inflate(R.layout.dialog_edit_gender, null, false);
        maleView = (RadioButton) mainView.findViewById(R.id.male);
        femaleView = (RadioButton) mainView.findViewById(R.id.female);
        mainView.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取修改内容
                boolean isMaleChecked = maleView.isChecked();
                if (onChangeGenderListener != null) {
                    onChangeGenderListener.onChangeGender(isMaleChecked);
                }
                hide();
            }
        });
        //调用父类方法设置布局
        setContentView(mainView);
        //调用父类方法设置dialog宽为屏幕的80%，高为WRAP_CONTENT
        setWidthAndHeight(activity.getWindow().getDecorView().getWidth() * 80 / 100, WindowManager.LayoutParams.WRAP_CONTENT);
    }
    public void show(boolean isMale) {
        maleView.setChecked(isMale);
        femaleView.setChecked(!isMale);
        show();
    }
}
