package com.example.immedsee.activity;

import android.content.Intent;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.baidu.lbsapi.model.BaiduPoiPanoData;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.immedsee.R;
import com.baidu.lbsapi.panoramaview.*;
import com.baidu.lbsapi.BMapManager;


public class ResultDetailsActivity extends AppCompatActivity {
    private String resultName;
    private String resultUid;
    private double resultLongitude;
    private double resultLatitude;
    private PoiSearch mPoiSearch;

    private PanoramaView mPanoramaView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanorApplication app = (PanorApplication) this.getApplication();
        if (app.mBMapManager == null) {
            app.mBMapManager = new BMapManager(app);
            app.mBMapManager.init(new PanorApplication.MyGeneralListener());
        }
        setContentView(R.layout.activity_result_details);
        Intent intent=getIntent();
         resultName=intent.getStringExtra("ResultName");
         resultUid=intent.getStringExtra("ResultUid");
         resultLatitude=intent.getDoubleExtra("ReaultLatitude",0);
        Log.d("ResultDetails", "resultLatitude: "+resultLatitude);
         resultLongitude=intent.getDoubleExtra("ReaultLongitude",0);
        Log.d("ResultDetails", "resultLongitude: "+resultLongitude);

        mPoiSearch= PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_tollbar);
       //resultImage=(ImageView)findViewById(R.id.result_image_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(resultName);

        mPanoramaView=(PanoramaView)findViewById(R.id.result_panorama_view);
        PanoramaRequest request=PanoramaRequest.getInstance(ResultDetailsActivity.this);
        BaiduPoiPanoData poiPanoData=request.getPanoramaInfoByUid(resultUid);
        mPanoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
        if (poiPanoData.hasInnerPano()) {//判断该POI是否有内景
            mPanoramaView.setPanoramaByUid(resultUid, PanoramaView.PANOTYPE_INTERIOR);
            //mPanoramaView.setIndoorAlbumGone();//除去内景相册
            mPanoramaView.setIndoorAlbumVisible();//将内景相册显示
        }else if (poiPanoData.hasStreetPano()) {//判断该POI是否有外景,就只能通过经纬度来显示外景
            mPanoramaView.setPanorama(resultLongitude,resultLatitude);//没有内景就通过经纬度来展示街景
        }else{
            Toast.makeText(ResultDetailsActivity.this, "sorry,网络不给力无法加载全景", Toast.LENGTH_SHORT).show();
        }

        mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                .poiUids(resultUid));

    }

    //POI搜索监听
    OnGetPoiSearchResultListener poiSearchResultListener= new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            Log.d("ResultDetails", "onGetPoiDetailResult: "+poiDetailSearchResult.toString());

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };
    @Override
    protected void onPause() {
        mPanoramaView.onPause();
        super.onPause();
    }
    @Override
    protected void onResume() {
        mPanoramaView.onResume();
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        mPanoramaView.destroy();
        super.onDestroy();
    }

}
