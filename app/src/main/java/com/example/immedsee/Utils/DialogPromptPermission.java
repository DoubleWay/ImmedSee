package com.example.immedsee.Utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.immedsee.R;


/**
 * 权限提示弹出框
 */

public class DialogPromptPermission extends Dialog {

    private static final String TAG = "DialogPromptPermission";
    private RelativeLayout rlSetting, rlCancelNext;
    private TextView tvPromptText;
    private String promptText;
    private Context context;

    public DialogPromptPermission(@NonNull Context context) {
        super(context);
        this.context = context;
        init();
    }

    public DialogPromptPermission(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
        init();
    }

    protected DialogPromptPermission(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
        init();
    }

    private void init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.drawable.shape_dialog_radius);
        setContentView(R.layout.dialog_prompt);
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rlSetting = (RelativeLayout) findViewById(R.id.rl_setting);
        rlCancelNext = (RelativeLayout) findViewById(R.id.rl_cancelNext);
        tvPromptText = (TextView) findViewById(R.id.tv_promptText);
        tvPromptText.setText(promptText);
        rlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Uri packageURI = Uri.parse("package:" + context.getApplicationInfo().packageName);
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                context.startActivity(intent);
            }
        });
        rlCancelNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
