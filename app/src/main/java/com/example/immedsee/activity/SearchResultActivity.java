package com.example.immedsee.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.example.immedsee.R;
import com.example.immedsee.adapter.SearchResultAdapter;
import com.example.immedsee.adapter.SugAdapter;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private int mXDirection;
    private float mCurrentAccracy;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    public LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;
    public boolean isFirstLocate=true;
    private BDLocation mcurrentLoction;
    private LatLng mLatLng;
    private PoiSearch mPoiSearch;
    private SuggestionSearch mSuggestionSearch;
    private String Query;
    private  double mLatitude;
    private  double mLongitude;
    private  String mLocationUid;
    private LatLng LL;
    private FloatingActionButton floatSearchResult;
    private RecyclerView recyclerView;
    private SearchResultAdapter resultAdapter;
    private boolean isFirstAdapter=true; //判断是不是第一次加载recyview
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search_result);
        floatSearchResult=(FloatingActionButton)findViewById(R.id.fab_sreach_resuit);
        mapView=(MapView)findViewById(R.id.searchbmapview);
        //移除百度地图LOGO
        mapView.removeViewAt(1);
        baiduMap=mapView.getMap();
        //baiduMap.setTrafficEnabled(true);//开启交通
        baiduMap.setMyLocationEnabled(true);
        mPoiSearch= PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器

        //mSuggestionSearch = SuggestionSearch.newInstance();
       // mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);//创建Sug搜索监听器
        recyclerView=(RecyclerView)findViewById(R.id.search_result_list);
        GridLayoutManager layoutManager=new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);

        Intent intent=getIntent();
        Query=intent.getStringExtra("Query");
        mLatitude=intent.getDoubleExtra("Latitude",0);
        Log.d("this", "Latitude: "+mLatitude);
        mLongitude=intent.getDoubleExtra("Longitude",0);
        Log.d("this", "Longitude: "+mLongitude);
        mLocationUid=intent.getStringExtra("locationUid");

        /**
         * 这里判断模糊搜索传过来经纬度的是不是0
         * 如果是0就先定位，然后执行POI周边搜索
         * 如果不是，就直接标注模糊搜索传来的经纬度
         *
         */
        if(mLatitude==0&&mLongitude==0) {
              requestLocation();
          }else {
              LL = new LatLng(mLatitude, mLongitude);
            /**
             * 根据模糊搜索传来的UID进行poi详情搜索
             */
            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
                    .poiUids(mLocationUid));
              marker();
          }

        floatSearchResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLocation();
                LatLng moveToLocation=new LatLng(mCurrentLatitude,mCurrentLongitude);
                MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(moveToLocation);
                baiduMap.animateMapStatus(update);
                update=MapStatusUpdateFactory.zoomTo(19f);
                baiduMap.animateMapStatus(update);

            }
        });
    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }
    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
        //option.setScanSpan(1000);
        option.setOpenGps(true);
        //  option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation||bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                navigateTo(bdLocation);
            }

        }
    }

    private void navigateTo(BDLocation bdLocation) {
        if(isFirstLocate){
            LatLng ll=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
            MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update=MapStatusUpdateFactory.zoomTo(17f);
            baiduMap.animateMapStatus(update);
            isFirstLocate=false;
        }
        MyLocationData myLocationData=new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                .direction(bdLocation.getDirection())
                .latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude())
                .build();

        //保存第一次定位的数据
        //  mLatLng=new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
        mcurrentLoction=bdLocation;
        mCurrentAccracy= bdLocation.getRadius();
        mCurrentLatitude= bdLocation.getLatitude();
        mCurrentLongitude= bdLocation.getLongitude();
        baiduMap.setMyLocationData(myLocationData);
      //  Log.d("this", "navigateTo: "+mCurrentLatitude+" "+mCurrentLongitude);

        mPoiSearch.searchNearby((new PoiNearbySearchOption().pageCapacity(10)).radius(1000)
                .location(new LatLng(mcurrentLoction.getLatitude(),mcurrentLoction.getLongitude()))
                .keyword(Query));

    }

    private void marker() {
        MapStatusUpdate  update=MapStatusUpdateFactory.zoomTo(12.5f);
        baiduMap.animateMapStatus(update);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_mark);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(LL)
                .icon(bitmap);
        // Bundle bundle = new Bundle();
        //info必须实现序列化接口
        // bundle.putParcelable("info", p);
        //在地图上添加Marker，并显示
        baiduMap.addOverlay(option);
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(LL));
    }

//POI搜索监听
    OnGetPoiSearchResultListener poiSearchResultListener=new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            //使用clear来清除所有覆盖物
            baiduMap.clear();
            if (poiResult.getAllPoi() != null) {
                //mLatLng=poiResult.getAllPoi().get(0).location;
                List<PoiInfo> mData = poiResult.getAllPoi();
                if(isFirstAdapter) {
                    resultAdapter = new SearchResultAdapter(mData);
                    recyclerView.setAdapter(resultAdapter);
                    Log.d("SearchResultActivuty", "onGetPoiResult: ");
                    resultAdapter.notifyDataSetChanged();
                    isFirstAdapter=false;
                }
                //定义Maker坐标点
                for (PoiInfo p : mData) {
                    LatLng point = new LatLng(p.location.latitude, p.location.longitude);
//构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(R.drawable.icon_mark);
//构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap);
                    Bundle bundle = new Bundle();
                    //info必须实现序列化接口
                    bundle.putParcelable("info", p);
                    //在地图上添加Marker，并显示
                    baiduMap.addOverlay(option).setExtraInfo(bundle);
                    //设置搜索到的第一条结果为地图的中心
                 //  baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
                }


            }

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
            /**
             * 在这里对详情搜索的结果进行处理，构造一个PoiInfo对象，将搜索带来的数据赋予这个对象
             * 然后将PoiInfo对象加入List，然后传入适配器，显示在recyview中
             */
              PoiInfo poiInfo=new PoiInfo();
              poiInfo.setCity(poiDetailSearchResult.getPoiDetailInfoList().get(0).getCity());
              poiInfo.setAddress(poiDetailSearchResult.getPoiDetailInfoList().get(0).getAddress());
              poiInfo.setName(poiDetailSearchResult.getPoiDetailInfoList().get(0).getName());
               List<PoiInfo> mData=new ArrayList<>();
              mData.add(poiInfo);
              resultAdapter=new SearchResultAdapter(mData);
              recyclerView.setAdapter(resultAdapter);
              resultAdapter.notifyDataSetChanged();

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };




    protected void onResume() {
        super.onResume();
        //Log.d("this", "onCreate: "+mcurrentLoction.getLatitude()+mcurrentLoction.getLongitude());
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
