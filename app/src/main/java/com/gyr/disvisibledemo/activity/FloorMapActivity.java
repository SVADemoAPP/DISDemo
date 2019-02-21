package com.gyr.disvisibledemo.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.util.Constant;

import net.yoojia.imagemap.ImageMap1;
import net.yoojia.imagemap.TouchImageView1;
import net.yoojia.imagemap.core.CollectPointShape;
import net.yoojia.imagemap.core.MoniPointShape;
import net.yoojia.imagemap.core.PushMessageShape;
import net.yoojia.imagemap.core.Shape;
import net.yoojia.imagemap.core.ShapeExtension;
import net.yoojia.imagemap.core.SpecialShape;
import net.yoojia.imagemap.core.pRRUInfoShape;

import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Random;

public class FloorMapActivity extends BaseActivity {
    @ViewInject(R.id.imagemap)
    private ImageMap1 map;
    private String mFloorMap;
    private int mWidth,mHeight;
    private Random mRandom=new Random();
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
        int prruX=mRandom.nextInt(mWidth);
        int prruY=mRandom.nextInt(mHeight);
        pRRUInfoShape pRRUInfoShape=new pRRUInfoShape("test",Color.YELLOW,this);
        pRRUInfoShape.setText("长按测试");
        pRRUInfoShape.setValues(prruX, prruY);
        map.addShape(pRRUInfoShape,false);

        map.setOnShapeClickListener(new ShapeExtension.OnShapeActionListener() {
            @Override
            public void onCollectShapeClick(CollectPointShape collectPointShape, float f, float f2) {

            }

            @Override
            public void onMoniShapeClick(MoniPointShape moniPointShape, float f, float f2) {

            }

            @Override
            public void onPrruInfoShapeClick(pRRUInfoShape prruinfoshape, float f, float f2) {
                showToast("单击:"+prruinfoshape.getTag());
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
    public void initView() {

    }

    @Override
    public void dealLogicAfterInitView() {

    }
}
