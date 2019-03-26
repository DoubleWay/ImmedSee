package com.example.immedsee.activity;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiDetailInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import com.example.immedsee.R;
import com.example.immedsee.adapter.SearchResultAdapter;


import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {
    private SwipeRefreshLayout swipeRefreshLayout;
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
    private String Query;
    private  double mLatitude;
    private  double mLongitude;
    private  String mLocationUid;
    private  String suggestKey;
    private LatLng LL;
    private FloatingActionButton floatSearchResult;
    private RecyclerView recyclerView;
    private SearchResultAdapter resultAdapter;
    private boolean isFirstAdapter=true; //判断是不是第一次加载recyview
    private PoiResult mPoiResult;
    private GridLayoutManager layoutManager;
    private Marker marker;
    private ImageView imageViewDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search_result);

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swip_refresh);//下拉刷新控件
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);


        floatSearchResult=(FloatingActionButton)findViewById(R.id.fab_sreach_resuit);
        mapView=(MapView)findViewById(R.id.searchbmapview);
        //移除百度地图LOGO
        mapView.removeViewAt(1);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMarkerClickListener(markerClickListener);//设置地图上点击事件监听
        baiduMap.setOnMapClickListener(mapClickListener);//设置地图点击事件监听

        mPoiSearch= PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器

        recyclerView=(RecyclerView)findViewById(R.id.search_result_list);
        layoutManager=new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);
       // recyclerView.addOnScrollListener(new OnScrollListener());


        Intent intent=getIntent();
        Query=intent.getStringExtra("Query");
        mLatitude=intent.getDoubleExtra("Latitude",0);
       // Log.d("this", "Latitude: "+mLatitude);
        mLongitude=intent.getDoubleExtra("Longitude",0);
        //Log.d("this", "Longitude: "+mLongitude);
        mLocationUid=intent.getStringExtra("locationUid");
        suggestKey=intent.getStringExtra("SuggestKey");
        Log.d("this", "suggestKey: "+suggestKey);

        /**
         * 这里判断模糊搜索传过来经纬度的是不是0
         * 如果是0就先定位，然后执行POI周边搜索
         * 如果不是，就直接标注模糊搜索传来的经纬度
         *
         */
        if(mLatitude==0&&mLongitude==0) {
              requestLocation();
            /**
             * recycleview的刷新事件判断，如果是poi搜索且有多页结果，下拉刷新为下一页
             * 如果是suggest搜索直接标注的，刷新事件没反应
             */
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    int currentPageNum = mPoiResult.getCurrentPageNum();
                    int totalPageNum = mPoiResult.getTotalPageNum();
                    //Log.d("number", "onGetPoiResult: " + totalPageNum);
                    if (currentPageNum < totalPageNum-1) {
                        isFirstAdapter = true;
                        mPoiSearch.searchNearby((new PoiNearbySearchOption().pageNum(currentPageNum + 1)).radius(1000)
                                .location(new LatLng(mcurrentLoction.getLatitude(), mcurrentLoction.getLongitude()))
                                .keyword(Query));
                        swipeRefreshLayout.setRefreshing(false);
                       /* Toast.makeText(SearchResultActivity.this, "hhhh", Toast.LENGTH_SHORT).show();*/
                    }else {
                        swipeRefreshLayout.setRefreshing(false);
                         Toast.makeText(SearchResultActivity.this, "已经是最后一页", Toast.LENGTH_SHORT).show();
                    }
                }

            });
          }else {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

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
        /**
         * 判断传入的是否是sug搜索的坐标
         * 如果不是就进行poi搜索
         * 如果是就进行定位和标注
         */
        if(mLatitude==0&&mLongitude==0) {
            mPoiSearch.searchNearby((new PoiNearbySearchOption().pageCapacity(20)).radius(1000)
                    .location(new LatLng(mcurrentLoction.getLatitude(), mcurrentLoction.getLongitude()))
                    .keyword(Query));
        }

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
        baiduMap.addOverlay(option);
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(LL));
        marker=(Marker)( baiduMap.addOverlay(option));
        marker.setTitle(suggestKey);
    }

//POI搜索监听
    OnGetPoiSearchResultListener poiSearchResultListener=new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            mPoiResult=poiResult;
            //使用clear来清除所有覆盖物
            baiduMap.clear();
            if (poiResult.getAllPoi() != null) {
                //mLatLng=poiResult.getAllPoi().get(0).location;
                int totalPoiNum = poiResult.getTotalPoiNum();
                int totalPageNum = poiResult.getTotalPageNum();
                int currentPageNum = poiResult.getCurrentPageNum();
                Log.d("number", "totalPageNum: " + totalPageNum);
                Log.d("number", "currentPageNum: "+currentPageNum);

                final List<PoiInfo> mData = poiResult.getAllPoi();

                if(isFirstAdapter) {
                    resultAdapter = new SearchResultAdapter(mData);
                    recyclerView.setAdapter(resultAdapter);
                    Log.d("SearchResultActivuty", "onGetPoiResult: ");
                    resultAdapter.notifyDataSetChanged();
                    isFirstAdapter=false;
                }
                /**
                 * poi搜索结果列表点击事件
                 */
                resultAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        PoiInfo info=mData.get(position);
                        Intent toResultDetails=new Intent(getApplicationContext(),ResultDetailsActivity.class);
                        toResultDetails.putExtra("ResultName",info.getName());
                        toResultDetails.putExtra("ResultUid",info.getUid());
                        toResultDetails.putExtra("ReaultLongitude",info.getLocation().longitude);
                        toResultDetails.putExtra("ReaultLatitude",info.getLocation().latitude);


                        startActivity(toResultDetails);
                    }
                });

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
                    marker=(Marker)(baiduMap.addOverlay(option));
                    //将poi地点的名字设置为marker的titile 下面方便获取
                    marker.setTitle(p.getName());
                    //设置搜索到的第一条结果为地图的中心
                 //  baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
                }


            }

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        }

        @Override
        public void onGetPoiDetailResult(final PoiDetailSearchResult poiDetailSearchResult) {
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

            resultAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    Intent toResultDetails=new Intent(getApplicationContext(),ResultDetailsActivity.class);
                    toResultDetails.putExtra("ResultName",poiDetailSearchResult.getPoiDetailInfoList().get(0).getName());
                    toResultDetails.putExtra("ResultUid",poiDetailSearchResult.getPoiDetailInfoList().get(0).getUid());
                    toResultDetails.putExtra("ReaultLongitude",poiDetailSearchResult.getPoiDetailInfoList().get(0).getLocation().longitude);
                    toResultDetails.putExtra("ReaultLatitude",poiDetailSearchResult.getPoiDetailInfoList().get(0).getLocation().latitude);
                    startActivity(toResultDetails);

                }
            });

        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

 /* class OnScrollListener extends RecyclerView.OnScrollListener {
      private int lastVisibleItem;

       @Override
      public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
           super.onScrollStateChanged(recyclerView, newState);
           if ((newState == RecyclerView.SCROLL_STATE_IDLE)
                   && (lastVisibleItem + 1 == resultAdapter.getItemCount())) {
               //已经滑动到最后一个 item
               //在这里执行刷新/加载更多的操作
              *//* int currentPageNum = mPoiResult.getCurrentPageNum();
               int totalPageNum = mPoiResult.getTotalPageNum();

               isFirstAdapter=true;
               mPoiSearch.searchNearby((new PoiNearbySearchOption().pageNum(currentPageNum+1)).radius(1000)
                       .location(new LatLng(mcurrentLoction.getLatitude(),mcurrentLoction.getLongitude()))
                       .keyword(Query));*//*
               TimerTask timerTask =new TimerTask() {
                   @Override
                   public void run() {
                       int currentPageNum = mPoiResult.getCurrentPageNum();
                       int totalPageNum = mPoiResult.getTotalPageNum();

                       isFirstAdapter=true;
                       mPoiSearch.searchNearby((new PoiNearbySearchOption().pageNum(currentPageNum+1)).radius(1000)
                               .location(new LatLng(mcurrentLoction.getLatitude(),mcurrentLoction.getLongitude()))
                               .keyword(Query));
                   }
               };
               Timer timer=new Timer();
               timer.schedule(timerTask,2000);
               Toast.makeText(SearchResultActivity.this, "LLLLL", Toast.LENGTH_SHORT).show();
           }
       }
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);
          lastVisibleItem=layoutManager.findLastVisibleItemPosition();
      }

  }*/


    BaiduMap.OnMapClickListener mapClickListener=new BaiduMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng)
        {
            baiduMap.hideInfoWindow();
        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi) {

            return false;
        }
    };

    /**
     * 地图上标注物的点击事件，显示信息
     */
  BaiduMap.OnMarkerClickListener markerClickListener=new BaiduMap.OnMarkerClickListener() {
      @Override
      public boolean onMarkerClick(final Marker marker) {
          final LatLng markerLaLng=marker.getPosition();

          //实例化一个地理编码查询对象
          GeoCoder geoCoder = GeoCoder.newInstance();
          //设置反地理编码位置坐标
          ReverseGeoCodeOption option = new ReverseGeoCodeOption();
          option.location(markerLaLng);
          geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
              @Override
              public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

              }
              @Override
              public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                  ReverseGeoCodeResult.AddressComponent addressDetail = reverseGeoCodeResult.getAddressDetail();
                  View view=View.inflate(getApplicationContext(),R.layout.infowindow,null);
                  TextView agentName=(TextView)view.findViewById(R.id.agent_name);
                  TextView agentAaddr=(TextView)view.findViewById(R.id.agent_addr);
                  agentName.setText(marker.getTitle());
                  agentAaddr.setText(reverseGeoCodeResult.getAddress());
                  InfoWindow infoWindow=new InfoWindow(view,markerLaLng,-60);
                  //InfoWindow infoWindow = new InfoWindow(button, latLng, -47);
                  //显示信息窗口
                  baiduMap.showInfoWindow(infoWindow);
                  Log.d("that", "反地理编码信息 "+reverseGeoCodeResult.getAddress());
              }
          });

          geoCoder.reverseGeoCode(option);

          //Toast.makeText(getActivity().getApplicationContext(),markerLaLng.toString(),Toast.LENGTH_SHORT).show();
          return false;
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
