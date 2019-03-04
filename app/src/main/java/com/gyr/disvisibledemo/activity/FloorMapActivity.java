package com.gyr.disvisibledemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.util.Constant;
import com.journeyapps.barcodescanner.CaptureActivity;

import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.Bubble;
import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.SpecialShape;
import net.yoojia.imagemap.core.pRRUInfoShape;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class FloorMapActivity extends BaseActivity implements View.OnClickListener {
    private static final int CODE_OPEN_CAMERA = 1;
    private static final int CODE_OPEN_ZXING_REQUEST = 10001;
    private static final String IMAGE_ROOT_PATH = Environment.getExternalStorageState() + File.separator + "Tester";//todo
    private pRRUInfoShape mNowSelectPrru;
    private String mFloorMapName; //选择的地图名称
    private int mWidth;
    private int mHeight;
    private Random mRandom = new Random();

    private Context mContext;

    private View mMenuView;
    private LinearLayout mMenuBind;
    private LinearLayout mMenuMove;
    private LinearLayout mMenuCamera;
    private LinearLayout mMenuUnBind;
    private TextView mToolName;
    private LinearLayout mBack;
    private LinearLayout mAdd;
    private ImageMap1 mFloorMap; //地图

    public final int TYPE_TAKE_PHOTO = 1;//Uri获取类型判断
    private static final int CODE_TAKE_PHOTO = 1;// 拍照
    private static final int CODE_SHORT_VIDEO = 2;// 短视频
    public final int NEED_CAMERA = 0;
    private String imgPath;//图片路径
    private Uri photoUri;
    private File ffile;
    private String mFileUrl;

    @Override
    public void findView() {
        mFloorMap = findViewById(R.id.imagemap);
        mBack = findViewById(R.id.back);
        mToolName = findViewById(R.id.tool_top_name);
        mAdd = findViewById(R.id.tool_right_add);
        mAdd.setVisibility(View.GONE);
        mBack.setVisibility(View.VISIBLE);
        mBack.setOnClickListener(this);
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_floor_map);
    }

    @Override
    public void dealLogicBeforeInitView() {
        mFloorMapName = (String) getIntent().getExtras().get("floormap");
        char num = mFloorMapName.charAt(mFloorMapName.length() - 1);
        Bitmap bitmap = BitmapFactory.decodeFile(Constant.sdPath + "/maps/floor_" + num + ".png");
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mFloorMap.setMapBitmap(bitmap);
        int prruX1 = mRandom.nextInt(mWidth);
        int prruY1 = mRandom.nextInt(mHeight);
        pRRUInfoShape pRRUInfoShape1 = new pRRUInfoShape("test1", Color.YELLOW, this);
        pRRUInfoShape1.setValues(prruX1, prruY1);
        pRRUInfoShape1.setBind(false);
        pRRUInfoShape1.setPrruShowType(pRRUInfoShape.pRRUType.outArea);
        mFloorMap.addShape(pRRUInfoShape1, false);
        int prruX2 = mRandom.nextInt(mWidth);
        int prruY2 = mRandom.nextInt(mHeight);
        pRRUInfoShape pRRUInfoShape2 = new pRRUInfoShape("test2", Color.YELLOW, this);
        pRRUInfoShape2.setValues(prruX2, prruY2);
        pRRUInfoShape2.setBind(true);
        pRRUInfoShape2.setPrruShowType(pRRUInfoShape.pRRUType.inArea);
        mFloorMap.addShape(pRRUInfoShape2, false);

    }

    private View.OnClickListener onMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_bind:
                    if (mNowSelectPrru != null) {
                        mNowSelectPrru.setBind(true);
                        mNowSelectPrru.setPrruShowType(pRRUInfoShape.pRRUType.inArea);
                    }
                    openZxing();
                    break;
                case R.id.menu_unbind:
                    if (mNowSelectPrru != null) {
                        mNowSelectPrru.setBind(false);
                        mNowSelectPrru.setPrruShowType(pRRUInfoShape.pRRUType.outArea);

                    }
                    openZxing();
                    break;
                case R.id.menu_move:

                    break;
                case R.id.menu_camera:
                    openCamera();
                    break;
                default:
                    break;
            }
        }
    };


    private void openSysCamera(String photoName) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                new File(Constant.sdPath + "/photos", photoName)));
        startActivityForResult(cameraIntent, CODE_OPEN_CAMERA);
    }


    @Override
    public void initView() {
        mToolName.setText("pRRU");
        mMenuView = View.inflate(this, R.layout.prru_menu_layout, null);
        mMenuBind = mMenuView.findViewById(R.id.menu_bind);
        mMenuUnBind = mMenuView.findViewById(R.id.menu_unbind);
        mMenuMove = mMenuView.findViewById(R.id.menu_move);
        mMenuCamera = mMenuView.findViewById(R.id.menu_camera);
        mMenuBind.setOnClickListener(onMenuClickListener);
        mMenuUnBind.setOnClickListener(onMenuClickListener);
        mMenuMove.setOnClickListener(onMenuClickListener);
        mMenuCamera.setOnClickListener(onMenuClickListener);

        mFloorMap.setBubbleView(mMenuView, new Bubble.RenderDelegate() {
            @Override
            public void onDisplay(Shape shape, View bubbleView) {
                //                Log.e("msg",shape.toString());
                if (shape instanceof pRRUInfoShape) {
                    if (((pRRUInfoShape) shape).isBind()) {
                        mMenuBind.setVisibility(View.VISIBLE);
                        mMenuUnBind.setVisibility(View.GONE);
                    } else {
                        mMenuUnBind.setVisibility(View.VISIBLE);
                        mMenuBind.setVisibility(View.GONE);
                    }
                }
            }
        });
//        mMenuView.setVisibility(View.VISIBLE);
        mFloorMap.setOnShapeClickListener(new ShapeExtension.OnShapeActionListener() {
            @Override
            public void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2) {

            }

            @Override
            public void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2) {

            }

            @Override
            public void onPrruInfoShapeClick(pRRUInfoShape prruinfoshape, float f, float f2) {
//                showToast("单击:"+prruinfoshape.getTag());
                mNowSelectPrru = prruinfoshape;
            }

            @Override
            public void onPushMessageShapeClick(PushMessageShape pushMessageShape, float f, float f2) {

            }

            @Override
            public void onSpecialShapeClick(SpecialShape specialShape, float f, float f2) {

            }

            @Override
            public void outShapeClick(float f, float f2) {

            }
        });

        mFloorMap.setOnLongClickListener1(new TouchImageView1.OnLongClickListener1() {
            @Override
            public void onLongClick(Shape shape) {
                if (shape instanceof pRRUInfoShape) {
                    showToast("长按:" + ((pRRUInfoShape) shape).getTag());
                }

            }
        });
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
                copyPhototoPath(imgPath, IMAGE_ROOT_PATH);
                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
        }
    }

    private void openZxing() {
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

}
