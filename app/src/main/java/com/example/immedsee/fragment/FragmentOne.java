package com.example.immedsee.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.immedsee.R;
import com.example.immedsee.activity.ResultDetailsActivity;
import com.example.immedsee.activity.RoutePlanActivity;
import com.example.immedsee.activity.SearchActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * DoubleWay on 2019/2/15:16:23
 * 邮箱：13558965844@163.com
 */
public class FragmentOne extends Fragment{
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
    public MyOrientationListener myOrientationListener;
    private int mXDirection;
    private float mCurrentAccracy;
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private BDLocation mCurrentLoction;
    public LocationClient mLocationClient;
    private TextView positionText;
    private FloatingActionButton floatingActionButton;
    private MapView mapView;
    private BaiduMap baiduMap;
    private CardView searchCardView;
    public boolean isFirstLocate=true;
    private Marker marker;
    private BitmapDescriptor bd; //加载标注物的图片
    private String []markerResult=null; //装拆分后的marker的poi的name和uid
    Boolean isSetTrafficCard=false;//判断地图是否开启路况
    Boolean isMapChange=false;//判断地图类型是否切换
    int locaType=0; //用来更改地图的定位模式

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getActivity().getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        /**
         * 初始化方向传感器
         */
        initOritationListener();
        SDKInitializer.initialize(getActivity().getApplicationContext());
        bd = BitmapDescriptorFactory.fromResource(R.drawable.map_long_click_mark);
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                .ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission
                .READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission
                .WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(getActivity(),permissions,1);
        }else {
            requestLocation();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_one,container,false);
        return view;
    }
    @Override
    public void onStart() {
        /**
         * 当从别的活动返回时 重新启动定位
         */
        mLocationClient.start();
        floatingActionButton=getView().findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng ll=new LatLng(mCurrentLatitude,mCurrentLongitude);
                MapStatusUpdate update= MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(update);
                update=MapStatusUpdateFactory.zoomTo(19f);
                baiduMap.animateMapStatus(update);
            }
        });
        searchCardView=getView().findViewById(R.id.toSearch);
        searchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent  intent=new Intent(getActivity().getApplicationContext(), SearchActivity.class);
               intent.putExtra("LoctionCity",mCurrentLoction.getCity());
                startActivity(intent);
            }
        });


        mapView=getView().findViewById(R.id.bmapview);
        //移除百度地图LOGO
//        mapView.removeViewAt(1);
        baiduMap=mapView.getMap();
       // baiduMap.setTrafficEnabled(true);
        baiduMap.setMyLocationEnabled(true);

        baiduMap.setOnMapLongClickListener(mapLongClickListener);//设置长按地图事件监听
        baiduMap.setOnMarkerClickListener(markerClickListener);//设置marker点击事件监听
        baiduMap.setOnMapClickListener(mapClickListener);//设置地图点击事件监听
        /**
         * 开启方向传感器
         */
        myOrientationListener.start();
        super.onStart();
        /**
         * 地图路况开启判断
         */
        CardView setTrafficCard=getView().findViewById(R.id.setTraffic);
        setTrafficCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSetTrafficCard) {
                    baiduMap.setTrafficEnabled(true);
                    ImageView trafficImage = (ImageView) view.findViewById(R.id.traffic_image);
                    trafficImage.setImageResource(R.drawable.traffic_up);
                    isSetTrafficCard=true;
                }else{
                    baiduMap.setTrafficEnabled(false);
                    ImageView trafficImage = (ImageView) view.findViewById(R.id.traffic_image);
                    trafficImage.setImageResource(R.drawable.traffic_down);
                    isSetTrafficCard=false;
                }
            }
        });
        CardView mapChangeCard=getView().findViewById(R.id.mapChange);
        mapChangeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(!isMapChange){
                     baiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                     ImageView mapImage = (ImageView) view.findViewById(R.id.map_image);
                     mapImage.setImageResource(R.drawable.map_change_map_type_satelllte);
                     isMapChange=true;
                 }else {
                     baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                     ImageView mapImage = (ImageView) view.findViewById(R.id.map_image);
                     mapImage.setImageResource(R.drawable.map_change_map_type_normal);
                     isMapChange=false;
                 }
            }
        });
        CardView locaTypeCard=(CardView)getView().findViewById(R.id.loca_type);
        locaTypeCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(locaType==0){
                mCurrentMode= MyLocationConfiguration.LocationMode.COMPASS;
                    BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.arrow);
                    MyLocationConfiguration configuration=new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                    baiduMap.setMyLocationConfiguration(configuration);
                    ImageView locaImage = (ImageView) view.findViewById(R.id.loca_image);
                    locaImage.setImageResource(R.drawable.loca_comepass);
                    Toast.makeText(getActivity().getApplicationContext(),"罗盘模式",Toast.LENGTH_SHORT).show();
                    locaType=1;
                }else  if(locaType==1){
                    mCurrentMode=MyLocationConfiguration.LocationMode.FOLLOWING;
                    BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.arrow);
                    MyLocationConfiguration configuration=new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                    baiduMap.setMyLocationConfiguration(configuration);
                    ImageView locaImage = (ImageView) view.findViewById(R.id.loca_image);
                    locaImage.setImageResource(R.drawable.loca_follow);
                    Toast.makeText(getActivity().getApplicationContext(),"跟随模式",Toast.LENGTH_SHORT).show();
                    locaType=2;
                }else if(locaType==2){
                    mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                    BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                            .fromResource(R.drawable.arrow);
                    MyLocationConfiguration configuration=new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                    baiduMap.setMyLocationConfiguration(configuration);
                    ImageView locaImage = (ImageView) view.findViewById(R.id.loca_image);
                    locaImage.setImageResource(R.drawable.loca_normal);
                    Toast.makeText(getActivity().getApplicationContext(),"普通模式",Toast.LENGTH_SHORT).show();
                    locaType=0;
                }

            }
        });
        CardView toRouteCard=(CardView)getView().findViewById(R.id.to_route);
        toRouteCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRouteIntent=new Intent(getActivity().getApplicationContext(),RoutePlanActivity.class);
                startActivity(toRouteIntent);

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
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setIsNeedAddress(true);
        option.setAddrType("all");
        mLocationClient.setLocOption(option);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_DENIED){
                            Toast.makeText(getActivity(),"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            getActivity().finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(getActivity(),"发生未知错误",Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
                break;
            default:
                break;
        }
    }

    public class MyOrientationListener implements SensorEventListener{
        private Context context;
        private SensorManager sensorManager;
        private Sensor sensor;

        private float lastX ;

        private OnOrientationListener onOrientationListener;

        public MyOrientationListener(Context context)
        {
            this.context = context;
        }
        public void start()
        {
            // 获得传感器管理器
            sensorManager = (SensorManager) context
                    .getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null)
            {
                // 获得方向传感器
                sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            }
            // 注册
            if (sensor != null)
            {//SensorManager.SENSOR_DELAY_UI
                sensorManager.registerListener(this, sensor,
                        SensorManager.SENSOR_DELAY_UI);
            }

        }
        public void stop()
        {
            sensorManager.unregisterListener(this);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION)
            {
                // 这里我们可以得到数据，然后根据需要来处理
                float x = sensorEvent.values[SensorManager.DATA_X];

                if( Math.abs(x- lastX) > 1.0 )
                {
                    onOrientationListener.onOrientationChanged(x);
                }
                lastX = x ;

            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
        public void setOnOrientationListener(OnOrientationListener onOrientationListener)
        {
            this.onOrientationListener = onOrientationListener ;
        }


    }
    public interface OnOrientationListener
    {
        void onOrientationChanged(float x);
    }


    /**
     * 初始化方向传感器
     */
    private void initOritationListener()
    {
        myOrientationListener = new MyOrientationListener(
                getActivity().getApplicationContext());
        myOrientationListener
                .setOnOrientationListener(new OnOrientationListener()
                {
                    @Override
                    public void onOrientationChanged(float x)
                    {
                        mXDirection = (int) x;

                        // 构造定位数据
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(mCurrentAccracy)
                                // 此处设置开发者获取到的方向信息，顺时针0-360
                                .direction(mXDirection)
                                .latitude(mCurrentLatitude)
                                .longitude(mCurrentLongitude).build();
                        // 设置定位数据
                        baiduMap.setMyLocationData(locData);
                        // 设置自定义图标
                        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                                .fromResource(R.drawable.arrow);
                        MyLocationConfiguration configuration=new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                        baiduMap.setMyLocationConfiguration(configuration);

                    }
                });
    }



    public class MyLocationListener extends BDAbstractLocationListener {
    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation||bdLocation.getLocType()==BDLocation.TypeGpsLocation){
            navigateTo(bdLocation);
        }
    }
}

    /**
     * 显示自己的定位
     * @param bdLocation
     */
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
        /**
         * 保存第一次定位的数据
         */
        mCurrentLoction=bdLocation;
        mCurrentAccracy= bdLocation.getRadius();
        mCurrentLatitude= bdLocation.getLatitude();
        mCurrentLongitude= bdLocation.getLongitude();
        /*BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.map_icon);
        MyLocationConfiguration configuration=new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);*/
        baiduMap.setMyLocationData(myLocationData);
      /*  baiduMap.setMyLocationConfiguration(configuration);*/
    }

    /**
     * 地图点击事件监听接口
     */
    BaiduMap.OnMapClickListener mapClickListener=new BaiduMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
               baiduMap.hideInfoWindow();
        }

        @Override
        public boolean onMapPoiClick(MapPoi mapPoi)
        {  //点击地图上的poi添加marker
            baiduMap.clear();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mapPoi.getPosition())//mark出现的位置
                    .icon(bd)       //mark图标
                    .draggable(true)//mark可拖拽
                    //.animateType(MarkerOptions.MarkerAnimateType.drop)//从天而降的方式
                    .animateType(MarkerOptions.MarkerAnimateType.grow)//从地生长的方式
                    ;
            //添加mark
            marker = (Marker) (baiduMap.addOverlay(markerOptions));//地图上添加mark
            /**
             * 将poi的name和uid组合传过去
             */
            marker.setTitle(mapPoi.getName());
            return false;
        }
    };



    /**
     * 地图长按事件监听接口，地图长按添加图标
     */
    BaiduMap.OnMapLongClickListener mapLongClickListener=new BaiduMap.OnMapLongClickListener() {
        @Override
        public void onMapLongClick(LatLng latLng) {
            baiduMap.clear();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)//mark出现的位置
                    .icon(bd)       //mark图标
                    .draggable(true)//mark可拖拽
                    //.animateType(MarkerOptions.MarkerAnimateType.drop)//从天而降的方式
                    .animateType(MarkerOptions.MarkerAnimateType.grow)//从地生长的方式
                    ;
            //添加mark
            marker = (Marker) (baiduMap.addOverlay(markerOptions));//地图上添加mark

        }
    };

    /**
     * 点击marker显示infowindow窗口信息
     */
    BaiduMap.OnMarkerClickListener markerClickListener=new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
             final LatLng markerLaLng=marker.getPosition();
            Log.d("ResultDetails", "onMarkerClick: "+markerLaLng.toString());
            GeoCoder geoCoder = GeoCoder.newInstance();
            //设置反地理编码位置坐标
            ReverseGeoCodeOption option = new ReverseGeoCodeOption();
            option.location(markerLaLng);
            geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                @Override
                public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                }
                @Override
                public void onGetReverseGeoCodeResult(final ReverseGeoCodeResult reverseGeoCodeResult) {
                    final ReverseGeoCodeResult.AddressComponent addressDetail = reverseGeoCodeResult.getAddressDetail();
                    View view=View.inflate(getActivity().getApplicationContext(),R.layout.infowindow,null);
                    TextView agentName=(TextView)view.findViewById(R.id.agent_name);
                    TextView agentAaddr=(TextView)view.findViewById(R.id.agent_addr);
                    TextView detailsText=(TextView) view.findViewById(R.id.details);
                    TextView navigationText=(TextView)view.findViewById(R.id.navigation);
                    /**
                     * 判断传过来的marker是长按地图产生的还是点击poi产生的，如果是前者就直接赋值，
                     * 如果是后者就拆分name和uid
                     */
                    if(marker.getTitle() == null){
                        marker.setTitle(reverseGeoCodeResult.getAddress());
                    }
                    agentName.setText(addressDetail.city);
                    agentAaddr.setText(reverseGeoCodeResult.getAddress());
                    detailsText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent toResultDetails = new Intent(getActivity().getApplicationContext(), ResultDetailsActivity.class);
                            toResultDetails.putExtra("ResultName", marker.getTitle());
                            toResultDetails.putExtra("ResultCity",addressDetail.city);
                            toResultDetails.putExtra("ResultLongitude", markerLaLng.longitude);
                            toResultDetails.putExtra("ResultLatitude", markerLaLng.latitude);
                            toResultDetails.putExtra("ResultAddress",reverseGeoCodeResult.getAddress());
                            startActivity(toResultDetails);

                        }
                    });
                    navigationText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent toRoutePlanIntent=new Intent(getActivity().getApplicationContext(), RoutePlanActivity.class);
                            toRoutePlanIntent.putExtra("ResultName", marker.getTitle());
                            toRoutePlanIntent.putExtra("ResultCity",addressDetail.city);
                            toRoutePlanIntent.putExtra("ResultLongitude", markerLaLng.longitude);
                            toRoutePlanIntent.putExtra("ResultLatitude", markerLaLng.latitude);
                            toRoutePlanIntent.putExtra("ResultAddress",reverseGeoCodeResult.getAddress());
                            startActivity(toRoutePlanIntent);
                        }
                    });
                    InfoWindow infoWindow=new InfoWindow(view,markerLaLng,-60);
                    //显示信息窗口
                    baiduMap.showInfoWindow(infoWindow);
                    Log.d("that", "反地理编码信息 "+reverseGeoCodeResult.getAddress());
                }
            });

            geoCoder.reverseGeoCode(option);
            return false;
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        /**
         * 关闭方向传感器
         */
        myOrientationListener.stop();
        /**
         * 当从当前跳转到另一个活动的时候停止定位
         */
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

}
