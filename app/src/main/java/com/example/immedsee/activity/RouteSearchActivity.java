package com.example.immedsee.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.immedsee.R;
import com.example.immedsee.adapter.SugAdapter;

import java.util.List;

/**
 * 对路线规划的起终点进行suggest搜索
 */
public class RouteSearchActivity extends AppCompatActivity {
    private SearchView searchViewSt;
    private SearchView searchViewEn;
    private SuggestionSearch mSuggestionSearch;
    private RecyclerView routePlanRecyclerStView;
    private RecyclerView routePlanRecyclerEnView;
    private SugAdapter sugAdapter;
    private String locationCity;
    private int searchTag;
    private Boolean routeSearchSubmit;
    private  String locationAddress;//保留定位留下来的地址信息
    private String resultAddress; //接收路线搜索的目标地址
    private double resultLatitude; //接收目标地址的经纬度
    private double resultLongitude;
    private double mLatitude;//保存初次定位后的位置信息
    private double mLongitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_route_search);
        Intent intent=getIntent();
        locationCity=intent.getStringExtra("LocationCity");
        locationAddress=intent.getStringExtra("LocationAddress");
        mLatitude=intent.getDoubleExtra("mLatitude",0);
        mLongitude=intent.getDoubleExtra("mLongitude",0);
        Toolbar toolbar=(Toolbar)findViewById(R.id.route_search_toolBar);
        toolbar.setTitle("起终点搜索");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        routePlanRecyclerStView=(RecyclerView)findViewById(R.id.route_suggest_search_st_list);
        routePlanRecyclerEnView=(RecyclerView)findViewById(R.id.route_suggest_search_en_list);
        GridLayoutManager layoutManagerSt=new GridLayoutManager(getApplicationContext(),1);
        GridLayoutManager layoutManagerEn=new GridLayoutManager(getApplicationContext(),1);
        routePlanRecyclerStView.setLayoutManager(layoutManagerSt);
        routePlanRecyclerEnView.setLayoutManager(layoutManagerEn);

        onSearch();

        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);//创建Sug搜索监听器
    }

    public void onSearch(){
        searchViewSt=(SearchView)findViewById(R.id.search_st);
        searchViewSt.setQuery(locationAddress,false);
        searchViewEn=(SearchView)findViewById(R.id.search_en);
        /*searchViewSt.setSubmitButtonEnabled(true);//让搜索框的提交键显示出来
        searchViewEn.setSubmitButtonEnabled(true);//让搜索框的提交键显示出来*/
        searchViewSt.setImeOptions(EditorInfo.IME_ACTION_SEARCH);//让键盘的回车键成为搜索
        searchViewEn.setImeOptions(EditorInfo.IME_ACTION_SEARCH);//让键盘的回车键成为搜索
        searchViewSt.setBackgroundColor(Color.WHITE);//设置搜索框的背景颜色为白色
        searchViewEn.setBackgroundColor(Color.WHITE);//设置搜索框的背景颜色为白色
        searchViewSt.setIconified(false);//设置搜索框默认展开
        searchViewEn.setIconified(false);//设置搜索框默认展开
        //下面两行去掉默认的搜索框下划线
        searchViewSt.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackground(null);
        searchViewSt.findViewById(android.support.v7.appcompat.R.id.submit_area).setBackground(null);
        searchViewEn.findViewById(android.support.v7.appcompat.R.id.search_plate).setBackground(null);
        searchViewEn.findViewById(android.support.v7.appcompat.R.id.submit_area).setBackground(null);

        searchViewSt.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    routePlanRecyclerStView.setVisibility(View.GONE);
                }else {
                    routePlanRecyclerStView.setVisibility(View.VISIBLE);
                }
                searchTag=1;
                //通过字段的变化是不是为空来选择将recycleview隐藏还是可见
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(locationCity)
                        .keyword(newText));
                //模糊搜索
                return false;
            }
        });
        searchViewEn.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    routePlanRecyclerEnView.setVisibility(View.GONE);
                }else {
                    routePlanRecyclerEnView.setVisibility(View.VISIBLE);
                }
                searchTag=2;
                //通过字段的变化是不是为空来选择将recycleview隐藏还是可见
                mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                        .city(locationCity)
                        .keyword(newText));
                //模糊搜索
                return false;
            }
        });
    }
  public void searchSubmit(View v){
        if(locationAddress!=null&&resultAddress!=null){
            Intent toRoutePlanIntent=new Intent(RouteSearchActivity.this,RoutePlanActivity.class);
            toRoutePlanIntent.putExtra("LocationAddress",locationAddress);
            toRoutePlanIntent.putExtra("mLatitude",mLatitude);
            toRoutePlanIntent.putExtra("mLongitude",mLongitude);
            toRoutePlanIntent.putExtra("ResultAddress",resultAddress);
            toRoutePlanIntent.putExtra("ResultLatitude",resultLatitude);
            toRoutePlanIntent.putExtra("ResultLongitude",resultLongitude);
            startActivity(toRoutePlanIntent);
        }else {
            Toast.makeText(RouteSearchActivity.this,"请输入起点或终点",Toast.LENGTH_SHORT).show();
        }
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
                if(searchTag==1){
                routePlanRecyclerStView.setAdapter(sugAdapter);
                sugAdapter.notifyDataSetChanged();
                sugAdapter.setOnItemClickListener(new SugAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            SuggestionResult.SuggestionInfo suggestionInfo=resl.get(position);
                            if(suggestionInfo.getPt()!=null) {
                                searchViewSt.clearFocus();
                                searchViewEn.clearFocus();
                                searchViewSt.setQuery(suggestionInfo.getKey(), false);
                                locationAddress = suggestionInfo.getKey();
                                mLatitude = suggestionInfo.getPt().latitude;
                                mLongitude = suggestionInfo.getPt().longitude;

                                routePlanRecyclerStView.setVisibility(View.GONE);

                            }

                        }
                    });
                }else if(searchTag==2){
                    routePlanRecyclerEnView.setAdapter(sugAdapter);
                    sugAdapter.notifyDataSetChanged();
                    sugAdapter.setOnItemClickListener(new SugAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            SuggestionResult.SuggestionInfo suggestionInfo=resl.get(position);
                            if(suggestionInfo.getPt()!=null) {
                                searchViewSt.clearFocus();
                                searchViewEn.clearFocus();
                                searchViewEn.setQuery(suggestionInfo.getKey(), false);
                                resultAddress = suggestionInfo.getKey();
                                resultLatitude = suggestionInfo.getPt().latitude;
                                resultLongitude = suggestionInfo.getPt().longitude;
                                routePlanRecyclerEnView.setVisibility(View.GONE);

                            }
                        }
                    });
                }

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("routeSearch", "onItemClick: hhhhhhhh");
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
