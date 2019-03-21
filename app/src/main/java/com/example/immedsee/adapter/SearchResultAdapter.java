package com.example.immedsee.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.example.immedsee.R;

import java.util.List;

/**
 * DoubleWay on 2019/3/21:15:47
 * 邮箱：13558965844@163.com
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private Context mContext;
        private List<PoiInfo> mPoiInfo;


    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView resultName;
        TextView resultAddress;

        public ViewHolder(View view){
            super(view);
            cardView=(CardView)view;
            resultName=(TextView)view.findViewById(R.id.search_result_name);
            resultAddress=(TextView)view.findViewById(R.id.search_result_address);
        }
    }

    public SearchResultAdapter(List<PoiInfo> PoiInfo){
        mPoiInfo=PoiInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
               if(mContext==null){
                   mContext=parent.getContext();
               }
               View view= LayoutInflater.from(mContext).inflate(R.layout.search_result_item,parent,false);

            return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PoiInfo info=mPoiInfo.get(position);
        holder.resultName.setText(info.getName());
        holder.resultAddress.setText(info.getAddress());
    }

    @Override
    public int getItemCount() {
        return mPoiInfo.size();
    }


}
