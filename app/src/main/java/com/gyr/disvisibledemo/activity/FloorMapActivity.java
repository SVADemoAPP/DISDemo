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
import com.gyr.disvisibledemo.framework.utils.StringUtil;
import com.gyr.disvisibledemo.util.Constant;
import com.gyr.disvisibledemo.util.XmlUntils;

import net.yoojia.imagemap.HighlightImageView1;
import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.Bubble;
import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PrruInfoShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.SpecialShape;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class FloorMapActivity extends BaseActivity implements View.OnClickListener {
    private static final int CODE_OPEN_CAMERA = 1;
    private static final String IMAGE_ROOT_PATH = Environment.getExternalStorageState() + File.separator + "Tester";
    private PrruInfoShape mNowSelectPrru;
    private Context mContext;
    private boolean mFirst = false;
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
    private String imgPath;//图片路径
    private Uri photoUri;
    private Bitmap mBitmap;
    private String mapPath;
    private PrruInfoShape tempPrruInfoShape;
    private PrruInfoShape redPrruInfoShape;
    private String[] mPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private String siteName;
    private String floorName;

    //    private boolean  mShowBubble=true; //默认
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (!mFirst) {
                mFirst = true;
                getData();
            }

        }
    }

    @Override
    public void dealLogicBeforeInitView() {
        mapPath = (String) getIntent().getExtras().get("floormap");
        mBitmap = BitmapFactory.decodeFile(Constant.DATA_PATH + File.separator + mapPath);
        mFloorMap.setMapBitmap(mBitmap);
        mFloorMap.setAllowRotate(false); //不能转动
        mFloorMap.setPrruListener(new HighlightImageView1.PrruModifyHListener() {   //监听地图上prru移动事件
            @Override
            public void startTranslate(PrruInfoShape shape, float x, float y) {
                if (tempPrruInfoShape != null) {
                    Log.e("XHF_start", "x=" + x + "-----y=" + y);
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                    //开始移动prru时禁止移动地图
                    mFloorMap.setAllowTranslate(false);
                }

            }

            @Override
            public void moveTranslate(PrruInfoShape shape, float x, float y) {
                Log.e("XHF_move", "x=" + x + "-----y=" + y);
                if (tempPrruInfoShape != null) {
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                }
            }

            @Override
            public void endTranslate(PrruInfoShape shape, float x, float y) {
                Log.e("XHF_end", "x=" + x + "-----y=" + y);
                if (tempPrruInfoShape != null) {
                    if (!shape.getMove()) {
                        return;
                    }
                    redPrruInfoShape.setValues(x, y);
                    showNormalDialog("pRRU位置修改", "确定本次修改？");
                }
                //prru移动结束后开放移动地图
                mFloorMap.setAllowTranslate(true);

            }

            @Override
            public void clickBlank() {


            }

            @Override
            public void clickOutSide() {  //判断是点击除开prru的外部
                mFloorMap.getBubble().setVisibility(View.GONE);
                if (tempPrruInfoShape != null) { //只有在调整事件触发的时候才有  点击空的没有prrushape的位置
                    mFloorMap.removeShape("temp"); //移除红色
                    mFloorMap.addShape(tempPrruInfoShape, false);//还原
                    tempPrruInfoShape = null;
                    showToast("已取消调整");
                    mFloorMap.setShowBubble(true);
                }
            }
        });
    }

    private void getData() {
        siteName = mapPath.substring(0, mapPath.indexOf(File.separator));
        floorName = mapPath.substring(mapPath.indexOf(File.separator) + 1, mapPath.indexOf("."));
        Document document = XmlUntils.getDocument(Constant.DATA_PATH + File.separator + siteName + File.separator + "project.xml");
        Element rootElement = XmlUntils.getRootElement(document);
        Element floors = XmlUntils.getElementByName(rootElement, "Floors");
        List<Element> floorList = XmlUntils.getElementListByName(floors, "Floor");
        for (Element element : floorList) {
            //如果是同一楼层
            if (floorName.equals(XmlUntils.getAttributeValueByName(element, "floorCode"))) {
                List<Element> nes = XmlUntils.getElementListByName(XmlUntils.getElementByName(element, "NEs"), "NE");
                for (Element ne : nes) {
                    PrruInfoShape prruInfoShape = new PrruInfoShape(XmlUntils.getAttributeValueByName(ne, "id"), Color.YELLOW, this);
                    prruInfoShape.setId(XmlUntils.getAttributeValueByName(ne, "id"));
                    prruInfoShape.setValues(Float.parseFloat(XmlUntils.getAttributeValueByName(ne, "x")), Float.parseFloat(XmlUntils.getAttributeValueByName(ne, "y")));
                    prruInfoShape.setBind(false);
                    prruInfoShape.setMove(false);
                    prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                    if (StringUtil.isNullOrEmpty(XmlUntils.getAttributeValueByName(ne, "esn"))) {
                        prruInfoShape.setBind(false);
                        prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                    } else {
                        prruInfoShape.setBind(true);
                        prruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.inArea);
                    }

                    mFloorMap.addShape(prruInfoShape, false);

                }
                break;
            }
        }
    }

    private View.OnClickListener onMenuClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_bind:
                    if (mNowSelectPrru == null) {
                        return;
                    }
                    getOtherRxPermission(mPermission, new PerMissonListener() { //使用前  先判断权限有没有开启


                        @Override
                        public void havePermission() {
                            openZxing();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });

                    break;
                case R.id.menu_unbind:
                    if (mNowSelectPrru != null) {
                        mNowSelectPrru.setBind(false);
                        mNowSelectPrru.setPrruShowType(PrruInfoShape.pRRUType.outArea);
                    }
                    getOtherRxPermission(mPermission, new PerMissonListener() { //使用前  先判断权限有没有开启

                        @Override
                        public void havePermission() {
                            //openZxing();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });
                    break;
                case R.id.menu_move:
                    //TODO 增加选择移动原因的窗口
                    float centerX = mNowSelectPrru.getCenterX();  //获取中心点xy
                    float centerY = mNowSelectPrru.getCenterY();
                    redPrruInfoShape.setValues(centerX, centerY);
                    redPrruInfoShape.setMove(true);
                    mFloorMap.addShape(redPrruInfoShape, false);
                    mMenuView.setVisibility(View.GONE);
                    tempPrruInfoShape = mNowSelectPrru;
                    mFloorMap.removeShape(mNowSelectPrru.getTag());
                    mFloorMap.setShowBubble(false);
                    showToast("请长按红色pRRU拖动到移动的位置");
                    break;
                case R.id.menu_camera:
                    //做权限判断
                    getOtherRxPermission(mPermission, new PerMissonListener() { //使用前  先判断权限有没有开启

                        @Override
                        public void havePermission() {
                            openCamera();
                        }

                        @Override
                        public void missPermission() {
                            showToast("请在权限管理中打开权限");
                        }
                    });

                    break;
                default:
                    break;
            }
        }
    };


    private void openSysCamera(String photoName) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                new File(Constant.SD_PATH + "/photos", photoName)));
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
                if (shape instanceof PrruInfoShape) {
                    if (((PrruInfoShape) shape).isBind()) {
                        mMenuUnBind.setVisibility(View.VISIBLE);
                        mMenuBind.setVisibility(View.GONE);
                    } else {
                        mMenuBind.setVisibility(View.VISIBLE);
                        mMenuUnBind.setVisibility(View.GONE);

                    }
                }
            }
        });
        mFloorMap.setOnShapeClickListener(new ShapeExtension.OnShapeActionListener() {
            @Override
            public void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2) {

            }

            @Override
            public void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2) {

            }

            @Override
            public void onPrruInfoShapeClick(PrruInfoShape prruinfoshape, float f, float f2) {
//                showToast("单击:" + prruinfoshape.getTag());
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
                if (shape instanceof PrruInfoShape) {
//                    showToast("长按:" + ((PrruInfoShape) shape).getTag());
                }

            }
        });
    }

    @Override
    public void dealLogicAfterInitView() {
        redPrruInfoShape = new PrruInfoShape("temp", Color.GREEN, mContext);
        redPrruInfoShape.setPrruShowType(PrruInfoShape.pRRUType.temple);
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
                    writeToXml(result.getContents());
                    mNowSelectPrru.setBind(true);
                    mNowSelectPrru.setPrruShowType(PrruInfoShape.pRRUType.inArea);
                    Toast.makeText(this, "绑定成功: " + result.getContents(), Toast.LENGTH_LONG).show();
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

    public void writeToXml(String esn) {
        String xmlFilePath = Constant.DATA_PATH + File.separator + siteName + File.separator + "project.xml";
        Document document = XmlUntils.getDocument(xmlFilePath);
        Element rootElement = XmlUntils.getRootElement(document);
        Element floors = XmlUntils.getElementByName(rootElement, "Floors");
        List<Element> floorList = XmlUntils.getElementListByName(floors, "Floor");
        for (Element element : floorList) {
            //如果是同一楼层
            if (floorName.equals(XmlUntils.getAttributeValueByName(element, "floorCode"))) {
                List<Element> nes = XmlUntils.getElementListByName(XmlUntils.getElementByName(element, "NEs"), "NE");
                for (Element ne : nes) {
                    if (XmlUntils.getAttributeValueByName(ne, "id").equals(mNowSelectPrru.getId())) {
                        Log.e("XHF", "");
                        XmlUntils.setAttributeValueByName(ne, "esn", esn);
                        XmlUntils.saveDocument(document, new File(xmlFilePath));
                        break;
                    }
                }
                break;
            }
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
        // 先判断是否已经回收
        if (mBitmap != null && !mBitmap.isRecycled()) {
            // 回收并且置为null
            mBitmap.recycle();
            mBitmap = null;
        }
    }


    private void showNormalDialog(String title, String message) {
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("pRRU位置修改成功");
                        tempPrruInfoShape.setValues(redPrruInfoShape.getCenterX(), redPrruInfoShape.getCenterY());
                        mFloorMap.removeShape("temp"); //移除红色
                        mFloorMap.addShape(tempPrruInfoShape, false);//还原
                        mFloorMap.setShowBubble(true);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("已取消修改");
                        mFloorMap.removeShape("temp"); //移除红色
                        mFloorMap.addShape(tempPrruInfoShape, false);//还原
                        tempPrruInfoShape = null;
                        mFloorMap.setShowBubble(true);
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void showChooseDialog(){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
        final String[] items = new String[] { "北京", "上海", "广州", "深圳" };
        normalDialog.setTitle("请选择移动原因");
        normalDialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(FloorMapActivity.this, items[which],
                        Toast.LENGTH_SHORT).show();
            }
        });
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("pRRU位置修改成功");
                        tempPrruInfoShape.setValues(redPrruInfoShape.getCenterX(), redPrruInfoShape.getCenterY());
                        mFloorMap.removeShape("temp"); //移除红色
                        mFloorMap.addShape(tempPrruInfoShape, false);//还原
                        mFloorMap.setShowBubble(true);
                    }
                });
        normalDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showToast("已取消修改");
                        mFloorMap.removeShape("temp"); //移除红色
                        mFloorMap.addShape(tempPrruInfoShape, false);//还原
                        tempPrruInfoShape = null;
                        mFloorMap.setShowBubble(true);
                    }
                });
        // 显示
        normalDialog.show();
    }


}
