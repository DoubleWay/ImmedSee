package com.example.immedsee.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.utils.DistanceUtil;
import com.example.immedsee.R;

import java.util.List;

/**
 * DoubleWay on 2019/3/21:15:47
 * 邮箱：13558965844@163.com
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
        private Context mContext;
        private List<PoiInfo> mPoiInfo;
        private LatLng mLocationLatlng;
        private OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView resultName;
        TextView resultAddress;
        TextView resultDistance;
        ImageView imageViewDetails;

        public ViewHolder(View view){
            super(view);
            cardView=(CardView)view;
            resultName=(TextView)view.findViewById(R.id.search_result_name);
            resultAddress=(TextView)view.findViewById(R.id.search_result_address);
            resultDistance=(TextView)view.findViewById(R.id.search_distance_address);
            imageViewDetails=(ImageView)view.findViewById(R.id.search_result_details);
        }
    }

    public SearchResultAdapter(List<PoiInfo> PoiInfo,LatLng LocationLatLng){
        mPoiInfo=PoiInfo;
        mLocationLatlng=LocationLatLng;
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        PoiInfo info=mPoiInfo.get(position);
        holder.resultName.setText(info.getName());
        holder.resultAddress.setText(info.getAddress());
        /**
         * 计算到目标地点的距离，并省略一位小数转化为String
         */
        double distance = (DistanceUtil.getDistance(mLocationLatlng,info.getLocation()))/10000;
        String s=String.format("%.2f",distance);
        holder.resultDistance.setText(s+"公里");
        if(mOnItemClickListener!=null){
            holder.imageViewDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.cardView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPoiInfo.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        mOnItemClickListener=onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }


}
