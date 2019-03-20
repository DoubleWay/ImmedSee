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
    private String locationCity;
    private SearchView mSearchView;
    private SuggestionSearch mSuggestionSearch;
    private SugAdapter sugAdapter;
    private RecyclerView recyclerView;
    private List<SuggestionResult.SuggestionInfo> SuggestionInfoList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
       //创建显示模糊搜索结果的列表
        recyclerView=(RecyclerView)findViewById(R.id.suggest_search_list);
        GridLayoutManager layoutManager=new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);//创建Sug搜索监听器

        Toolbar toolbar=(Toolbar)findViewById(R.id.search_toolBar);
        toolbar.setTitle("搜索");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        locationCity=intent.getStringExtra("LoctionCity");
        Log.d("this", "onCreate: "+locationCity);

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
                        .city(locationCity)
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
                        SuggestionResult.SuggestionInfo suggestionInfo=resl.get(position);
                        if(suggestionInfo.getPt()!=null) {
                            Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
                            intent.putExtra("Latitude",suggestionInfo.getPt().latitude);
                            intent.putExtra("Longitude",suggestionInfo.getPt().longitude);
                            startActivity(intent);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }
                });
            }

            //获取在线建议检索结果
        }

    };




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return  true;
    }

}
