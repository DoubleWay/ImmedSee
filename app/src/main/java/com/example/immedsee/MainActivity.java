package com.example.immedsee;



import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;


import com.baidu.location.LocationClient;
import com.example.immedsee.fragment.FragmentOne;
import com.example.immedsee.fragment.FragmentThree;
import com.example.immedsee.fragment.FragmentTwo;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FragmentOne fragmentOne = new FragmentOne();
    private FragmentTwo fragmentTwo =new FragmentTwo();
    private FragmentThree fragmentThree =new FragmentThree();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    //showNav(R.id.navigation_home);
                    if(!fragmentOne.isAdded()){
                    fragmentOne.isFirstLocate=true;
                    beginTransaction.addToBackStack(null);
                    beginTransaction.replace(R.id.content,fragmentOne);
                    beginTransaction.commit();
                    }
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    //showNav(R.id.navigation_dashboard);
                    if(!fragmentTwo.isAdded()) {
                        beginTransaction.replace(R.id.content, fragmentTwo);
                        beginTransaction.addToBackStack(null);
                        beginTransaction.commit();
                    }
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    //showNav(R.id.navigation_notifications);
                    if(!fragmentThree.isAdded()) {
                        beginTransaction.replace(R.id.content, fragmentThree);
                        beginTransaction.addToBackStack(null);
                        beginTransaction.commit();
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         //init();
        setDefaultFragment();
        mTextMessage = (TextView) findViewById(R.id.message);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
   //用来初始化组件
    private void setDefaultFragment(){
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        beginTransaction.replace(R.id.content,fragmentOne);
        beginTransaction.commit();
    }

}




