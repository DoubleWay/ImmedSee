package com.example.immedsee.Utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限申请工具类
 */

public class PermissionUtils {

    /**
     * @param activity
     * @param permission  要申请的权限
     * @param requestCode 请求码
     * @return 权限判断结果
     */
    public static boolean checkSelfPermission(@NonNull Activity activity, @NonNull String permission, int requestCode) {
        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
        try {
            if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(activity, permission)) {
                return true;
            } else {
                //提示用户开户权限
                ActivityCompat.requestPermissions(activity, new String[]{permission},
                        requestCode);
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(activity, "请在设置或安全中心开启权限", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static boolean checkSelfPermission(@NonNull Activity activity, @NonNull String[] permission, int requestCode) {
        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
        try {
            List<String> permissionList = new ArrayList<String>();
            boolean b = true;
            for (String per : permission) {
                if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(activity, per)) {
                    b = false;
                    permissionList.add(per);
                }
            }
            if (b) {
                return true;
            } else {
                //提示用户开户权限
                ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]),
                        requestCode);
                return false;
            }
        } catch (Exception e) {
            Toast.makeText(activity, "请在设置或安全中心开启该项权限", Toast.LENGTH_LONG).show();
            return false;
        }
    }

}
