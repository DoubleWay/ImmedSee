package com.example.immedsee;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.RequiresPermission;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

public class SearchActivity extends AppCompatActivity {
    private SearchView mSearchView;
    private PoiSearch mPoiSearch;
    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        textView=(TextView)findViewById(R.id.test_view);
        mPoiSearch=PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchResultListener);//设置POI检索监听器
        Toolbar toolbar=(Toolbar)findViewById(R.id.search_toolBar);
        toolbar.setTitle("搜索");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
   /*     mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("杭州")
                        .keyword("沙县小吃")
                        .pageNum(10));
            }
        });*/
        //设置搜索文本监听
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
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city("北京")
                        .keyword("沙县小吃")
                        .pageNum(10));
               // Toast.makeText(SearchActivity.this,query,Toast.LENGTH_SHORT).show();
                /*textView.setText(query);*/
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }




    //创建POI检索监听器
    OnGetPoiSearchResultListener poiSearchResultListener=new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            String poiname = poiResult.getAllPoi().get(0).name;
            String poiadd = poiResult.getAllPoi().get(0).address;
            String idString = poiResult.getAllPoi().get(0).uid;
            textView.setText(
                    "第一条结果是：\n名称＝［"+
                            poiname+
                            "］\nID = ["+
                            idString
                            + "] \n地址＝［"+
                            poiadd+
                            "］");
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
                Intent intent=new Intent(this,MainActivity.class);
                startActivity(intent);
                break;
        }
        return  true;
    }
}
