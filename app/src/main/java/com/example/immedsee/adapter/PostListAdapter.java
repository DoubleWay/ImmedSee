package com.example.immedsee.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.immedsee.R;
import com.example.immedsee.dao.Post;

import java.util.List;

import cn.bmob.v3.b.I;

/**
 * DoubleWay on 2019/4/10:16:42
 * 邮箱：13558965844@163.com
 */
public class PostListAdapter extends RecyclerView.Adapter<PostListAdapter.ViewHolder> {
    private Context mContext;
    private List<Post> mPostList;
    private OnItemClickListener mOnItemClickListener;

    static class ViewHolder extends RecyclerView.ViewHolder{
           private TextView postContent;
           private TextView postMoney;
           private TextView postAuthorName;
           private TextView postCreateTime;
           private ImageView postSolved;
           private CardView postCardView;

        public ViewHolder(View itemView) {
            super(itemView);
            postContent=(TextView)itemView.findViewById(R.id.post_content);
            postMoney=(TextView)itemView.findViewById(R.id.post_money);
            postAuthorName=(TextView)itemView.findViewById(R.id.post_authorName);
            postCreateTime=(TextView)itemView.findViewById(R.id.post_CreateTime);
            postSolved=(ImageView)itemView.findViewById(R.id.post_solved);
            postCardView=(CardView)itemView.findViewById(R.id.post_card_view);
        }
    }

    public PostListAdapter(List<Post> PostList){
        mPostList=PostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.post_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
              Post post=mPostList.get(position);
              holder.postContent.setText(post.getPostContent());
              holder.postMoney.setText(post.getPostMoney()+"");
              holder.postAuthorName.setText(post.getAuthor().getByName());
              holder.postCreateTime.setText(post.getCreatedAt());
              if(post.isEnd()){
                  holder.postSolved.setVisibility(View.VISIBLE);
              }else {
                  holder.postSolved.setVisibility(View.GONE);
              }
              if(mOnItemClickListener!=null){
                  holder.postCardView.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          int position=holder.getLayoutPosition();
                          mOnItemClickListener.onItemClick(view,position);
                      }
                  });

              }
    }
    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
