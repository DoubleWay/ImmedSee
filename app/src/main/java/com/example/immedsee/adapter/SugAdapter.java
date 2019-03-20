package com.example.immedsee.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.SuggestionResult;
import com.example.immedsee.R;

import java.util.List;

/**
 * DoubleWay on 2019/3/19:16:58
 * 邮箱：13558965844@163.com
 */
public class SugAdapter extends RecyclerView.Adapter<SugAdapter.ViewHolder>{
       private Context mContext;
       private OnItemClickListener mOnItemClickListener;
       private List<SuggestionResult.SuggestionInfo> mSuggestionInfoList;


    static  class ViewHolder extends RecyclerView.ViewHolder{
        private CardView cardView;
        private TextView suggestResuitKeyView;
        private TextView suggestResuitDisView;

          public ViewHolder(View view){
              super(view);
              cardView=(CardView)view;
              suggestResuitKeyView=(TextView)view.findViewById(R.id.suggest_search_result_key);
              suggestResuitDisView=(TextView)view.findViewById(R.id.suggest_search_result_dis);
          }


    }

    public SugAdapter(List<SuggestionResult.SuggestionInfo> SuggestionInfoList){
         mSuggestionInfoList=SuggestionInfoList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.suggest_search_result_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        SuggestionResult.SuggestionInfo suggestionRe=mSuggestionInfoList.get(position);
        holder.suggestResuitKeyView.setText(suggestionRe.getKey());
        holder.suggestResuitDisView.setText(suggestionRe.getCity()+suggestionRe.getDistrict());

        if (mOnItemClickListener != null) {
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(holder.cardView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSuggestionInfoList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
