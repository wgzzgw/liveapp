package com.study.utils;

import android.widget.ImageView;

import com.MyApplication;
import com.bumptech.glide.Glide;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by yy on 2018/4/17.
 */
//图片处理工具
public class ImgUtils {

    public static void loadRound(String url, ImageView targetView) {
        //将对应url图片进行图片裁剪后，设置到targetView
        Glide.with(MyApplication.getContext())
                .load(url)
                .bitmapTransform(new CropCircleTransformation(MyApplication.getContext()))//圆形裁剪
                .into(targetView);
    }
    //将默认图片进行图片裁剪后设置到targetView
    public static void loadRound(int resId, ImageView targetView) {
        Glide.with(MyApplication.getContext())
                .load(resId)
                .bitmapTransform(new CropCircleTransformation(MyApplication.getContext()))
                .into(targetView);
    }
    //将对应url图片设置到targetView
    public static void load(String url, ImageView targetView) {
        Glide.with(MyApplication.getContext())
                .load(url)
                .into(targetView);
    }
    //将默认图片设置到targetView
    public static void load(int resId, ImageView targetView) {
        Glide.with(MyApplication.getContext())
                .load(resId)
                .into(targetView);
    }
}
