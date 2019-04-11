package com.example.immedsee.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.adapter.PostListAdapter;
import com.example.immedsee.dao.Post;

import java.util.Collections;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class PostAuthorActivity extends AppCompatActivity {
    private CircleImageView postAuthorIcon;
    private TextView postAuthorName;
    private TextView postAuthorSignature;
    private RecyclerView recyclerViewPost;
    private PostListAdapter postListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_author);
        postAuthorIcon=(CircleImageView)findViewById(R.id.post_author_icon_image);
        postAuthorName=(TextView)findViewById(R.id.post_author_username);
        postAuthorSignature=(TextView)findViewById(R.id.post_author_signature);

        recyclerViewPost=(RecyclerView)findViewById(R.id.user_post);
        GridLayoutManager layoutManager=new GridLayoutManager(this,1);
        recyclerViewPost.setLayoutManager(layoutManager);

        Post  post=(Post)getIntent().getSerializableExtra("post_data");
        if(post!=null) {
            UiTools.showSimpleLD(this,R.string.loading);
            Log.d("PostAuthorActivity", "onCreate: " + post.getAuthor().getByName());
            postAuthorName.setText(post.getAuthor().getByName());
            postAuthorSignature.setText(post.getAuthor().getSignature());
            //获得帖子发布人的头像
            if (post.getAuthor().getAvatar() != null){
                String fileUrl = post.getAuthor().getAvatar().getFileUrl();
                setAvatar(fileUrl);
            }else {
                setDefaultAvatar();
            }
        }
        initPostData(post);
    }

    private void initPostData(Post post) {
        BmobQuery<Post> query=new BmobQuery<>();
        query.addWhereEqualTo("author",post.getAuthor());
        query.include("author");
        query.findObjects(new FindListener<Post>() {
            @Override
            public void done(final List<Post> list, BmobException e) {
                UiTools.closeSimpleLD();
                if(e==null){
                    Collections.reverse(list);
                    postListAdapter=new PostListAdapter(list);
                    recyclerViewPost.setAdapter(postListAdapter);
                    postListAdapter.notifyDataSetChanged();
                    postListAdapter.setOnItemClickListener(new PostListAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if(Constant.user!=null) {
                                Intent toPostDetailsIntent = new Intent(PostAuthorActivity.this, PostDetailsActivity.class);
                                toPostDetailsIntent.putExtra("post_data",list.get(position));
                                startActivity(toPostDetailsIntent);
                            }else {
                                DialogPrompt dialogPrompt=new DialogPrompt(PostAuthorActivity.this,R.string.please_login);
                                dialogPrompt.show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setDefaultAvatar() {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(R.drawable.header_icon)
                .apply(options)
                .into(postAuthorIcon);
    }

    private void setAvatar(String path) {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(path)
                .apply(options)
                .into(postAuthorIcon);
    }
}
