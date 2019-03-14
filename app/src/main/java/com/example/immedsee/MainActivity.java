package com.example.immedsee;



import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.location.LocationClient;
import com.example.immedsee.fragment.FragmentOne;
import com.example.immedsee.fragment.FragmentThree;
import com.example.immedsee.fragment.FragmentTwo;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FragmentOne fragmentOne = new FragmentOne();
    private FragmentTwo fragmentTwo =new FragmentTwo();
    private FragmentThree fragmentThree =new FragmentThree();
    public long exitTime=System.currentTimeMillis();
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentOne.isFirstLocate=true;
                    showNav(R.id.navigation_home);
                    return true;
                case R.id.navigation_dashboard:
                    showNav(R.id.navigation_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    showNav(R.id.navigation_notifications);
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
        beginTransaction.add(R.id.content,fragmentOne,"fragmentOne").add(R.id.content,fragmentTwo,"fragmentTwo").add(R.id.content,fragmentThree,"fagmentThree");//开启一个事务将fragment动态加载到组件
        beginTransaction.hide(fragmentOne).hide(fragmentTwo).hide(fragmentThree);//隐藏fragment
        beginTransaction.addToBackStack(null);//返回到上一个显示的fragment
        beginTransaction.commit();//每一个事务最后操作必须是commit（），否则看不见效果
        showNav(R.id.navigation_home);
    }
    private void showNav(int navid){
        FragmentTransaction beginTransaction=getSupportFragmentManager().beginTransaction();
        switch (navid){
            case R.id.navigation_home:
                beginTransaction.hide(fragmentTwo).hide(fragmentThree);
                beginTransaction.show(fragmentOne);
                beginTransaction.addToBackStack(null);
                beginTransaction.commit();
                break;
            case R.id.navigation_dashboard:
                beginTransaction.hide(fragmentOne).hide(fragmentThree);
                beginTransaction.show(fragmentTwo);
                beginTransaction.addToBackStack(null);
                beginTransaction.commit();
                break;
            case R.id.navigation_notifications:
                beginTransaction.hide(fragmentTwo).hide(fragmentOne);
                beginTransaction.show(fragmentThree);
                beginTransaction.addToBackStack(null);
                beginTransaction.commit();
                break;
        }
    }

    /**
     *视线双击两次退出app
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN){
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return false;
    }
}





