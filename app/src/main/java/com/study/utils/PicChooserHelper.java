package com.study.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.MyApplication;
import com.qiniu.android.http.ResponseInfo;
import com.study.widget.PicChooseDialog;
import com.tencent.TIMUserProfile;

import java.io.File;
import java.io.IOException;

/**
 * Created by yy on 2018/4/18.
 */

public class PicChooserHelper {
    private Activity mActivity;
    private Uri mCameraFileUri;
    private TIMUserProfile mUserProfile;
    private Fragment mFragment;//关联fragment
    private static final int FROM_CAMERA = 2;
    private static final int FROM_ALBUM = 1;
    private static final int CROP = 0;
    public static enum PicType {
        Avatar, Cover
    }
    private PicType mPicType;//图片的类型，枚举型=>头像，封面
    private Uri cropUri = null;
    private OnChooseResultListener mOnChooserResultListener;
    public PicChooserHelper(Activity activity, PicType picType) {
        mActivity = activity;
        mPicType = picType;

    }
    public PicChooserHelper(Fragment fragment, PicType picType) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
        mPicType = picType;
        mUserProfile = MyApplication.getApplication().getSelfProfile();
    }
    public void showPicChooserDialog() {
        if(mActivity==null){
            mActivity=mFragment.getActivity();
        }
        PicChooseDialog dialog = new PicChooseDialog(mActivity);
        dialog.setOnDialogClickListener(new PicChooseDialog.OnDialogClickListener() {
            @Override
            public void onCamera() {
                //拍照
                takePicFromCamera();
            }
            @Override
            public void onAlbum() {
                //相册
                PicFromAlbum();
            }
        });
        dialog.show();
    }

    private void takePicFromCamera() {
        mCameraFileUri = createAlbumUri();
        //启动相机程序
        Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //获取设备的系统版本
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24) {
            //小于7.0的版本
            //指定图片输出地址
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, mCameraFileUri);
            if (mFragment == null) {
                mActivity.startActivityForResult(intentCamera, FROM_CAMERA);
            } else {
                mFragment.startActivityForResult(intentCamera, FROM_CAMERA);
            }
        }else{
            //大于7.0的版本
           // Uri uri = FileProvider.getUriForFile(mActivity.this,"com.study.utils.fileprovider",file);
            Uri uri = getImageContentUri(mCameraFileUri);
            intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            if (mFragment == null) {
                mActivity.startActivityForResult(intentCamera, FROM_CAMERA);
            } else {
                mFragment.startActivityForResult(intentCamera, FROM_CAMERA);
            }
        }
        }
    private void PicFromAlbum() {
        /*
        * 相册选择进行运行时权限申请
        * */
        if(ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(mActivity,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            takePicFromAlbum();
        }
    }
    private void takePicFromAlbum() {
        Intent picIntent = new Intent("android.intent.action.GET_CONTENT");
        //设置MIME类型
        picIntent.setType("image/*");
        //打开相册
        if (mFragment == null) {
            mActivity.startActivityForResult(picIntent, FROM_ALBUM);
        } else {
            mFragment.startActivityForResult(picIntent, FROM_ALBUM);
        }
    }
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicFromAlbum();
                }else{
                    Toast.makeText(mActivity,"你已拒绝了授权",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    private Uri createAlbumUri() {
        /*
        * 使用应用关联目录可以不进行运行时权限
        * */
        String dirPath = Environment.getExternalStorageDirectory() + "/" + mActivity.getApplication().getApplicationInfo().packageName;
        File dir = new File(dirPath);
        if (!dir.exists()||dir.isFile()) {
            dir.mkdirs();//创建文件夹
        }
        String id = "";
        if (mUserProfile != null) {
            id = mUserProfile.getIdentifier();//获取UserProfile ID
        }
        String fileName = id + ".jpg";
        //创建File对象，用于存储拍照后的图片
        File picFile = new File(dirPath, fileName);
        if (picFile.exists()) {
            picFile.delete();
        }
        return Uri.fromFile(picFile);
    }
    /**
     * 转换 content:// uri
     */
    public Uri getImageContentUri(Uri uri) {
        String filePath = uri.getPath();//得到图片的uri地址，uri统一资源标识符
        Cursor cursor = mActivity.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            //拼接uri
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            return mActivity.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_CAMERA) {
            //从相机拍摄返回
            if (resultCode == Activity.RESULT_OK) {
                startCrop(mCameraFileUri);//裁剪图片
            }
        } else if (requestCode == FROM_ALBUM) {
            //从相册选择返回
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                startCrop(uri);//裁剪图片
            }
        } else if (requestCode == CROP) {
            //裁剪结束
            if (resultCode == Activity.RESULT_OK) {
                //上传到服务器保存起来
                //七牛上传
                uploadTo7Niu(cropUri.getPath());
            }
        }
    }
    /*
    * 同createAlbumUri，filename不同
    * */
    private Uri createCropUri() {
        String dirPath = Environment.getExternalStorageDirectory() + "/" + mActivity.getApplication().getApplicationInfo().packageName;
        File dir = new File(dirPath);
        if (!dir.exists()||dir.isFile()) {
            dir.mkdirs();
        }
        String id = "";
        if (mUserProfile != null) {
            id = mUserProfile.getIdentifier();
        }
        String fileName="";
        if(mPicType==PicType.Avatar){
        fileName = id + "Avatar_crop.jpg";}
        else if(mPicType==PicType.Cover){
         fileName = id + "Avatar_crop.jpg";
        }
        File picFile = new File(dirPath, fileName);
        if (picFile.exists()) {
            picFile.delete();
        }
        try {
            picFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(picFile);
    }
    private void startCrop(Uri uri) {
        cropUri = createCropUri();//返回crop uri
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.putExtra("crop", "true");
        //设置输入大小与输出大小
        if(mPicType == PicType.Avatar) {
            intent.putExtra("aspectX", 300);
            intent.putExtra("aspectY", 300);
            intent.putExtra("outputX", 300);
            intent.putExtra("outputY", 300);
        }else if(mPicType == PicType.Cover){
            intent.putExtra("aspectX", 500);
            intent.putExtra("aspectY", 300);
            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 300);
        }
        intent.putExtra("return-data", false);
        //设置图片输出类型
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion < 24) {
            //小于7.0的版本
            //设置裁剪数据和数据类型
            intent.setDataAndType(uri, "image/*");
            //设置图片输出地址
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            if (mFragment == null) {
                mActivity.startActivityForResult(intent, CROP);
            } else {
                mFragment.startActivityForResult(intent, CROP);
            }
        } else {
            //大于7.0的版本
            {
                String scheme = uri.getScheme();//获取uri协议
                if (scheme.equals("content")) {
                    //设置裁剪数据和数据类型
                    intent.setDataAndType(uri, "image/*");
                } else {
                    Uri contentUri = getImageContentUri(uri);
                    //设置裁剪数据和数据类型
                    intent.setDataAndType(contentUri, "image/*");
                }
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cropUri);
            if (mFragment == null) {
                mActivity.startActivityForResult(intent, CROP);
            } else {
                mFragment.startActivityForResult(intent, CROP);
            }
        }
    }
    private void uploadTo7Niu(String path) {
        String id = "";
        if (mUserProfile != null) {
            id = mUserProfile.getIdentifier();
        }
        String name="";
        if(mPicType==PicType.Avatar){
            name = id + "_" + System.currentTimeMillis() + "_avatar";
        }
        else if(mPicType==PicType.Cover){
            name = id + "_" + System.currentTimeMillis() + "_cover";
        }
        /*
        * path:图片路径，name:七牛云Bucket，回调接口
        * */
        QnUploadHelper.uploadPic(path, name, new QnUploadHelper.UploadCallBack() {
            @Override
            public void success(String url) {
                //上传成功
                if (mOnChooserResultListener != null) {
                    mOnChooserResultListener.onSuccess(url);
                }
            }

            @Override
            public void fail(String key, ResponseInfo info) {
                //上传失败！
                if (mOnChooserResultListener != null) {
                    mOnChooserResultListener.onFail(info.error);
                }
            }
        });
    }
/*
* 上传图片最终回调接口
* */
    public interface OnChooseResultListener {
        void onSuccess(String url);
        void onFail(String msg);
    }
    public void setOnChooseResultListener(OnChooseResultListener l) {
        mOnChooserResultListener = l;
    }
}
