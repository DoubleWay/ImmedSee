package com.example.immedsee;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cn.bmob.push.PushConstants;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * DoubleWay on 2019/4/23:14:47
 * 邮箱：13558965844@163.com
 */
public class MyPushMessageReceiver extends BroadcastReceiver {
    private  Context mContext;
    private Map<String, String> myMap;
    private String alert;
    @Override
    public void onReceive(Context context, Intent intent) {
      if(mContext==null) {
          mContext = context;
      }
        if(intent.getAction().equals(PushConstants.ACTION_MESSAGE)){
            Log.d("bmobtest", "客户端收到推送内容："+intent.getStringExtra("msg"));
            Toast.makeText(context,"即视有一条新的通知",Toast.LENGTH_SHORT).show();
            //Toast.makeText(context,"客户端收到推送内容："+intent.getStringExtra(PushConstants.EXTRA_PUSH_MESSAGE_STRING),Toast.LENGTH_SHORT).show();

            try {
                JSONObject object=new JSONObject(intent.getStringExtra("msg"));
                Log.d("bmobtest", "hhhhh44"+object.toString());
                alert=object.getString("alert");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendNotificationMsg();
        }
    }

    private void sendNotificationMsg() {
        Log.d("bmobtest", "hhhhh22");
        NotificationManager manager=(NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=new NotificationCompat.Builder(mContext,"notification")
                .setContentTitle("即视有一条通知给你")
                .setContentText(alert)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),R.drawable.ic_launcher_foreground))
                .setAutoCancel(true)
                .build();
              manager.notify(1, notification);
    }


}
