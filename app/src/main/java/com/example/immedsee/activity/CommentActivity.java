package com.example.immedsee.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaDataSource;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.Utils.UniqueCodeUtils;
import com.example.immedsee.Utils.UriToPathUtil;
import com.example.immedsee.View.MultiImageView;
import com.example.immedsee.dao.Comment;
import com.example.immedsee.dao.Post;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UploadBatchListener;

public class CommentActivity extends AppCompatActivity {
    private Post post;
    private EditText commentContentEdit;
    private MultiImageView multiImageView;
    private List<String> list_path = new ArrayList<>();
    public static final int IMAGE_SELECT = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Toolbar toolbar = (Toolbar)findViewById(R.id.comment_toolBar);
        toolbar.setTitle("评论");
        post=(Post)getIntent().getSerializableExtra("post_data");
       /* if(post!=null){
            Log.d("CommentActivity", post.getPostContent());
        }
        Log.d("CommentActivity",Constant.user.getByName());*/
        commentContentEdit=findViewById(R.id.ed_comment_contents);
         multiImageView=findViewById(R.id.multiImageView);

    }

    /**
     * 确认添加评论
     * @param view
     */
   public void addComment(View view){
        /*Toast.makeText(this,"hhhh",Toast.LENGTH_SHORT).show();*/
       String commentContent=commentContentEdit.getText().toString();

       if(commentContent.isEmpty()){
           DialogPrompt dialogPrompt=new DialogPrompt(this,R.string.please_input_your_content);
           dialogPrompt.show();
           return;
       }
       if(list_path.size()==0){
           DialogPrompt dialogPrompt=new DialogPrompt(this,R.string.please_choose_images);
           dialogPrompt.show();
           return;
       }
       UiTools.showSimpleLD(this,R.string.commentSend_loading);
       final Comment comment=new Comment();
       comment.setContent(commentContent);
       comment.setPost(post);
       comment.setUser(Constant.user);

       final String [] filePath=list_path.toArray((new String[list_path.size()]));
       BmobFile.uploadBatch(filePath, new UploadBatchListener() {
           @Override
           public void onSuccess(List<BmobFile> list, List<String> list1) {
               if(list1.size()==filePath.length){
                   comment.setImageContent(list);
                   comment.save(new SaveListener<String>() {
                       @Override
                       public void done(String s, BmobException e) {
                           UiTools.closeSimpleLD();
                           if(e==null){
                               DialogPrompt dialogPrompt=new DialogPrompt(CommentActivity.this,R.string.commentUP_success);
                               dialogPrompt.showAndFinish(CommentActivity.this);
                               //评论发送成功后
                               CommentActivity.this.setResult(RESULT_OK);

                           }else {
                               DialogPrompt dialogPrompt=new DialogPrompt(CommentActivity.this,R.string.commentUP_error);
                               dialogPrompt.show();
                           }
                       }
                   });
               }
           }

           @Override
           public void onProgress(int i, int i1, int i2, int i3) {

           }

           @Override
           public void onError(int i, String s) {

           }
       });

   }



    /**
     * 从图库中选取图片,显示在九宫格上
     */
    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(intent, IMAGE_SELECT);
        Log.d("CommentActivity", "selectImage: "+list_path.size());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&requestCode==IMAGE_SELECT){
            Uri originalUri = data.getData();
            /**
             * 通过Ucrop裁剪控件来对图片进行压缩，但是会丢失图片的清晰度
             */
            //创建裁剪输出uri
            Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "SampleCropImage"+ UniqueCodeUtils.genSimplePWD()+".jpeg"));
            UCrop.of(originalUri,destinationUri)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(512, 512)
                    .start(this);
           /* list_path.add(UriToPathUtil.getRealFilePath(this,originalUri));
            multiImageView.setList(list_path);*/
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            Log.d("this", "处理完成");
            final Uri resultUri = UCrop.getOutput(data);
            //这里的resultUri.getPath()获取到的是图片的绝对路径
            Log.d("this", "resultUri.getPath()=" + resultUri.getPath());
            list_path.add(resultUri.getPath());
            multiImageView.setList(list_path);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Log.e("this", "剪裁错误：" + cropError.getMessage());
        }
    }
}
