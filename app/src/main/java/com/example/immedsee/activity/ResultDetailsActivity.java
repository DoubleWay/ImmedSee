package com.example.immedsee.activity;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.immedsee.R;

public class ResultDetailsActivity extends AppCompatActivity {
    private final String baseUrl = "http://pcsv1.map.bdimg.com/scape/?qt=pdata&pos=0_0&z=0&sid=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_details);
        Intent intent=getIntent();
        String resultName=intent.getStringExtra("ResultName");
        String resultUid=intent.getStringExtra("ResultUid");
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_tollbar);
        ImageView resultImage=(ImageView)findViewById(R.id.result_image_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(resultName);

        String url = baseUrl + resultUid;
        Glide.with(this).load(url).into(resultImage);

    }
}
