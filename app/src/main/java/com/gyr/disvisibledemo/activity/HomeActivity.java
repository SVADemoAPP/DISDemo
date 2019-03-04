package com.gyr.disvisibledemo.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.zaaach.toprightmenu.MenuItem;
import com.zaaach.toprightmenu.TopRightMenu;

import org.xutils.x;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private LinearLayout mllTop;

    private RvGroupAdapter mRvGroupAdapter;
    private List<SiteModel> siteList = new ArrayList<>();
    private SuperPopupWindow mSearchWindow;

    private RecyclerView mSearchRv;
    private RvSearchAdapter mRvSearchAdaper;
    private List<String> mSearchList = new ArrayList<>();
    private String mSearchStr = "";
    private LinearLayout mllAdd;     //右上角添加按钮
    private LinearLayout mllSearch; //搜索占位框
    private EditText mEdtSearch;   //搜索框
    private TextView mTvSCancel;  //搜索取消按钮
    private LinearLayout mllSearchReal; //搜索框
    private RelativeLayout mRlSearch;
    private Context mContext;
    private TopRightMenu mTopRightMenu;


    @Override
    public void findView() {
        mRecyclerView = findViewById(R.id.rv_group);
        mllTop = findViewById(R.id.ll_top);
        mllAdd = findViewById(R.id.tool_right_add);
        mllSearch = findViewById(R.id.ll_search);
        mEdtSearch = findViewById(R.id.edt_search);
        mTvSCancel = findViewById(R.id.search_cancel);
        mllSearchReal = findViewById(R.id.ll_search_rl);
        mRlSearch = findViewById(R.id.rl_search_pop);
        mSearchRv = findViewById(R.id.search_rv);

        mllAdd.setOnClickListener(this);
        mllTop.setOnClickListener(this);
        mllSearch.setOnClickListener(this);
        mTvSCancel.setOnClickListener(this);
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //监听搜索框的输入变化完成之后的逻辑
                mSearchStr = s.toString();
                int len = mSearchStr.length();
                if (len == 0) {
                    mSearchList.clear();
                } else {
                    mSearchList.clear();
                    for (int i = 0; i < len; i++) {
                        mSearchList.add(mSearchStr + "_" + i);
                    }

                }
                mRvSearchAdaper.notifyDataSetChanged();
            }

        });
    }

    @Override
    public void setContentLayout() {
        mContext = this;
        setContentView(R.layout.activity_home);
        x.view().inject(this);
    }


    private void initFloorMapsDir() {
        File dir = new File(Constant.SD_PATH + "/data/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photosDir = new File(Constant.SD_PATH + "/photos/");
        if (!photosDir.exists()) {
            photosDir.mkdirs();
        }
        /*if (SharedPrefHelper.getBoolean(this, "isFirst", true)) {
            SharedPrefHelper.putBoolean(this, "isFirst", false);
            for (int i = 0; i < 5; i++) {
                File mapFile = new File(Constant.SD_PATH + "/maps/floor_" + i + ".png");
                if (!mapFile.exists()) {
                    try {
                        mapFile.createNewFile();
                        FileUtils.writeBytesToFile(this.getAssets().open("floor_" + i + ".png"), mapFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }*/
    }

    private void initSiteAndFloor() {
        File dataFile = new File(Constant.DATA_PATH);
        List<File> fileList  = Arrays.asList(dataFile.listFiles());
        for (File file  : fileList){
            if(!file.isFile()){
                SiteModel siteModel = new SiteModel();
                siteModel.siteName = file.getName();
                for(File subFiles : Arrays.asList(file.listFiles())){
                    String fileType = subFiles.getName().toUpperCase().substring(subFiles.getName().lastIndexOf("."));
                    if(subFiles.isFile() && Constant.IMGFILE.contains(fileType)){
                        FloorModel floorModel = new FloorModel();
                        floorModel.floorName = subFiles.getName().substring(0,subFiles.getName().lastIndexOf("."));
                        floorModel.floorMap = floorModel.floorName;
                        siteModel.floorModelList.add(floorModel);
                    }

                }

            }

        }
    }


    @Override
    public void dealLogicBeforeInitView() {
        initFloorMapsDir();
        initSiteAndFloor();
    }

    @Override
    public void initView() {
//        tv_confirm.getPaint().setFlags(Paint. UNDERLINE_TEXT_FLAG);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemViewCacheSize(siteList.size());
        mRvGroupAdapter = new RvGroupAdapter(siteList, this, onMemberItemClickListener);
        mRvGroupAdapter.setOnGroupItemClickListener(onGroupItemClickListener);
        mRecyclerView.setAdapter(mRvGroupAdapter);
        initSearchPop();
    }

    @Override
    public void dealLogicAfterInitView() {
        showTopRightMenu();

    }

    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener = new RvMemberAdapter.OnMemberItemClickListener() {
        @Override
        public void memberClick(String floorMap, int type) {
//            showToast(floorMap);
            Bundle bundle = new Bundle();
            bundle.putString("floormap", floorMap);
            openActivity(FloorMapActivity.class, bundle);
//
//            String ss= (String) HomeActivity.this.getIntent().getExtras().get("map");
        }
    };

    private void initSearchPop() {
        mSearchRv.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        mRvSearchAdaper = new RvSearchAdapter(mSearchList, HomeActivity.this);
        mRvSearchAdaper.setOnSearchItemClickListener(new RvSearchAdapter.OnSearchItemClickListener() {
            @Override
            public void searchClick(String str) {
                showToast("搜索单击回调：" + str);
            }
        });
        mSearchRv.setAdapter(mRvSearchAdaper);
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
                    }else{
                        shareFile(siteName);
                    }
                    break;
                case 1:
                    break;
                case 2:
                    String path = Constant.DATA_PATH + File.separator + siteName;
                    FileUtils.deleteDir(new File(path));
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tool_right_add:
                mTopRightMenu.showAsDropDown(mllAdd, -225, 0);    //带偏移量
                break;
            case R.id.ll_search:
                setSearch(true); //显示搜索
//                showSearchPop();
                break;
            case R.id.search_cancel:
                mEdtSearch.setText("");
                setSearch(false); //显示搜索占位View
                break;
//                case R.id:
//            break;
            default:
                break;
        }
    }

    /**
     * 搜索框与占位View的切换
     *
     * @param flag
     */
    private void setSearch(boolean flag) {
        if (flag)  //显示搜索
        {
            mllSearch.setVisibility(View.GONE);
            mllSearchReal.setVisibility(View.VISIBLE);
            showSoftInputFromWindow(mEdtSearch);
            mRlSearch.setVisibility(View.VISIBLE);
        } else {    //显示占位View
            mllSearch.setVisibility(View.VISIBLE);
            mllSearchReal.setVisibility(View.GONE);
            hideSoftInputFromWindow(mEdtSearch);
            mRlSearch.setVisibility(View.GONE);
        }
    }


    /**
     * 显示键盘
     *
     * @param editText
     */
    private void showSoftInputFromWindow(EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(editText, 0);
    }

    /**
     * 隐藏键盘
     *
     * @param editText
     */
    private void hideSoftInputFromWindow(EditText editText) {
        editText.setFocusable(false);
        editText.setFocusableInTouchMode(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 初始化topRightmenu
     */
    private void showTopRightMenu() {
        mTopRightMenu = new TopRightMenu((Activity) mContext);
        //添加菜单项
        List<MenuItem> mMenuItems = new ArrayList<>();
        mMenuItems.add(new MenuItem("从蓝牙目录查找"));
        mMenuItems.add(new MenuItem("从其他目录查找"));
        mTopRightMenu
                .setHeight(264)     //默认高度480
                .setWidth(350)      //默认宽度wrap_content
                .showIcon(false)     //显示菜单图标，默认为true
                .dimBackground(true)        //背景变暗，默认为true
                .needAnimationStyle(true)   //显示动画，默认为true
                .setAnimationStyle(R.style.TRM_ANIM_STYLE)
                .addMenuList(mMenuItems)
                .setOnMenuItemClickListener(new TopRightMenu.OnMenuItemClickListener() {
                    @Override
                    public void onMenuItemClick(int position) {
                        switch (position)
                        {
                            case 0: //蓝牙目录
                                Toast.makeText(mContext, "蓝牙" + position, Toast.LENGTH_SHORT).show();
                                break;
                            case 1: //其他目录
                                Toast.makeText(mContext, "其他" + position, Toast.LENGTH_SHORT).show();
                                break;
                        }

                    }
                });

    }
}