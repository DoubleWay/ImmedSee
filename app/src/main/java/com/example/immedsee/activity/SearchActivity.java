package com.example.immedsee.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.immedsee.R;
import com.example.immedsee.adapter.SugAdapter;
import com.example.immedsee.dao.SearchHistory;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchActivity extends AppCompatActivity  implements View.OnClickListener{
    private String locationCity;
    private double resultLocationLongitude;
    private double resultLocationLatitude;
    private SearchView mSearchView;
    private SuggestionSearch mSuggestionSearch;
    private SugAdapter sugAdapter;
    private RecyclerView recyclerView;
    private RecyclerView searchHistoryRecycler;

    private List<SuggestionResult.SuggestionInfo> SuggestionInfoList;
    private ImageView imageViewFood;
    private ImageView imageViewHotel;
    private ImageView imageViewBus;
    private ImageView imageViewBank;
    private ImageView imageViewGass;
    private ImageView imageViewView;
    private ImageView imageViewKTV;
    private ImageView imageViewSupermaket;
    private TextView historyText;
    private TextView removeHistoryText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_search);

       //创建显示模糊搜索结果的列表
        recyclerView=(RecyclerView)findViewById(R.id.suggest_search_list);
        searchHistoryRecycler=(RecyclerView)findViewById(R.id.search_history);
        GridLayoutManager layoutManager=new GridLayoutManager(getApplicationContext(),1);
        GridLayoutManager layoutManagerhis=new GridLayoutManager(getApplicationContext(),1);
        recyclerView.setLayoutManager(layoutManager);
        searchHistoryRecycler.setLayoutManager(layoutManagerhis);



        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(suggestionResultListener);//创建Sug搜索监听器

        Toolbar toolbar=(Toolbar)findViewById(R.id.search_toolBar);
        toolbar.setTitle("搜索");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent=getIntent();
        locationCity=intent.getStringExtra("LoctionCity");
        /**
         * 接受目标地点周边搜索的坐标
         */
        resultLocationLatitude=intent.getDoubleExtra("ResultLocationLatitude",0);
        resultLocationLongitude=intent.getDoubleExtra("ResultLocationLongitude",0);
       /* Log.d("this", "SearchactivityLongitude: "+resultLocationLongitude);
        Log.d("this", "SearchactivityLatitude: "+resultLocationLatitude);*/

        Log.d("this", "onCreate: "+locationCity);
        historyText=(TextView)findViewById(R.id.history_text);
        removeHistoryText=(TextView)findViewById(R.id.remove_history_text);
        imageViewFood=(ImageView)findViewById(R.id.search_food);
        imageViewHotel=(ImageView)findViewById(R.id.search_hotel);
        imageViewBus=(ImageView)findViewById(R.id.search_bus);
        imageViewBank=(ImageView)findViewById(R.id.search_bank);
        imageViewGass=(ImageView)findViewById(R.id.search_gasstation);
        imageViewView=(ImageView)findViewById(R.id.search_view);
        imageViewKTV=(ImageView)findViewById(R.id.search_ktv);
        imageViewSupermaket=(ImageView)findViewById(R.id.search_supermarket);

        imageViewFood.setOnClickListener(this);
        imageViewHotel.setOnClickListener(this);
        imageViewBus.setOnClickListener(this);
        imageViewBank.setOnClickListener(this);
        imageViewGass.setOnClickListener(this);
        imageViewView.setOnClickListener(this);
        imageViewKTV.setOnClickListener(this);
        imageViewSupermaket.setOnClickListener(this);
        removeHistoryText.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //从数据库进行遍历，将历史纪录打印出来
        List<SearchHistory> historyList= LitePal.findAll(SearchHistory.class);
        if(historyList.size()>0){
            historyText.setVisibility(View.VISIBLE);
            removeHistoryText.setVisibility(View.VISIBLE);
        }
        Collections.reverse(historyList);//重要，将list的排列顺序倒置
        final List<SuggestionResult.SuggestionInfo> historySuggest=new ArrayList<>();
        for(SearchHistory history:historyList){
            SuggestionResult.SuggestionInfo mySuggestionInfo=new SuggestionResult.SuggestionInfo();
            mySuggestionInfo.setKey(history.getName());
            mySuggestionInfo.setPt(new LatLng(history.getLatitude(),history.getLongitude()));
            mySuggestionInfo.setUid(history.getUid());
            mySuggestionInfo.setCity(history.getCity());
            mySuggestionInfo.setDistrict(history.getDistrict());
            historySuggest.add(mySuggestionInfo);
        }
        sugAdapter=new SugAdapter(historySuggest);
        searchHistoryRecycler.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
        sugAdapter.setOnItemClickListener(new SugAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SuggestionResult.SuggestionInfo suggestionInfo=historySuggest.get(position);

                if(suggestionInfo.getPt()!=null) {
                    //对数据库进行操作，将推荐搜索点击历史存入数据库
                    Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
                    intent.putExtra("Latitude",suggestionInfo.getPt().latitude);
                    intent.putExtra("Longitude",suggestionInfo.getPt().longitude);
                    intent.putExtra("locationUid",suggestionInfo.getUid());
                    intent.putExtra("SuggestKey",suggestionInfo.key);
                    Log.d("search", "onItemClick: "+suggestionInfo.key);
                    startActivity(intent);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
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
            public boolean onQueryTextSubmit(final String query) {
               Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
                /**
                 * 将要进行周边搜索的目标地点坐标传给搜索页面
                 */
                intent.putExtra("ResultLocationLongitude", resultLocationLongitude);
                intent.putExtra("ResultLocationLatitude", resultLocationLatitude);
                intent.putExtra("Query", query);
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
                        boolean isAddHistory=true; //判断是否加入搜索历史
                        SuggestionResult.SuggestionInfo suggestionInfo=resl.get(position);


                        if(suggestionInfo.getPt()!=null) {
                            List<SearchHistory> historyListCompare= LitePal.findAll(SearchHistory.class);
                            for(SearchHistory history:historyListCompare){
                                if(suggestionInfo.key.equals(history.getName())){
                                    isAddHistory=false;
                                    break;
                                }
                            }
                        //对数据库进行操作，将推荐搜索点击历史存入数据库
                          if(isAddHistory) { //去重判断
                              SearchHistory history = new SearchHistory();
                              history.setLatitude(suggestionInfo.getPt().latitude);
                              history.setLongitude(suggestionInfo.getPt().longitude);
                              history.setUid(suggestionInfo.getUid());
                              history.setName(suggestionInfo.key);
                              history.setCity(suggestionInfo.getCity());
                              history.setDistrict(suggestionInfo.getDistrict());
                              history.save();
                          }
                            Intent intent=new Intent(SearchActivity.this,SearchResultActivity.class);
                            intent.putExtra("Latitude",suggestionInfo.getPt().latitude);
                            intent.putExtra("Longitude",suggestionInfo.getPt().longitude);
                            intent.putExtra("locationUid",suggestionInfo.getUid());
                            intent.putExtra("SuggestKey",suggestionInfo.key);
                            Log.d("search", "onItemClick: "+suggestionInfo.key);
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

    @Override
    public void onClick(View view) {
         switch (view.getId()){
             case R.id.search_food:
                 Intent intentFood=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentFood.putExtra("Query","美食");
                 intentFood.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentFood.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentFood);
              break;
             case R.id.search_hotel:
                 Intent intentHotel=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentHotel.putExtra("Query","酒店");
                 intentHotel.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentHotel.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentHotel);
                 break;
             case R.id.search_bus:
                 Intent intentBus=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentBus.putExtra("Query","公交站");
                 intentBus.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentBus.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentBus);
                 break;
             case R.id.search_bank:
                 Intent intentBank=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentBank.putExtra("Query","银行");
                 intentBank.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentBank.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentBank);
                 break;
             case R.id.search_gasstation:
                 Intent intentGass=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentGass.putExtra("Query","加油站");
                 intentGass.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentGass.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentGass);
                 break;
             case R.id.search_view:
                 Intent intentView=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentView.putExtra("Query","景点");
                 intentView.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentView.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentView);
                 break;
             case R.id.search_ktv:
                 Intent intentKtv=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentKtv.putExtra("Query","KTV");
                 intentKtv.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentKtv.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentKtv);
                 break;
             case R.id.search_supermarket:
                 Intent intentsupermarket=new Intent(SearchActivity.this,SearchResultActivity.class);
                 intentsupermarket.putExtra("Query","超市");
                 intentsupermarket.putExtra("ResultLocationLongitude",resultLocationLongitude);
                 intentsupermarket.putExtra("ResultLocationLatitude",resultLocationLatitude);
                 startActivity(intentsupermarket);
                 break;
             case R.id.remove_history_text:
                 /**
                  * 点击弹出dialog确认是否清除历史纪录
                  */
                 AlertDialog.Builder builder=new AlertDialog.Builder(SearchActivity.this);
                 builder.setTitle("确认");
                 builder.setMessage("你将清空历史纪录");
                 builder.setCancelable(true);
                 builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         LitePal.deleteAll(SearchHistory.class);
                         //先删除再查询
                         List<SearchHistory> historyList= LitePal.findAll(SearchHistory.class);
                         List<SuggestionResult.SuggestionInfo> historySuggest=new ArrayList<>();
                         for(SearchHistory history:historyList){
                             SuggestionResult.SuggestionInfo mySuggestionInfo=new SuggestionResult.SuggestionInfo();
                             mySuggestionInfo.setKey(history.getName());
                             mySuggestionInfo.setPt(new LatLng(history.getLatitude(),history.getLongitude()));
                             mySuggestionInfo.setUid(history.getUid());
                             mySuggestionInfo.setCity(history.getCity());
                             mySuggestionInfo.setDistrict(history.getDistrict());
                             historySuggest.add(mySuggestionInfo);
                         }
                         sugAdapter=new SugAdapter(historySuggest);
                         searchHistoryRecycler.setAdapter(sugAdapter);
                         sugAdapter.notifyDataSetChanged();
                         historyText.setVisibility(View.GONE);
                         removeHistoryText.setVisibility(View.GONE);
                     }
                 });
                 builder.show();
                 break;


              default:
                  break;
         }
    }
}
