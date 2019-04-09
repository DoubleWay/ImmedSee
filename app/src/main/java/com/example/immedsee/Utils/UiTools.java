package com.example.immedsee.Utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.example.immedsee.R;


/**
 *工具类 用于异步展示dialog
 */

public class UiTools {
    private static ProgressDialog mypDialog = null;

    public static void showSimpleLD(Context ctx, String msg) {
        if (TextUtils.isEmpty(msg)) {
            msg = ctx.getResources().getString(R.string.loading);
        }
        init(ctx, msg);
    }

    public static void showSimpleLD(Context ctx, @StringRes int stringId) {
        init(ctx, ctx.getResources().getString(stringId));
    }

    private static void init(Context ctx, String msg) {
        if (null == mypDialog) {
            mypDialog = new ProgressDialog(ctx);
        }
        mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mypDialog.setIndeterminate(false);
        mypDialog.setCancelable(true);
        mypDialog.setCanceledOnTouchOutside(false);
        mypDialog.setOnCancelListener(new Dialog.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                // TODO 自动生成的方法存根
                mypDialog = null;
            }
        });
        mypDialog.setMessage(msg);
        mypDialog.show();
    }

    public static void closeSimpleLD() {
        if (null != mypDialog && mypDialog.isShowing()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mypDialog.cancel();
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        mypDialog.cancel();
                    }
                });
            }

        }
    }
}
