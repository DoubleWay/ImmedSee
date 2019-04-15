package com.example.immedsee.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.adapter.CommentListAdapter;
import com.example.immedsee.dao.Comment;
import com.example.immedsee.dao.Post;
import com.example.immedsee.dao.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class PostDetailsActivity extends AppCompatActivity {
    private TextView postTitle;
    private TextView postContent;
    private TextView postAuthorName;
    private TextView postMoney;
    private TextView commentNumber;
    private ImageView postdetailSolved;
    private RecyclerView commentRecycle;
    private CommentListAdapter commentListAdapter;
    private Post post;//保存帖子信息

    private final static int REQUEST_CODE_ADDCOMMENT=1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        Toolbar toolbar = (Toolbar)findViewById(R.id.post_details_toolBar);
        toolbar.setTitle("悬赏详情");
        setSupportActionBar(toolbar);
        postTitle=(TextView)findViewById(R.id.post_details_title);
        postContent=(TextView)findViewById(R.id.post_details_content);
        postAuthorName=(TextView)findViewById(R.id.post_details_user);
        postMoney=(TextView)findViewById(R.id.post_details_money) ;
        postdetailSolved=(ImageView)findViewById(R.id.post_details_solved);
        commentNumber=(TextView)findViewById(R.id.post_comment_number);

        commentRecycle=(RecyclerView)findViewById(R.id.comm_recyclelist);
        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        commentRecycle.setLayoutManager(layoutManager);

        Intent intent=getIntent();
         post=(Post)getIntent().getSerializableExtra("post_data");
        postTitle.setText(post.getPostTitle());
        postContent.setText(post.getPostContent());
        postAuthorName.setText(post.getAuthor().getByName());
        postMoney.setText(post.getPostMoney()+"即视币");
        if(post.isEnd()){
            postdetailSolved.setVisibility(View.VISIBLE);
        }else {
            postdetailSolved.setVisibility(View.GONE);
        }
        UiTools.showSimpleLD(this,R.string.loading);
        setCommentInfo();
        Log.d("PostDetailsActivity", "onCreate: "+post.getAuthor().getByName());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_details_menu, menu);
        return true;
    }

    //帖子删除功能
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.post_delete:
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("确认");
                builder.setMessage("你确认删除帖子？");
                builder.setCancelable(true);
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        User currentUser = BmobUser.getCurrentUser(User.class);
                        Log.d("PostDetailsActivity", "done: "+currentUser.getMoney());
                        if(!post.getAuthor().getUsername().equals(Constant.user.getUsername()) ){
                            Log.d("PostDetailsActivity", "onClick:删除 "+post.getAuthor().getUsername());
                            Log.d("PostDetailsActivity", "onClick:删除 "+Constant.user.getUsername());
                            DialogPrompt dialogPrompt=new DialogPrompt(PostDetailsActivity.this,R.string.post_delete_error);
                            dialogPrompt.show();
                            return;
                        }else {
                            Log.d("PostDetailsActivity", "onClick:删除 ");
                            if(!post.isEnd()){

                                Constant.user.setMoney(Constant.user.getMoney()+post.getPostMoney());
                                Log.d("PostDetailsActivity", "done: "+Constant.user.getMoney());
                                Log.d("PostDetailsActivity", "done: "+post.getPostMoney());
                                Constant.user.update(new UpdateListener() {
                                    @Override
                                    public void done(BmobException e) {
                                        if(e==null){
                                            Log.d("PostDetailsActivity", "done: 赏金退还成功");
                                        }else {
                                            Log.d("PostDetailsActivity", "done: 赏金退还失败");
                                        }
                                    }
                                });
                            }
                            post.setDeleteTag(1);
                            post.update(new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                    if(e==null){
                                        Log.d("PostDetailsActivity", "done: 删除成功");
                                        DialogPrompt dialogPrompt=new DialogPrompt(PostDetailsActivity.this,R.string.post_delete_success);
                                        dialogPrompt.showAndFinish(PostDetailsActivity.this);
                                    }else {
                                        Log.d("PostDetailsActivity", "done: 删除失败");
                                        DialogPrompt dialogPrompt=new DialogPrompt(PostDetailsActivity.this,R.string.post_delete_error2);
                                        dialogPrompt.show();
                                    }
                                }
                            });
                        }
                    }
                });
                builder.show();
        }
        return true;
    }

    /**
     * 加载评论列表
     */

    private void setCommentInfo() {
        BmobQuery<Comment> query=new BmobQuery<>();
        query.order("-updatedAt");//根据修改时间排序，这样就能将满意答案置顶。
        query.addWhereEqualTo("post",post);
        query.include("post");
        query.include("user");
        query.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> list, BmobException e) {
                commentNumber.setText("评论数"+list.size());
                UiTools.closeSimpleLD();
               /* Collections.reverse(list);//重要，将list的排列顺序倒置*/
                     if(e==null){
                         //根据Tag判断评论是否显示
                         final List<Comment>list2=new ArrayList<>();
                         for(Comment comment:list){
                             if(comment.getDeleteTag()==0){
                                 list2.add(comment);
                             }
                         }
                         commentListAdapter=new CommentListAdapter(list2);
                         commentRecycle.setAdapter(commentListAdapter);
                         commentListAdapter.notifyDataSetChanged();
                     }
            }
        });
    }



    /**
     * 添加评论
     * @param v
     */
    public void addComment(View v){
        if(post.isEnd()){
            DialogPrompt dialogPrompt=new DialogPrompt(this,R.string.post_end);
            dialogPrompt.show();
            return;
        }
        Intent toCommentIntent=new Intent(this,CommentActivity.class);
        toCommentIntent.putExtra("post_data",post);
        startActivityForResult(toCommentIntent,REQUEST_CODE_ADDCOMMENT);
}

    /**
     * 点击用户名得到帖子作者信息
     * @param view
     */
  public void authorInfo(View view){
        Intent toAuthorInfoIntent=new Intent(this,PostAuthorActivity.class);
        toAuthorInfoIntent.putExtra("user",post.getAuthor());
        startActivity(toAuthorInfoIntent);
}

    public void share(View view){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT,postContent.getText().toString() +"_"+postAuthorName.getText()+"_ _来自即视");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "分享"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE_ADDCOMMENT){
            setCommentInfo();
        }
    }
}
