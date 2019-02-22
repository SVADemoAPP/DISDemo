package com.gyr.disvisibledemo.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.util.Constant;

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
import java.util.Random;

public class FloorMapActivity extends BaseActivity {
    @ViewInject(R.id.imagemap)
    private ImageMap1 map;
    private String mFloorMap;
    private int mWidth,mHeight;
    private Random mRandom=new Random();
    private View mMenuView;
    private TextView menu_bind,menu_move,menu_camera;
    private pRRUInfoShape mNowSelectPrru;
    private final int CODE_OPEN_CAMERA=1;
    @Override
    public void setContentLayout() {
        setContentView(R.layout.activity_floor_map);
        x.view().inject(this);
    }

    @Override
    public void dealLogicBeforeInitView() {
        mFloorMap= (String) getIntent().getExtras().get("floormap");
        char num=mFloorMap.charAt(mFloorMap.length()-1);
        Bitmap bitmap = BitmapFactory.decodeFile(Constant.sdPath + "/maps/floor_" + num+".png");
        mWidth=bitmap.getWidth();
        mHeight=bitmap.getHeight();
        map.setMapBitmap(bitmap);
        int prruX1=mRandom.nextInt(mWidth);
        int prruY1=mRandom.nextInt(mHeight);
        pRRUInfoShape pRRUInfoShape1=new pRRUInfoShape("test1",Color.YELLOW,this);
        pRRUInfoShape1.setValues(prruX1, prruY1);
        pRRUInfoShape1.setBind(false);
        pRRUInfoShape1.setPrruShowType(pRRUInfoShape.pRRUType.outArea);
        map.addShape(pRRUInfoShape1,false);
        int prruX2=mRandom.nextInt(mWidth);
        int prruY2=mRandom.nextInt(mHeight);
        pRRUInfoShape pRRUInfoShape2=new pRRUInfoShape("test2",Color.YELLOW,this);
        pRRUInfoShape2.setValues(prruX2, prruY2);
        pRRUInfoShape2.setBind(true);
        pRRUInfoShape2.setPrruShowType(pRRUInfoShape.pRRUType.inArea);
        map.addShape(pRRUInfoShape2,false);

    }

    private View.OnClickListener onMenuClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.menu_bind:
                    if(mNowSelectPrru!=null){

                        if(mNowSelectPrru.isBind()){
                            mNowSelectPrru.setBind(false);
                            mNowSelectPrru.setPrruShowType(pRRUInfoShape.pRRUType.outArea);
                        }else{
                            mNowSelectPrru.setBind(true);
                            mNowSelectPrru.setPrruShowType(pRRUInfoShape.pRRUType.inArea);
                        }
                    }
                    break;
                case R.id.menu_move:
                    break;
                case R.id.menu_camera:
//                    openActivity(CameraActivity.class);
                    openSysCamera(mNowSelectPrru.getTag()+".png");
                    break;
                default:
                    break;
            }
        }
    };


    private void openSysCamera(String photoName) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(
                new File(Constant.sdPath+"/photos", photoName)));
        startActivityForResult(cameraIntent, CODE_OPEN_CAMERA);
    }
    @Override
    public void initView() {
        mMenuView=View.inflate(this,R.layout.prru_menu_layout,null);
        menu_bind=mMenuView.findViewById(R.id.menu_bind);
        menu_move=mMenuView.findViewById(R.id.menu_move);
        menu_camera=mMenuView.findViewById(R.id.menu_camera);
        menu_bind.setOnClickListener(onMenuClickListener);
        menu_move.setOnClickListener(onMenuClickListener);
        menu_camera.setOnClickListener(onMenuClickListener);
        map.setBubbleView(mMenuView, new Bubble.RenderDelegate() {
            @Override
            public void onDisplay(Shape shape, View bubbleView) {
//                Log.e("msg",shape.toString());
                if(shape instanceof pRRUInfoShape){
                    if(((pRRUInfoShape) shape).isBind()){
                        menu_bind.setText("解绑");
                    }else {
                        menu_bind.setText("绑定");
                    };
                }
            }
        });
//        mMenuView.setVisibility(View.VISIBLE);
        map.setOnShapeClickListener(new ShapeExtension.OnShapeActionListener() {
            @Override
            public void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2) {

            }

            @Override
            public void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2) {

            }

            @Override
            public void onPrruInfoShapeClick(pRRUInfoShape prruinfoshape, float f, float f2) {
//                showToast("单击:"+prruinfoshape.getTag());
                mNowSelectPrru=prruinfoshape;
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

        map.setOnLongClickListener1(new TouchImageView1.OnLongClickListener1() {
            @Override
            public void onLongClick(Shape shape) {
                if(shape instanceof pRRUInfoShape){
                    showToast("长按:"+((pRRUInfoShape)shape).getTag());
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
        switch (requestCode){
            case CODE_OPEN_CAMERA:
                showToast("拍照保存到："+Constant.sdPath+"/photos/"+mNowSelectPrru.getTag()+".png");
                break;
            default:
                break;
        }
    }

}
