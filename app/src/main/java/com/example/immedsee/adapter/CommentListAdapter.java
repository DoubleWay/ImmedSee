package com.example.immedsee.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.View.MultiImageView;
import com.example.immedsee.activity.PostAuthorActivity;
import com.example.immedsee.activity.PostDetailsActivity;
import com.example.immedsee.activity.SearchActivity;
import com.example.immedsee.dao.Comment;
import com.example.immedsee.dao.Post;
import com.example.immedsee.dao.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CloudCodeListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * DoubleWay on 2019/4/12:11:15
 * 邮箱：13558965844@163.com
 */
public class CommentListAdapter extends RecyclerView.Adapter<CommentListAdapter.ViewHolder> {

    private Context mContext;
    private List<Comment> mCommentList;
    private AlertDialog al;

    static class ViewHolder extends RecyclerView.ViewHolder{
         private TextView commentContent;
         private MultiImageView commentImages;
         private TextView commentUserName;
         private TextView commentTime;
         private TextView commentIsSolved;


        public ViewHolder(View itemView) {
            super(itemView);
            commentContent=(TextView)itemView.findViewById(R.id.comment_content);
            commentImages=(MultiImageView)itemView.findViewById(R.id.comment_multiImageView);
            commentUserName=(TextView)itemView.findViewById(R.id.comment_user);
            commentTime=(TextView)itemView.findViewById(R.id.comment_time);
            commentIsSolved=(TextView)itemView.findViewById(R.id.comment_isLove);
        }
    }

    public  CommentListAdapter(List<Comment> commentList){
        mCommentList=commentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mContext==null){
            mContext=parent.getContext();
        }
        View view= LayoutInflater.from(mContext).inflate(R.layout.comment_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Comment comment=mCommentList.get(position);
        holder.commentContent.setText(comment.getContent());
        //加载图片
        final List<String> filePath=new ArrayList<>();
        for (BmobFile bmobFile:comment.getImageContent()){
            String fileUrl=bmobFile.getFileUrl();
            /*Log.d("CommentListAdapter", "onBindViewHolder: "+fileUrl);*/
            filePath.add(fileUrl);
        }
        //加载是否解决图标时候，进行判断,加else是防止recycleview item复用
        if(comment.isSolvde()==true){
            Drawable drawableLeft=mContext.getDrawable(R.mipmap.ic_action_love_success);
            holder.commentIsSolved.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,null,null,null);
            comment.setSolvde(true);
        }else {
            Log.d("CommentListAdapter", "onBindViewHolder: 没有解决，不加载解决图标");
            Drawable drawableLeft=mContext.getDrawable(R.mipmap.ic_action_love);
            holder.commentIsSolved.setCompoundDrawablesWithIntrinsicBounds(drawableLeft,null,null,null);
            comment.setSolvde(true);
        }
        holder.commentImages.setList(filePath);
        holder.commentUserName.setText(comment.getUser().getByName());
        holder.commentTime.setText(comment.getCreatedAt());
//利用MultiImageView控件的图片点击事件实现点击查看大图
        holder.commentImages.setOnItemClickListener(new MultiImageView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                View dialogImageView = LayoutInflater.from(mContext).inflate(
                        R.layout.dialog_image, null);
                ImageView diaimageView=(ImageView)dialogImageView.findViewById(R.id.dialog_img);
                Glide.with(mContext)
                        .load(filePath.get(position))
                        /*.apply(new RequestOptions().placeholder(R.mipmap.loading))*/
                        .into(diaimageView);
                al= new AlertDialog.Builder(mContext)
                        .setView(dialogImageView)
                        .show();
                Log.d("CommentListAdapter", "onItemClick: "+position);
            }
        });

        /**
         * 因为再适配器里才能取得每个评论具体的作者信息，所以点击评论的作者跳转到作者详情的点击事件写在adapter里面
         */
        holder.commentUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toAuthorInfoIntent=new Intent(mContext,PostAuthorActivity.class);
                toAuthorInfoIntent.putExtra("user",comment.getUser());
                mContext.startActivity(toAuthorInfoIntent);
            }
        });


         /* holder.commentIsSolved.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {

                  Toast.makeText(mContext,"hhhh",Toast.LENGTH_SHORT).show();
              }
          });*/
/**
 * 点击满意进行结贴
 */
          holder.commentIsSolved.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View view) {
                  Log.d("CommentListAdapter", "onClick: hhhh ");
                  AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
                  builder.setTitle("确认");
                  builder.setMessage("你将选择本答案作为满意答案");
                  builder.setCancelable(true);
                  builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          //首先查询这个评论属于哪个帖子，如果当前登陆的用户不是帖子的拥有者，就不能进行结贴。
                          BmobQuery<Post>query=new BmobQuery<>();
                          query.addWhereEqualTo("objectId",comment.getPost().getObjectId());
                          query.include("author");
                          query.findObjects(new FindListener<Post>() {
                              @Override
                              public void done(List<Post> list, BmobException e) {
                                  if(e==null){
                                      if(list.get(0).isEnd()){
                                          DialogPrompt dialogPrompt=new DialogPrompt((Activity)mContext,R.string.post_end);
                                          dialogPrompt.show();
                                      }
                                      else if(Constant.user.getUsername().equals(list.get(0).getAuthor().getUsername())) {
                                          Drawable drawableLeft = mContext.getDrawable(R.mipmap.ic_action_love_success);
                                          holder.commentIsSolved.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, null, null);
                                          list.get(0).setEnd(true);
                                          list.get(0).update(new UpdateListener() {
                                              @Override
                                              public void done(BmobException e) {
                                                  if(e==null){
                                                      Log.d("CommentListAdapter", "done: 帖子解决成功");
                                                  }else {
                                                      Log.d("CommentListAdapter", "done: 帖子解决失败");
                                                  }
                                              }
                                          });

                                          /**
                                           * 因为Bmob后端云，无法在客户端修改user表中其他用户的数据，你没有登陆就无法修改，所以通过
                                           * 云函数，在后端云上对user表进行修改
                                           */
                                          double money=comment.getUser().getMoney()+list.get(0).getPostMoney();
                                          JSONObject params = new JSONObject();
                                          try {
                                            // params.put("name",comment.getUser().getUsername());
                                              params.put("id",comment.getUser().getObjectId());
                                             // params.put("password","123456");
                                              params.put("money",money);
                                              Log.d("moneyManage", " " +comment.getUser().getMoney());
                                              Log.d("moneyManage", " " +list.get(0).getPostMoney());
                                              Log.d("moneyManage", " " +money);
                                          } catch (JSONException e1) {
                                              e1.printStackTrace();
                                          }
                                          AsyncCustomEndpoints ace = new AsyncCustomEndpoints();
                                    //第一个参数是云函数的方法名称，第二个参数是上传到云函数的参数列表（JSONObject cloudCodeParams），第三个参数是回调类
                                          ace.callEndpoint("moneyManage", params, new CloudCodeListener() {
                                              @Override
                                              public void done(Object object, BmobException e) {
                                                  if (e == null) {
                                                      String result = object.toString();
                                                      Log.d("moneyManage", " " + result);
                                                  } else {
                                                      Log.d("moneyManage", " " + e.getMessage());
                                                  }
                                              }
                                           });

                                          comment.setSolvde(true);
                                          Log.d("CommentListAdapter", "done: 评论作者的财富为"+comment.getUser().getMoney());
                                          Log.d("CommentListAdapter", "done: 帖子价值为"+list.get(0).getPostMoney());
                                          /*comment.getUser().setMoney(comment.getUser().getMoney()+list.get(0).getPostMoney());*/
                                          //点击满意后，图标变红，将comment的是否解决问题改为ture，然后存入服务器
                                          comment.update(new UpdateListener() {
                                              @Override
                                              public void done(BmobException e) {
                                                  if(e==null){
                                                      Log.d("CommentListAdapter", "done: 图标修改成功");
                                                  }else {
                                                      Log.d("CommentListAdapter", "done: 图标修改失败");
                                                  }
                                              }
                                          });
                                      }else {
                                          //为了显示dialogPrompt提示，将获得的上下文强转为activity
                                          DialogPrompt dialogPrompt=new DialogPrompt((Activity)mContext,R.string.post_end_error_user);
                                          dialogPrompt.show();
                                          Log.d("CommentListAdapter", "done: 不是你的帖子 无法结贴");
                                          Log.d("CommentListAdapter", "done: "+list.get(0).getAuthor().getByName());
                                          Log.d("CommentListAdapter", "done: "+Constant.user.getByName());
                                      }
                                  }
                              }
                          });
                      }
                  });
                  builder.show();
              }
          });


    }

    @Override
    public int getItemCount() {
        return mCommentList.size();
    }
}
