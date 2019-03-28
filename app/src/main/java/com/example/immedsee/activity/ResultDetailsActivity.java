package com.example.immedsee.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.lbsapi.model.BaiduPanoData;
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
    private String resultCity;
    private String resultUid;
    private double resultLongitude;
    private double resultLatitude;
    private PoiSearch mPoiSearch;
    private TextView resultNameText;
    private TextView resultTagText;
    private TextView resultAddressText;
    private TextView resultPhoneText;
    private TextView resultTimeText;
    private ImageView resultPhoneCall;
    private PoiDetailSearchResult mPoiDetailSearchResult;
    private CardView resultToSearch;
   // private WebView resultWebView;

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
        final Intent intent=getIntent();
         resultName=intent.getStringExtra("ResultName");
        resultCity=intent.getStringExtra("ResultCity");
         resultUid=intent.getStringExtra("ResultUid");
         resultLatitude=intent.getDoubleExtra("ReaultLatitude",0);
        Log.d("ResultDetails", "resultLatitude: "+resultLatitude);
         resultLongitude=intent.getDoubleExtra("ReaultLongitude",0);
        Log.d("ResultDetails", "resultLongitude: "+resultLongitude);
        resultToSearch=(CardView)findViewById(R.id.result_toSearch);

        mPoiSearch= PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器
        resultNameText=(TextView)findViewById(R.id.result_name_text);
        resultTagText=(TextView)findViewById(R.id.result_tag_text);
        resultAddressText=(TextView)findViewById(R.id.result_address_text);
        resultPhoneText=(TextView)findViewById(R.id.result_phone_text);
        resultTimeText=(TextView)findViewById(R.id.result_time_text);
        resultPhoneCall=(ImageView)findViewById(R.id.result_phone_call);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing_tollbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collapsingToolbarLayout.setTitle(resultName);

        resultToSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toSearchIntent=new Intent(ResultDetailsActivity.this,SearchActivity.class);
                toSearchIntent.putExtra("LoctionCity",resultCity);
                /**
                 * 将要进行周边搜索的目标地点坐标传给搜索页面
                 */
                toSearchIntent.putExtra("ResultLocationLongitude",resultLongitude);
                toSearchIntent.putExtra("ResultLocationLatitude",resultLatitude);

                startActivity(toSearchIntent);
            }
        });

          mPanoramaView = (PanoramaView) findViewById(R.id.result_panorama_view);
          mPanoramaView.setPanoramaPitch(90);
          mPanoramaView.setPanoramaHeading(60);
          mPanoramaView.setShowTopoLink(false);
          mPanoramaView.setPanoramaImageLevel(PanoramaView.ImageDefinition.ImageDefinitionMiddle);
        if(resultUid!=null) {
            PanoramaRequest request = PanoramaRequest.getInstance(ResultDetailsActivity.this);
            BaiduPoiPanoData poiPanoData = request.getPanoramaInfoByUid(resultUid);
          if (poiPanoData.hasInnerPano()) {//判断该POI是否有内景
              mPanoramaView.setPanoramaByUid(resultUid, PanoramaView.PANOTYPE_INTERIOR);
              //mPanoramaView.setIndoorAlbumGone();//除去内景相册
              mPanoramaView.setIndoorAlbumVisible();//将内景相册显示
          } else if (poiPanoData.hasStreetPano()) {//判断该POI是否有外景,就只能通过经纬度来显示外景
              mPanoramaView.setPanorama(resultLongitude, resultLatitude);//没有内景就通过经纬度来展示街景
          } else {
              Toast.makeText(ResultDetailsActivity.this, "sorry,这里没有全景", Toast.LENGTH_SHORT).show();
          }
          mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                  .poiUids(resultUid));
      }  else {

            resultNameText.setText(resultName);
            mPanoramaView.setPanorama(resultLongitude, resultLatitude);
      }

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
        public void onGetPoiDetailResult( PoiDetailSearchResult poiDetailSearchResult) {
           mPoiDetailSearchResult=poiDetailSearchResult;
            Log.d("ResultDetails", "onGetPoiDetailResult: "+poiDetailSearchResult.toString());
            resultNameText.setText(poiDetailSearchResult.getPoiDetailInfoList().get(0).getName());
            resultTagText.setText("类型："+poiDetailSearchResult.getPoiDetailInfoList().get(0).getTag());
            resultAddressText.setText("地址："+poiDetailSearchResult.getPoiDetailInfoList().get(0).getAddress());
            if(!poiDetailSearchResult.getPoiDetailInfoList().get(0).getTelephone().equals("")){
                resultPhoneCall.setVisibility(View.VISIBLE);
                resultPhoneText.setText("电话："+poiDetailSearchResult.getPoiDetailInfoList().get(0).getTelephone());
                /**
                 * 打电话运行时权限
                 */
                resultPhoneCall.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(ContextCompat.checkSelfPermission(ResultDetailsActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(ResultDetailsActivity.this, new String[]{"Manifest.permission.CALL_PHONE"},1);
                        }else {
                            call();

                        }

                    }
                });
                Log.d("ResultDetails", "onGetPoiDetailResult: "+poiDetailSearchResult.getPoiDetailInfoList().get(0).getTelephone());
            }
            if(!poiDetailSearchResult.getPoiDetailInfoList().get(0).getShopHours().equals("")){
                resultTimeText.setText("营业时间："+poiDetailSearchResult.getPoiDetailInfoList().get(0).getShopHours());
            }
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    private void call() {
        try {
            String []Telephone=null;
            Telephone=mPoiDetailSearchResult.getPoiDetailInfoList().get(0).getTelephone().split(",");
            /**
             * 如果有多个电话的话进行拆分，只取第一个号码
             */
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:"+Telephone[0]));
            startActivity(intent);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    call();
                }else {
                    Toast.makeText(ResultDetailsActivity.this,"你拒绝了权限",Toast.LENGTH_SHORT).show();
                }
                break;

            default:break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return  true;
    }
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
