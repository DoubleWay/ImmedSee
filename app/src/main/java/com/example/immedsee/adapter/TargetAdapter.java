package com.example.immedsee.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.immedsee.R;
import com.example.immedsee.entity.Target;

import java.util.List;

/**
 * DoubleWay on 2019/2/25:16:35
 * 邮箱：13558965844@163.com
 */
public class TargetAdapter extends RecyclerView.Adapter<TargetAdapter.ViewHolder> {
    private Context mContext;
    private List<Target> targetList;


    static  class ViewHolder extends  RecyclerView.ViewHolder{
          CardView cardView;
          TextView targetName;
          TextView targetContent;
        public ViewHolder(View itemView) {
            super(itemView);
            cardView=(CardView)itemView;
            targetName=(TextView)itemView.findViewById(R.id.target_name);
            targetContent=(TextView)itemView.findViewById(R.id.target_content);
        }
    }

    public TargetAdapter(List<Target> targetList) {
        this.targetList = targetList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.target_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Target target=targetList.get(position);
        holder.targetName.setText(target.getName());
        holder.targetContent.setText(target.getContent());
    }

    @Override
    public int getItemCount() {
        return targetList.size();
    }

}
