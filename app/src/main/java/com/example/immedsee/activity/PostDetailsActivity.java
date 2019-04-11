package com.example.immedsee.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.immedsee.R;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.dao.Post;

public class PostDetailsActivity extends AppCompatActivity {
    private TextView postTitle;
    private TextView postContent;
    private TextView postAuthorName;
    private TextView postMoney;
    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        postTitle=(TextView)findViewById(R.id.post_details_title);
        postContent=(TextView)findViewById(R.id.post_details_content);
        postAuthorName=(TextView)findViewById(R.id.post_details_user);
        postMoney=(TextView)findViewById(R.id.post_details_money) ;
        Intent intent=getIntent();
         post=(Post)getIntent().getSerializableExtra("post_data");
        postTitle.setText(post.getPostTitle());
        postContent.setText(post.getPostContent());
        postAuthorName.setText(post.getAuthor().getByName());
        postMoney.setText(post.getPostMoney()+"即视币");
        Log.d("PostDetailsActivity", "onCreate: "+post.getAuthor().getByName());
    }

public void addComment(View v){
        Intent toCommentIntent=new Intent(this,CommentActivity.class);
        startActivity(toCommentIntent);
}


  public void authorInfo(View view){
        Intent toAuthorInfoIntent=new Intent(this,PostAuthorActivity.class);
        toAuthorInfoIntent.putExtra("post_data",post);
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
}
