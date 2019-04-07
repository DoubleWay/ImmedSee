package com.example.immedsee.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteLine;
import com.baidu.mapapi.search.route.BikingRoutePlanOption;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteLine;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.example.immedsee.R;
import com.example.immedsee.overly.BikingRouteOverlay;
import com.example.immedsee.overly.DrivingRouteOverlay;
import com.example.immedsee.overly.OverlayManager;
import com.example.immedsee.overly.TransitRouteOverlay;
import com.example.immedsee.overly.WalkingRouteOverlay;

import java.util.ArrayList;

public class RoutePlanActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener {
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    int nodeIndex = -1; // 节点索引,供浏览节点时使用
    RouteLine route = null;  //路线
    OverlayManager routeOverlay = null;  //该类提供一个能够显示和管理多个Overlay的基类
    private TextView popupText = null; // 泡泡view

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    MapView mMapView = null;    // 地图View
    BaiduMap mBaidumap = null;
    // 搜索相关
    RoutePlanSearch mSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    public LocationClient mLocationClient; //定位模块
    public boolean isFirstLocate=true; //判断是不是第一次定位
    private String resultCity; //接收路线搜索的城市
    private String resultAddress; //接收路线搜索的目标地址
    private String resultName; //接收路线搜索的目标
    private EditText editSt ;
    private EditText editEn ;  //初始化起终点信息输入框
    private  String locationAddress;//保留定位留下来的地址信息
    private String locationCity;
    private double resultLatitude; //接收目标地址的经纬度
    private double resultLongitude;
    private double mLatitude;//保存初次定位后的位置信息
   private double mLongitude;
   private BDLocation mBDLocation;

    /*导航起终点Marker，可拖动改变起终点的坐标*/
    private LatLng startPt,endPt;

    BikeNaviLaunchParam bikeParam;
    WalkNaviLaunchParam walkParam;

    private static boolean isPermissionRequested = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_route_plan);
        CharSequence titleLable = "路线规划功能";
        setTitle(titleLable);

        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaidumap = mMapView.getMap();
        requestPermission();//权限申请
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener (new MyLocationListener());
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        // 地图点击事件处理
        mBaidumap.setOnMapClickListener(this);
        // 初始化搜索模块，注册事件监听
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(this);

        final Intent intent=getIntent();
        resultCity=intent.getStringExtra("ResultCity");
        resultAddress=intent.getStringExtra("ResultAddress");
        resultName=intent.getStringExtra("ResultName");
        resultLatitude=intent.getDoubleExtra("ResultLatitude",0);
        resultLongitude=intent.getDoubleExtra("ResultLongitude",0);
        locationAddress=intent.getStringExtra("LocationAddress");
        mLatitude=intent.getDoubleExtra("mLatitude",0);
        mLongitude=intent.getDoubleExtra("mLongitude",0);
        requestLocation(); //开启定位
        editSt = (EditText) findViewById(R.id.start);
        editEn = (EditText) findViewById(R.id.end);
        editEn.setText(resultAddress);
        editSt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=new Intent(RoutePlanActivity.this,RouteSearchActivity.class);
                intent1.putExtra("LocationCity",locationCity);
                intent1.putExtra("LocationAddress",locationAddress);
                intent1.putExtra("mLatitude",mLatitude);
                intent1.putExtra("mLongitude",mLongitude);
                startActivity(intent1);
            }
        });
        editEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(RoutePlanActivity.this,RouteSearchActivity.class);
                intent2.putExtra("LocationCity",locationCity);
                intent2.putExtra("LocationAddress",locationAddress);
                intent2.putExtra("mLatitude",mLatitude);
                intent2.putExtra("mLongitude",mLongitude);
                startActivity(intent2);
            }
        });

        if(resultAddress!=null) {
            editEn.setSelection(resultAddress.length());//将光标移到最后
        }
        Log.d("RoutePlan", "resultLatitude: "+resultLatitude);
        Log.d("RoutePlan", "resultLongitude: "+resultLongitude);
        Log.d("RoutePlan", "mLatitude: "+mLatitude);
        Log.d("RoutePlan", "mLongitude: "+mLongitude);


    }

    /**
     * 定位初始化方法
     */
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
        // option.setScanSpan(5000);
        option.setOpenGps(true);
        //  option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);

        option.setAddrType("all");
        mLocationClient.setLocOption(option);
    }

    /**
     * 地图定位监听
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if(bdLocation.getLocType()==BDLocation.TypeNetWorkLocation||bdLocation.getLocType()==BDLocation.TypeGpsLocation){
                mBDLocation=bdLocation;
                if(mLatitude==0&&mLongitude==0) {
                    mLatitude = bdLocation.getLatitude();
                    mLongitude = bdLocation.getLongitude();
                }
                //解决bdLocation.getAddrStr()过段时间值变为null的问题
                if(isFirstLocate){
                    if(locationAddress==null) {
                        locationAddress = bdLocation.getAddrStr();
                    }
                locationCity=bdLocation.getCity();
                isFirstLocate=false;
                }
                Log.d("RoutePlan", "onReceiveLocation: "+locationAddress);
                Log.d("RoutePlan", "onReceiveLocation: "+bdLocation.getAddrStr());
                Log.d("RoutePlan", "mLatitude: "+mLatitude);
                Log.d("RoutePlan", "mLongitude: "+mLongitude);
                editSt.setText(locationAddress);
                editSt.setSelection(locationAddress.length());//将光标移到最后
                startPt = new LatLng(mLatitude,mLongitude);//初始步行导航起终点
                endPt = new LatLng(resultLatitude, resultLongitude);
//构造Bike,WalkNaviLaunchParam
                walkParam = new WalkNaviLaunchParam().stPt(startPt).endPt(endPt);
                bikeParam = new BikeNaviLaunchParam().stPt(startPt).endPt(endPt);
                navigateTo(bdLocation);
            }

        }
    }

    /**
     *移动到自己的位置
     */
    private void navigateTo(BDLocation bdLocation) {

            LatLng ll = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            mBaidumap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(17f);
            mBaidumap.animateMapStatus(update);


//保存定位的信息

    }
    /**按钮的点击方法
     * 发起路线规划搜索示例
     *
     * @param v
     */
    public void searchButtonProcess(View v) {
        PlanNode stNode;
        PlanNode enNode;
        // 重置浏览节点的路线数据
        route = null;
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);
        mBaidumap.clear(); //清除地图上画的路线
        // 处理搜索按钮响应
        Log.d("RoutePlan", "searchButtonProcess: "+editEn.getText().toString());

        // 设置起终点信息，对于tranist search 来说，城市名无意义
        if(resultLatitude==0&&resultLongitude==0) {
            resultCity=locationCity;
             stNode = PlanNode.withCityNameAndPlaceName(resultCity, editSt.getText().toString());//通过地名和城市名确定出行节点信息
             enNode = PlanNode.withCityNameAndPlaceName(resultCity, editEn.getText().toString());
        }else {
         stNode = PlanNode.withLocation(new LatLng(mLatitude,mLongitude));//通过经纬度确定出行节点信息
         enNode = PlanNode.withLocation(new LatLng(resultLatitude,resultLongitude));
        }

        // 实际使用中请对起点终点城市进行正确的设定
        if (v.getId() == R.id.drive) {
            //驾车行驶
            mSearch.drivingSearch((new DrivingRoutePlanOption())
                    .from(stNode).to(enNode));
        } else if (v.getId() == R.id.transit) {
            //公交行驶
            mSearch.transitSearch((new TransitRoutePlanOption())
                    .from(stNode).city(resultCity).to(enNode));
        } else if (v.getId() == R.id.walk) {
            //步行
            mSearch.walkingSearch((new WalkingRoutePlanOption())
                    .from(stNode).to(enNode));
        } else if (v.getId() == R.id.bike) {
            //骑行
            mSearch.bikingSearch((new BikingRoutePlanOption())
                    .from(stNode).to(enNode));
        }
    }

    /**
     * 节点浏览示例
     *
     * @param v
     */
    public void nodeClick(View v) {
        if (route == null || route.getAllStep() == null) {
            //如果路线的信息为空或者路节点的信息为空
            return;
        }
        if (nodeIndex == -1 && v.getId() == R.id.pre) {
            return;
        }
        // 设置节点索引
        if (v.getId() == R.id.next) {
            if (nodeIndex < route.getAllStep().size() - 1) {
                nodeIndex++;
            } else {
                return;
            }
        } else if (v.getId() == R.id.pre) {
            if (nodeIndex > 0) {
                nodeIndex--;
            } else {
                return;
            }
        }
        // 获取节结果信息
        LatLng nodeLocation = null;  //经纬度
        String nodeTitle = null;  //节点title(驾程信息)
        Object step = route.getAllStep().get(nodeIndex);
        /**
         * instanceof 判断左边的对象是不是右边的类的实例化对象
         */
        if (step instanceof DrivingRouteLine.DrivingStep) { //DrivingStep 一个驾车路段
            //驾车
            nodeLocation = ((DrivingRouteLine.DrivingStep) step).getEntrance().getLocation(); //驾车路段入口的经纬度
            nodeTitle = ((DrivingRouteLine.DrivingStep) step).getInstructions();//驾车路段总体信息
        } else if (step instanceof WalkingRouteLine.WalkingStep) {
            //步行
            nodeLocation = ((WalkingRouteLine.WalkingStep) step).getEntrance().getLocation();
            nodeTitle = ((WalkingRouteLine.WalkingStep) step).getInstructions();
        } else if (step instanceof TransitRouteLine.TransitStep) {
            //公交
            nodeLocation = ((TransitRouteLine.TransitStep) step).getEntrance().getLocation();
            nodeTitle = ((TransitRouteLine.TransitStep) step).getInstructions();
        } else if (step instanceof BikingRouteLine.BikingStep) {
            //骑行
            nodeLocation = ((BikingRouteLine.BikingStep) step).getEntrance().getLocation();
            nodeTitle = ((BikingRouteLine.BikingStep) step).getInstructions();
        }

        if (nodeLocation == null || nodeTitle == null) {
            return;
        }
        // 移动节点至中心
        mBaidumap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
        // show popup
        //这个是在每次点击左或者右(也就是前后节点，这个时候就会在站点的上方弹出一个infowindow,显示路线换成信息)
        popupText = new TextView(RoutePlanActivity.this);
        popupText.setBackgroundResource(R.drawable.popup);
        popupText.setTextColor(0xFF000000);
        popupText.setText(nodeTitle);
        mBaidumap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

    }
    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
 /*       if (routeOverlay == null) {
            return;
        }
        if (useDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this,
                    "将使用系统起终点图标",
                    Toast.LENGTH_SHORT).show();

        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this,
                    "将使用自定义起终点图标",
                    Toast.LENGTH_SHORT).show();

        }
        useDefaultIcon = !useDefaultIcon;
        routeOverlay.removeFromMap();  //将所有overlay从地图中移出
        routeOverlay.addToMap();  //添加*/
  if(v.getId()==R.id.walk_guide){
        walkParam.extraNaviMode(0);
        startWalkNavi();
  } else if(v.getId()==R.id.bike_guide){
         startBikeNavi();
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    /**
     * 得到步行路线的结果
     * @param result
     */
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
             //result.getSuggestAddrInfo();
            return;
        }
        //如果检索结果正常返回
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            //设置路线数据
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();  //将所有overlay添加到地图中
            overlay.zoomToSpan();//缩放地图
        }

    }

    /**
     * 得到公交车的驾驶路线
     * @param result
     */
    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {

        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            TransitRouteOverlay overlay = new TransitRouteOverlay(mBaidumap);
            mBaidumap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            //设置路线数据
            overlay.setData(result.getRouteLines().get(0));
            overlay.addToMap();  //将所有overlay添加到地图中
            overlay.zoomToSpan();//缩放地图
        }
    }


    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    /**
     * 得到驾车路线
     * @param result
     */
    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = result.getRouteLines().get(0);
            DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(result.getRouteLines().get(0));  //设置路线数据
            overlay.addToMap(); //将所有overlay添加到地图中
            overlay.zoomToSpan();//缩放地图
        }
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    /**
     * 得到骑行的路线
     *
     * @param bikingRouteResult
     */
    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {
        if (bikingRouteResult == null || bikingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(RoutePlanActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            return;
        }
        if (bikingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            nodeIndex = -1;
            mBtnPre.setVisibility(View.VISIBLE);
            mBtnNext.setVisibility(View.VISIBLE);
            route = bikingRouteResult.getRouteLines().get(0);
            BikingRouteOverlay overlay = new BikingRouteOverlay(mBaidumap);
            routeOverlay = overlay;
            mBaidumap.setOnMarkerClickListener(overlay);
            overlay.setData(bikingRouteResult.getRouteLines().get(0));//设置路线数据
            overlay.addToMap();//将所有overlay添加到地图中
            overlay.zoomToSpan(); //缩放地图
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaidumap.hideInfoWindow();
    }

    @Override
    public boolean onMapPoiClick(MapPoi poi) {
        return false;
    }
    /**
     * 开始骑行导航
     */
    private void startBikeNavi() {
        Log.d("guide", "startBikeNavi");
        try {
            BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d("guide", "BikeNavi engineInitSuccess");
                    routePlanWithBikeParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d("guide", "BikeNavi engineInitFail");
                }
            });
        } catch (Exception e) {
            Log.d("guide", "startBikeNavi Exception");
            e.printStackTrace();
        }
    }
    /**
     * 发起骑行导航算路
     */
    private void routePlanWithBikeParam() {
        BikeNavigateHelper.getInstance().routePlanWithParams(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("guide", "BikeNavi onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d("guide", "BikeNavi onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(RoutePlanActivity.this, BNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Toast.makeText(RoutePlanActivity.this,"对不起，没有推荐路线",Toast.LENGTH_SHORT).show();
                Log.d("guide", "BikeNavi onRoutePlanFail");
                Log.d("guide", error.toString());
            }

        });
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        Log.d("guide", "startBikeNavi");
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d("guide", "WalkNavi engineInitSuccess");
                    routePlanWithWalkParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d("guide", "WalkNavi engineInitFail");
                }
            });
        } catch (Exception e) {
            Log.d("guide", "startBikeNavi Exception");
            e.printStackTrace();
        }
    }
    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithParams(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d("guide", "WalkNavi onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d("View", "onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(RoutePlanActivity.this, WNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d("guide", error.toString());
                Toast.makeText(RoutePlanActivity.this,"对不起，没有推荐路线",Toast.LENGTH_SHORT).show();
                Log.d("guide", "WalkNavi onRoutePlanFail");
            }

        });
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {

            isPermissionRequested = true;

            ArrayList<String> permissions = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (permissions.size() == 0) {
                return;
            } else {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
            }
        }
    }
/*
    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.start();
    }*/

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mSearch.destroy();
        mMapView.onDestroy();
        super.onDestroy();
    }

}
