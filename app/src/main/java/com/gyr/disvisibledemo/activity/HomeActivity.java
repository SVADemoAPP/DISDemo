package com.gyr.disvisibledemo.activity;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.adapter.RvGroupAdapter;
import com.gyr.disvisibledemo.adapter.RvMemberAdapter;
import com.gyr.disvisibledemo.bean.FloorModel;
import com.gyr.disvisibledemo.bean.SiteModel;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.framework.sharef.SharedPrefHelper;
import com.gyr.disvisibledemo.util.Constant;
import com.gyr.disvisibledemo.util.FileUtil;

import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseActivity {

    @ViewInject(R.id.tv_confirm)
    private TextView tv_confirm;
    @ViewInject(R.id.rv_group)
    private RecyclerView mRecyclerView;

    private RvGroupAdapter mRvGroupAdapter;
    private List<SiteModel> siteList=new ArrayList<>();
    @Override
    public void setContentLayout() {
        setContentView(R.layout.activity_home);
        x.view().inject(this);
    }

    private void initFloorMapsDir(){
        File dir = new File(Constant.sdPath + "/maps/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photosDir = new File(Constant.sdPath + "/photos/");
        if (!photosDir.exists()) {
            photosDir.mkdirs();
        }
        if(SharedPrefHelper.getBoolean(this,"isFirst",true)) {
            SharedPrefHelper.putBoolean(this,"isFirst",false);
            for(int i=0;i<5;i++){
                File mapFile = new File(Constant.sdPath + "/maps/floor_"+i+".png");
                if (!mapFile.exists()) {
                    try {
                        mapFile.createNewFile();
                        FileUtil.writeBytesToFile(this.getAssets().open("floor_"+i+".png"), mapFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    private void initSiteAndFloor(){
        for(int i=1;i<6;i++){
            SiteModel siteModel=new SiteModel();
            siteModel.siteName="U"+i;
            for(int j=1;j<5;j++){
                FloorModel floorModel=new FloorModel();
                floorModel.floorName="F"+"_"+j;
                floorModel.floorMap="map"+i+"_"+j;
                siteModel.floorModelList.add(floorModel);
            }
            siteList.add(siteModel);
        }
    }



    @Override
    public void dealLogicBeforeInitView() {
        initFloorMapsDir();
        initSiteAndFloor();
    }

    @Override
    public void initView() {
        tv_confirm.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemViewCacheSize(siteList.size());
        mRvGroupAdapter=new RvGroupAdapter(siteList,this,onMemberItemClickListener);
        mRecyclerView.setAdapter(mRvGroupAdapter);
    }

    @Override
    public void dealLogicAfterInitView() {

    }

    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener=new RvMemberAdapter.OnMemberItemClickListener() {
        @Override
        public void memberClick(String floorMap, int type) {
//            showToast(floorMap);
            Bundle bundle=new Bundle();
            bundle.putString("floormap",floorMap);
            openActivity(FloorMapActivity.class,bundle);
//
//            String ss= (String) HomeActivity.this.getIntent().getExtras().get("map");
        }
    };


    @Event(type = View.OnClickListener.class,value = {})
    private void xClick(View v){
        switch (v.getId()){
//            case R.id.home_tv1:
//                showToast("tv1单击");
//                break;
            default:
                break;
        }
    }

    @Event(type = View.OnLongClickListener.class,value = {})
    private boolean xLongClick(View v){
        switch (v.getId()){
//            case R.id.home_tv1:
//                showToast("tv1长按");
//                break;
            default:
                break;
        }
        return true; //返回true消费掉，避免再响应单击事件
    }
}
