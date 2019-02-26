package com.gyr.disvisibledemo.adapter;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.activity.HomeActivity;
import com.gyr.disvisibledemo.bean.FloorModel;
import com.gyr.disvisibledemo.bean.SiteModel;
import com.gyr.disvisibledemo.util.BlueUntil;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

public class RvGroupAdapter extends RecyclerView.Adapter<RvGroupAdapter.MyViewHolder>{

    private List<SiteModel> siteList;
    private Context mContext;
    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener;
    private OnGroupItemClickListener onGroupItemClickListener;

    public RvGroupAdapter(List<SiteModel> siteList, Context mContext,
                          RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener) {
        this.siteList = siteList;
        this.mContext = mContext;
        this.onMemberItemClickListener=onMemberItemClickListener;
    }

    public void setOnGroupItemClickListener(OnGroupItemClickListener onGroupItemClickListener) {
        this.onGroupItemClickListener = onGroupItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view=LayoutInflater.from(mContext)
                .inflate(R.layout.item_rv_group,viewGroup,false);
        MyViewHolder holder=new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.site.setText(siteList.get(position).siteName);
        holder.floorList.clear();
        holder.floorList.addAll(siteList.get(position).floorModelList);
        holder.mRvMemberAdapter.setOnMemberItemClickListener(onMemberItemClickListener);
        //展开按钮点击监听
        holder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.rv_member.getVisibility()==View.VISIBLE){
                    holder.rv_member.setVisibility(View.GONE);
//                    holder.tv1.setText("展开");
                    holder.img.setImageResource(R.mipmap.group_arrow_up);
                }else{
                    holder.rv_member.setVisibility(View.VISIBLE);
//                    holder.tv1.setText("收缩");
                    holder.img.setImageResource(R.mipmap.group_arrow_down);
                    if(holder.itemView.getParent() instanceof RecyclerView){
                        ((RecyclerView)holder.itemView.getParent()).scrollToPosition(position);
                    }
                }
            }
        });
        //导出按钮点击监听
        holder.export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGroupItemClickListener.groupClick(siteList.get(position).siteName,0);
            }
        });
        //导出按钮点击监听
        holder.merge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalDialog("合并","确定需要进行合并操作吗？");
            }
        });
        //删除按钮点击监听
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNormalDialog("刪除","确定要进行删除操作吗？");
            }
        });
    }


    private void showNormalDialog(String title, String message){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(mContext);
//        normalDialog.setIcon(R.drawable.icon_dialog);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                        Toast.makeText(mContext,"操作成功",Toast.LENGTH_SHORT).show();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView site,export,merge,delete;
        RecyclerView rv_member;
         RvMemberAdapter mRvMemberAdapter;
         List<FloorModel> floorList=new ArrayList<>();
        public MyViewHolder(View view) {
            super(view);
            img = view.findViewById(R.id.group_img);
            site=view.findViewById(R.id.group_site);
            export=view.findViewById(R.id.group_export);
            merge=view.findViewById(R.id.group_merge);
            delete=view.findViewById(R.id.group_delete);
            rv_member=view.findViewById(R.id.rv_member);
            rv_member.setLayoutManager(new LinearLayoutManager(mContext));
            mRvMemberAdapter=new RvMemberAdapter(floorList,mContext);
            rv_member.setAdapter(mRvMemberAdapter);
        }
    }
    public interface OnGroupItemClickListener{
        void groupClick(String siteName,int type);
    }

}
