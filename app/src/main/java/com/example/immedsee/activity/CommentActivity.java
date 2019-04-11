package com.example.immedsee.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.immedsee.R;
import com.example.immedsee.Utils.UriToPathUtil;
import com.example.immedsee.View.MultiImageView;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    private MultiImageView multiImageView;
    List<String> tableImgs=new ArrayList<>();
    private List<String> list_path = new ArrayList<String>();
    public static final int IMAGE_SELECT = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
         multiImageView=findViewById(R.id.multiImageView);

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
        /*Log.d("CommentActivity", "selectImage: "+list_path.get(list_path.size()));*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK&&requestCode==IMAGE_SELECT){
            Uri originalUri = data.getData();
            list_path.add(UriToPathUtil.getRealFilePath(this,originalUri));
            multiImageView.setList(list_path);
        }
    }
}
