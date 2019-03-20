package com.example.immedsee;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
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
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
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
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.immedsee.adapter.SugAdapter;
import com.example.immedsee.fragment.FragmentOne;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private SearchView mSearchView;
    private PoiSearch mPoiSearch;
    private SuggestionSearch mSuggestionSearch;
   // private TextView textView;
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    private int mXDirection;
    private float mCurrentAccracy;

    private double mCurrentLatitude;
    private double mCurrentLongitude;
    public LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;
    public boolean isFirstLocate=true;
   // private AutoCompleteTextView keyWorldsView;
    private BDLocation mcurrentLoction;
    private LatLng mLatLng;
    private SugAdapter sugAdapter;
    private RecyclerView recyclerView;
    private List<SuggestionResult.SuggestionInfo> SuggestionInfoList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search);
       //创建显示模糊搜索结果的列表
        recyclerView=(RecyclerView)findViewById(R.id.suggest_search_list);
        GridLayoutManager layoutManager=new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);
       // textView=(TextView)findViewById(R.id.test_view);
        mapView=(MapView)findViewById(R.id.searchbmapview);
        //移除百度地图LOGO
        mapView.removeViewAt(1);
        baiduMap=mapView.getMap();
        //baiduMap.setTrafficEnabled(true);//开启交通
        baiduMap.setMyLocationEnabled(true);
        mPoiSearch=PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器

        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);//创建Sug搜索监听器

        Toolbar toolbar=(Toolbar)findViewById(R.id.search_toolBar);
        toolbar.setTitle("搜索");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        //地图需要的权限申请
        /*List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(this,Manifest.permission
                .ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission
                .READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(this,Manifest.permission
                .WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(this,permissions,1);
        }else {
            requestLocation();
        }*/
        requestLocation();
    }
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }
    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
       // option.setScanSpan(1000);
        option.setOpenGps(true);
        //  option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_DENIED){
                            Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }*/
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
           /* update=MapStatusUpdateFactory.zoomTo(19f);
            baiduMap.animateMapStatus(update);*/
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

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_view, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setSubmitButtonEnabled(true);//让搜索框的提交键显示出来
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);//让键盘的回车键成为搜索
        mSearchView.setBackgroundColor(Color.WHITE);//设置搜索框的背景颜色为白色
        //下面两行去掉默认的搜索框下划线
        mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackground(null);
        mSearchView.findViewById(android.support.v7.appcompat.R.id.submit_area).setBackground(null);
        mSearchView.setIconified(false);//设置搜索框默认展开

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //POI周边搜索
               /* mPoiSearch.searchNearby((new PoiNearbySearchOption().pageCapacity(10)).radius(1000)
                        .location(new LatLng(mcurrentLoction.getLatitude(),mcurrentLoction.getLongitude()))
                        .keyword(query));
                recyclerView.setVisibility(View.GONE);*/
               Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
               intent.putExtra("Query",query);
               startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    recyclerView.setVisibility(View.GONE);
                }else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
                //通过字段的变化是不是为空来选择将recycleview隐藏还是可见
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(mcurrentLoction.getCity())
                        .keyword(newText));
                //模糊搜索
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
//SuggestSearch 监听器
    OnGetSuggestionResultListener suggestionResultListener=new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {
            if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
                return;
                //未找到相关结果
            }else
            {
                final List<SuggestionResult.SuggestionInfo> resl=suggestionResult.getAllSuggestions();
                sugAdapter=new SugAdapter(resl);
                recyclerView.setAdapter(sugAdapter);
                sugAdapter.notifyDataSetChanged();
               /* for(int i=0;i<resl.size();i++)
                {
                    Log.i("result: ","Tag"+resl.get(i).getTag()+"city"+resl.get(i).city+" dis "+resl.get(i).district+"key "+resl.get(i).key);

                }*/
                sugAdapter.setOnItemClickListener(new SugAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        baiduMap.clear();
                        SuggestionResult.SuggestionInfo suggestionInfo=resl.get(position);
                        Toast.makeText(SearchActivity.this,suggestionInfo.getUid(),Toast.LENGTH_SHORT).show();
                        //mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(suggestionInfo.getUid()));
                        if(suggestionInfo.getPt()!=null) {
                            Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
                            intent.putExtra("Latitude",suggestionInfo.getPt().latitude);
                            intent.putExtra("Longitude",suggestionInfo.getPt().longitude);
                            startActivity(intent);
                      /*      baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(suggestionInfo.getPt()));
                            BitmapDescriptor bitmap = BitmapDescriptorFactory
                                    .fromResource(R.drawable.icon_mark);
//构建MarkerOption，用于在地图上添加Marker
                            OverlayOptions option = new MarkerOptions()
                                    .position(suggestionInfo.getPt())
                                    .icon(bitmap);
                            // Bundle bundle = new Bundle();
                            //info必须实现序列化接口
                            // bundle.putParcelable("info", p);
                            //在地图上添加Marker，并显示
                            baiduMap.addOverlay(option);*/
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
            }

            //获取在线建议检索结果
        }

    };


    //创建POI检索监听器
    OnGetPoiSearchResultListener poiSearchResultListener=new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            //使用clear来清除所有覆盖物
            baiduMap.clear();
            if(poiResult.getAllPoi() != null)
            mLatLng=poiResult.getAllPoi().get(0).location;

            if (poiResult.getAllPoi() != null) {
                List<PoiInfo> mData = poiResult.getAllPoi();
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
                    baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
                }


            }

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                /*Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);*/
                finish();
                break;
        }
        return  true;
    }

    protected void onResume() {
        super.onResume();
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
        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
