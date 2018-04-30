package com.study.widget;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.interactiveliveapp.R;

/**
 * Created by yy on 2018/4/28.
 */
/*
主播操作对话框
 */
public class HostControlDialog extends TransParentNoDimDialog{
    private TextView mBeautyView;//美颜
    private TextView mFlashView;//闪光灯
    private TextView mVoiceView;//声音

    private int dialogWidth;
    private int dialogHeight;

    public HostControlDialog(Activity activity) {
        super(activity);
        View mainView =
                LayoutInflater.from(activity).inflate(R.layout.dialog_host_control, null, false);
        setContentView(mainView);
        findAllViews(mainView);
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        mainView.measure(width, height);
        //得到测量后的值
        dialogWidth = mainView.getMeasuredWidth();
        dialogHeight = mainView.getMeasuredHeight();
        setWidthAndHeight(dialogWidth, dialogHeight);
    }

    private void findAllViews(View mainView) {
        mBeautyView = (TextView) mainView.findViewById(R.id.beauty);
        mBeautyView.setOnClickListener(clickListener);
        mFlashView = (TextView) mainView.findViewById(R.id.flash_light);
        mFlashView.setOnClickListener(clickListener);
        mVoiceView = (TextView) mainView.findViewById(R.id.voice);
        mVoiceView.setOnClickListener(clickListener);
        mainView.findViewById(R.id.camera).setOnClickListener(clickListener);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(listener!=null){
                    listener.onDialogDismiss();
                }
            }
        });
    }
    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(listener != null) {
                if (v.getId() == R.id.beauty) {
                    listener.onBeautyClick();
                }else if(v.getId() == R.id.flash_light){
                    listener.onFlashClick();
                }else if(v.getId() == R.id.voice){
                    listener.onVoiceClick();
                }else if(v.getId() == R.id.camera){
                    listener.onCameraClick();
                }
            }
            hide();
        }
    };
    private OnControlClickListener listener;
    public void setOnControlClickListener(OnControlClickListener l){
        listener = l;
    }
    public interface OnControlClickListener{
        public void onBeautyClick();
        public void onFlashClick();
        public void onVoiceClick();
        public void onCameraClick();
        public void onDialogDismiss();
    }
    public void show(View view) {
        //view在屏幕上的位置。
        int[] outLocation = new int[2];
        view.getLocationOnScreen(outLocation);

        Window window = dialog.getWindow();
        //以左上角坐标显示
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        //使对话框居于view中间
        params.x = outLocation[0] - (dialogWidth - view.getWidth()) / 2;
        params.y = outLocation[1] - dialogHeight - view.getHeight();
        params.alpha = 0.7f;
        window.setAttributes(params);
        super.show();
    }
    public void updateView(boolean beautyOn, boolean flashOn, boolean voiceOn) {
        if (beautyOn) {
            mBeautyView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_beauty_on, 0, 0, 0);
            mBeautyView.setText("关美颜");
        } else {
            mBeautyView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_beauty_off, 0, 0, 0);
            mBeautyView.setText("开美颜");
        }
        if (flashOn) {
            mFlashView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_flashlight_on, 0, 0, 0);
            mFlashView.setText("关闪光");
        } else {
            mFlashView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_flashlight_off, 0, 0, 0);
            mFlashView.setText("开闪光");
        }
        if (voiceOn) {
            mVoiceView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_mic_on, 0, 0, 0);
            mVoiceView.setText("关声音");
        } else {
            mVoiceView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_mic_off, 0, 0, 0);
            mVoiceView.setText("开声音");
        }
    }
}
