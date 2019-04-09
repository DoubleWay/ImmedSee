package com.example.immedsee.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.immedsee.R;

/**
 * 工具视图，用于展示各种结果视图
 */
public class DialogPrompt extends Dialog {

    private TextView tvMessage, tvClose;

    public DialogPrompt(Activity activity, String prompt) {
        super(activity);
        init(prompt);
        change();
    }

    public DialogPrompt(Activity activity, @StringRes int stringId) {
        super(activity);
        init(activity.getResources().getString(stringId));
        change();
    }

    public DialogPrompt(Activity activity, String prompt, int s) {
        super(activity);
        init(prompt);
        change(s);
    }

    public DialogPrompt(Activity activity, @StringRes int stringId, int s) {
        super(activity);
        init(activity.getResources().getString(stringId));
        change(s);
    }

    @Override
    public void show() {
        // TODO 自动生成的方法存根
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.show();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    DialogPrompt.super.show();
                }
            });
        }
    }

    public void showAndFinish(final Activity activity) {
        show();
        setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                // TODO 自动生成的方法存根
                activity.finish();
            }
        });
    }

    private void change() {
        setCancelable(false);
    }

    private void change(int s) {
        final String text = tvClose.getText().toString();
        s++;
        CountDownTimer timer = new CountDownTimer(s * 1000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tvClose.setText(text + "(" + millisUntilFinished / 1000 + ")");
                if (millisUntilFinished / 1000 == 1) {
                    new Handler().postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            // TODO 自动生成的方法存根
                            DialogPrompt.this.dismiss();
                        }
                    }, 1000);
                }
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    private void init(String prompt) {
        setCanceledOnTouchOutside(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_message);
        setContentView(R.layout.dialog_prompt_us);
        tvClose = (TextView) findViewById(R.id.tv_closeDialog);
        tvMessage = (TextView) findViewById(R.id.tv_message);
        tvMessage.setGravity(Gravity.CENTER_HORIZONTAL);
        tvMessage.setText(prompt);
        tvClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DialogPrompt.this.dismiss();
            }
        });
    }
}
