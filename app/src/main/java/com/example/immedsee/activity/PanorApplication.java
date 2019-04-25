package com.example.immedsee.activity;

import com.baidu.lbsapi.BMapManager;
import com.baidu.lbsapi.MKGeneralListener;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

import java.util.logging.Logger;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobInstallationManager;
import cn.bmob.v3.InstallationListener;
import cn.bmob.v3.exception.BmobException;

/**
 * 获得全景需要集成的applicatio
 */
public class PanorApplication extends Application {
    private static PanorApplication mInstance = null;
    public BMapManager mBMapManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);
        Bmob.initialize(this, "054691472ad9df302769cef111cd2442");
        BmobInstallationManager.getInstance().initialize(new InstallationListener<BmobInstallation>() {
            @Override
            public void done(BmobInstallation bmobInstallation, BmobException e) {
                if (e == null) {
                    //Logger.i(bmobInstallation.getObjectId() + "-" + bmobInstallation.getInstallationId());
                    Log.d("application", "done: "+bmobInstallation.getObjectId() + "-" + bmobInstallation.getInstallationId());
                } else {
                   // Logger.e(e.getMessage());
                    Log.d("application", "done: "+e.getMessage());

                }
            }
        });
// 启动推送服务
        BmobPush.startWork(this);
        mInstance = this;
        initEngineManager(this);
    }

    public void initEngineManager(Context context) {
        if (mBMapManager == null) {
            mBMapManager = new BMapManager(context);
        }

        if (!mBMapManager.init(new MyGeneralListener())) {
            Toast.makeText(
                    PanorApplication.getInstance().getApplicationContext(),
                    "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
        }
        Log.d("ljx", "initEngineManager");
    }

    public static PanorApplication getInstance() {
        return mInstance;
    }

    // 常用事件监听，用来处理通常的网络错误，授权验证错误等
    static class MyGeneralListener implements MKGeneralListener {

        @Override
        public void onGetPermissionState(int iError) {
            // 非零值表示key验证未通过
            if (iError != 0) {
                // 授权Key错误：
                Toast.makeText(
                        PanorApplication.getInstance()
                                .getApplicationContext(),
                        /*"请在AndoridManifest.xml中输入正确的授权Key,并检查您的网络连接是否正常！error: "*/
                        "请确定您打开了网络或者定位"
                                + iError, Toast.LENGTH_LONG).show();
            } /*else {
                Toast.makeText(
                        PanorApplication.getInstance()
                                .getApplicationContext(), "key认证成功",
                        Toast.LENGTH_LONG).show();
            }*/
        }
    }
}
