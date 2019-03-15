package com.gyr.disvisibledemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.fragment.PrruMapFragment;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class FloorMapActivity extends BaseActivity implements View.OnClickListener {
    private static final int CODE_OPEN_CAMERA = 1;
    private static final String IMAGE_ROOT_PATH = Environment.getExternalStorageState() + File.separator + "Tester";//todo
    private Context mContext;
    private boolean mFirst = false;
    private TextView mToolName;
    private LinearLayout mBack;
    private LinearLayout mAdd;


    public final int TYPE_TAKE_PHOTO = 1;//Uri获取类型判断
    private static final int CODE_TAKE_PHOTO = 1;// 拍照
    private static final int CODE_SHORT_VIDEO = 2;// 短视频
    public final int NEED_CAMERA = 0;
    private String imgPath;//图片路径
    private Uri photoUri;
    private String mapPath;
    private PrruMapFragment prruMapFragment;
    private File ffile;


    @Override
    public void findView() {
        mBack = findViewById(R.id.back);
        mToolName = findViewById(R.id.tool_top_name);
        mAdd = findViewById(R.id.tool_right_add);
        mAdd.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(this);
    }

    public String getMap() {
        return mapPath;
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_floor_map);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (!mFirst) {
                mFirst = true;
            }

        }
    }

    @Override
    public void dealLogicBeforeInitView() {
        mapPath = (String) getIntent().getExtras().get("floormap");
        prruMapFragment = new PrruMapFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.prru_replace, prruMapFragment);
        fragmentTransaction.commit();

    }




    @Override
    public void initView() {
        mToolName.setText("pRRU");
    }

    @Override
    public void dealLogicAfterInitView() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) //二维码扫描返回结果
        {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "取消", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "扫描结果: " + result.getContents(), Toast.LENGTH_LONG).show();
                }
            }
        }
        switch (requestCode) {
            case CODE_TAKE_PHOTO:  //拍照返回后获取图片
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        if (data.hasExtra("data")) {
                            //有数据
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //>N
                                String photoPath = "";
                                if (data.getData() != null) {
                                    photoPath = data.getData().getPath();
                                }
                                Log.e("XHF", "have data:path" + photoPath);
                                imgPath = photoPath;

                            } else {//<7.0
                                imgPath = data.getData().getPath();

                            }
                        }
                    } else {
                        //没有数据
                        MediaScannerConnection.scanFile(mContext, new String[]{photoUri.getPath()}, null, null); //扫描文件
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  //>23
                            try {
                                String photoUriPath = photoUri.getPath();
                                imgPath = photoUriPath;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            imgPath = photoUri.getPath();
                        }
                    }
                }
                Toast.makeText(mContext, "Path:" + imgPath, Toast.LENGTH_SHORT).show();
                if (imgPath != null && !imgPath.equals("")) {
                    copyPhototoPath(imgPath, IMAGE_ROOT_PATH);
                }

                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
        }
    }


    /**
     * 拍照  适配7.0上下
     */
    public void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = get24MediaFileUri(TYPE_TAKE_PHOTO);
            //添加权限
            takeIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takeIntent, CODE_TAKE_PHOTO);
        } else {
            Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = getMediaFileUri(TYPE_TAKE_PHOTO);
            takeIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(takeIntent, CODE_TAKE_PHOTO);
        }
    }

    //24以上版本获取
    public Uri get24MediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            ffile = mediaFile;
        } else {
            return null;
        }
        return FileProvider.getUriForFile(mContext, "com.gyr.disvisibledemo.fileprovider", mediaFile);
    }

    //24以下版本获取
    public Uri getMediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Photo");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
            ffile = mediaFile;
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);
    }

    /***
     *
     * 将拍摄的照片复制到指定位置
     *
     * @param oldPath
     * @param newPath
     * @return
     */
    private void copyPhototoPath(String oldPath, String newPath) {
        File file = new File(oldPath);
        String name = file.getName(); //获取文件名
        File newFile = new File(newPath, name);
        try {
            if (!newFile.exists()) {
                newFile.createNewFile();
            }
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(newFile);
            int n = 0;
            byte[] bb = new byte[1024];
            while ((n = in.read(bb)) != -1) {
                out.write(bb, 0, n);
            }
            out.close();// 关闭输入输出流
            in.close();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void openZxing() {
        // 打开扫描界面扫描条形码或二维码
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES); //设置扫描的类型
        intentIntegrator.setOrientationLocked(false);  //方向锁定
        intentIntegrator.setCaptureActivity(PortraitZxingActivity.class);
        intentIntegrator.setCameraId(0); //前置相机还是后置相机
        intentIntegrator.setBeepEnabled(false); //是否发出成功的声音
        intentIntegrator.setBarcodeImageEnabled(true);
        intentIntegrator.initiateScan();
    }

}
