package com.gyr.disvisibledemo.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gyr.disvisibledemo.R;
import com.gyr.disvisibledemo.bean.FloorModel;
import com.gyr.disvisibledemo.bean.SiteModel;

import java.util.ArrayList;
import java.util.List;

public class RvGroupAdapter extends RecyclerView.Adapter<RvGroupAdapter.MyViewHolder>{

    private List<SiteModel> siteList;
    private Context mContext;
    private RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener;


    public RvGroupAdapter(List<SiteModel> siteList, Context mContext,
                          RvMemberAdapter.OnMemberItemClickListener onMemberItemClickListener) {
        this.siteList = siteList;
        this.mContext = mContext;
        this.onMemberItemClickListener=onMemberItemClickListener;
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
        holder.tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.rv_member.getVisibility()==View.VISIBLE){
                    holder.rv_member.setVisibility(View.GONE);
                    holder.tv1.setText("展开");
                }else{
                    holder.rv_member.setVisibility(View.VISIBLE);
                    holder.tv1.setText("收缩");
                    if(holder.itemView.getParent() instanceof RecyclerView){
                        ((RecyclerView)holder.itemView.getParent()).scrollToPosition(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }


    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tv1,site,export,merge,delete;
        RecyclerView rv_member;
         RvMemberAdapter mRvMemberAdapter;
         List<FloorModel> floorList=new ArrayList<>();
        public MyViewHolder(View view) {
            super(view);
            tv1 = view.findViewById(R.id.group_tv1);
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


}
