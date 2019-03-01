package com.gyr.disvisibledemo.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.adapter.RvGroupAdapter;
import com.gyr.disvisibledemo.adapter.RvMemberAdapter;
import com.gyr.disvisibledemo.adapter.RvSearchAdapter;
import com.gyr.disvisibledemo.bean.FloorModel;
import com.gyr.disvisibledemo.bean.SiteModel;
import com.gyr.disvisibledemo.framework.activity.BaseActivity;
import com.gyr.disvisibledemo.framework.sharef.SharedPrefHelper;
import com.gyr.disvisibledemo.util.BlueUntils;
import com.gyr.disvisibledemo.util.Constant;
import com.gyr.disvisibledemo.util.FileUtils;
import com.gyr.disvisibledemo.util.ZipUtils;
import com.gyr.disvisibledemo.view.popup.SuperPopupWindow;

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
    @ViewInject(R.id.et_search)
    private EditText et_search;
    @ViewInject(R.id.rv_group)
    private RecyclerView mRecyclerView;
    @ViewInject(R.id.ll_top)
    private LinearLayout ll_top;

    private RvGroupAdapter mRvGroupAdapter;
    private List<SiteModel> siteList=new ArrayList<>();
    private SuperPopupWindow mSearchWindow;

    private RecyclerView search_rv;
    private RvSearchAdapter mRvSearchAdaper;
    private List<String> mSearchList=new ArrayList<>();
    private String mSearchStr="";

    @Override
    public void setContentLayout() {
        setContentView(R.layout.activity_home);
        x.view().inject(this);
    }

    private void initFloorMapsDir(){
        File dir = new File(Constant.SD_PATH + "/maps/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photosDir = new File(Constant.SD_PATH + "/photos/");
        if (!photosDir.exists()) {
            photosDir.mkdirs();
        }
        if(SharedPrefHelper.getBoolean(this,"isFirst",true)) {
            SharedPrefHelper.putBoolean(this,"isFirst",false);
            for(int i=0;i<5;i++){
                File mapFile = new File(Constant.SD_PATH + "/maps/floor_"+i+".png");
                if (!mapFile.exists()) {
                    try {
                        mapFile.createNewFile();
                        FileUtils.writeBytesToFile(this.getAssets().open("floor_"+i+".png"), mapFile);
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
        mRvGroupAdapter.setOnGroupItemClickListener(onGroupItemClickListener);
        mRecyclerView.setAdapter(mRvGroupAdapter);
        et_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //监听搜索框的输入变化完成之后的逻辑
                mSearchStr=s.toString();
                int len=mSearchStr.length();
                if(len==0){
                    hideSearchPop();
                }else {
                    mSearchList.clear();
                    for (int i = 0; i < len; i++) {
                        mSearchList.add(mSearchStr+"_"+i);
                    }
                    showSearchPop();
                    mRvSearchAdaper.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void dealLogicAfterInitView() {


    }

    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener = new RvMemberAdapter.OnMemberItemClickListener() {
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

    private void showSearchPop() {

        if (mSearchWindow == null) {
            mSearchWindow = new SuperPopupWindow(this, R.layout.popup_search_layout, new SuperPopupWindow.ViewListener() {
                @Override
                public void getViewOfPop(View view) {
                    search_rv=view.findViewById(R.id.search_rv);
                    search_rv.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
                    mRvSearchAdaper=new RvSearchAdapter(mSearchList,HomeActivity.this);
                    mRvSearchAdaper.setOnSearchItemClickListener(new RvSearchAdapter.OnSearchItemClickListener() {
                        @Override
                        public void searchClick(String str) {
                            showToast("搜索单击回调："+str);
                        }
                    });
                    search_rv.setAdapter(mRvSearchAdaper);
                }
            });
            mSearchWindow.setWidthAndHeight(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            mSearchWindow.setOutsideTouchable(false);
        }
        mSearchWindow.showPopupWindowAsDropDown(ll_top);
    }

    private void hideSearchPop() {
        if(mSearchWindow!=null){
            mSearchWindow.hidePopupWindow();
        }
    }
    private RvGroupAdapter.OnGroupItemClickListener onGroupItemClickListener = new RvGroupAdapter.OnGroupItemClickListener() {
        @Override
        public void groupClick(String siteName, int type) {
            switch (type){
                case 0:
                    if(!BlueUntils.isBluetoothAvaliable()){
                        showToast("本机没有找到蓝牙硬件，无法使用蓝牙功能！");
                    }
                    BluetoothAdapter mAdapter = BlueUntils.getBluetoothAdapter();

                    if(!mAdapter.isEnabled()){
                        //弹出对话框提示用户是后打开
                        Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        enabler.putExtra("siteName",siteName);
                        startActivityForResult(enabler,0);
                        //不做提示，强行打开，此方法需要权限<uses-permissionandroid:name="android.permission.BLUETOOTH_ADMIN" />
                        // mAdapter.enable();
                    }else{
                        shareFile(siteName);
                    }
                    break;
                case 1:
                    break;
                case 2:
                    break;
                default:
                    showToast("未知的点击操作" + type);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == 0){
            String siteName = data.getStringExtra("siteName");
            shareFile(siteName);
        }

    }

    @Event(type = View.OnClickListener.class,value = {R.id.tv_confirm})
    private void xClick(View v){
        switch (v.getId()){
            case R.id.tv_confirm:
                showToast("点击确定,搜索值："+mSearchStr);
                break;
            default:
                break;
        }
    }

    private void shareFile(String siteName){

        String basePath = Constant.SD_PATH + File.separator + "data" + File.separator;
        String path = basePath + siteName + ".zip";
        try {
            ZipUtils.zipDirectory(basePath+siteName);
        }catch (IOException e){
            showToast("文件压缩失败");
        }

        //调用android分享窗口
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.setPackage("com.android.bluetooth");
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(path)));//path为文件的路径
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooser = Intent.createChooser(intent, "Share app");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooser);
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
