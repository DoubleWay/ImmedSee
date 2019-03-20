package com.example.immedsee;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
    }
}
