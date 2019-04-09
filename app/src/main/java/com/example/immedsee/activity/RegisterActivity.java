package com.example.immedsee.activity;

import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.immedsee.R;
import com.example.immedsee.Utils.Code;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.dao.User;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText resUsername;
    private EditText resPassword;
    private EditText resRePassword;
    private Button btnRegister;
    private EditText resCodes;
    private ImageView resCodesImg;
    private String realCode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar=(Toolbar)findViewById(R.id.register_toolBar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        resUsername=(EditText)findViewById(R.id.register_username);
        resPassword=(EditText)findViewById(R.id.register_password);
        resRePassword=(EditText)findViewById(R.id.register_repassword);
        resCodes=(EditText)findViewById(R.id.register_codes);
        resCodesImg=(ImageView)findViewById(R.id.register_codes_image);
        //将验证码以图片的形式展现出来
        resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
        //获得验证码里的数字
        realCode=Code.getInstance().getCode().toLowerCase();
        resCodesImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
                //重新生成随机码
                realCode=Code.getInstance().getCode().toLowerCase();
            }
        });
        btnRegister=(Button)findViewById(R.id.register_btn);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

    }

    private void register() {
        String name = resUsername.getText().toString();
        String pwd = resPassword.getText().toString();
        String pwdConfirm = resRePassword.getText().toString();
        String code=resCodes.getText().toString();

        if (name.isEmpty()) {
            DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, R.string.user_name_is_not_empty);
            dialogPrompt.show();
            resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
            //再次重新生成随机码
            realCode=Code.getInstance().getCode().toLowerCase();
            return;
        }
        if (pwd.isEmpty()) {
            DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, R.string.please_input_password);
            dialogPrompt.show();
            resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
            //再次重新生成随机码
            realCode=Code.getInstance().getCode().toLowerCase();
            return;
        }
        if (!pwd.equals(pwdConfirm)) {
            DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, R.string.password_new_different);
            dialogPrompt.show();
            resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
            //再次重新生成随机码
            realCode=Code.getInstance().getCode().toLowerCase();
            return;
        }
        if(!code.equals(realCode)){
            DialogPrompt dialogPrompt=new DialogPrompt(RegisterActivity.this,R.string.codes_error);
            dialogPrompt.show();
            resCodesImg.setImageBitmap(Code.getInstance().createBitmap());
            //再次重新生成随机码
            realCode=Code.getInstance().getCode().toLowerCase();
            return;
        }
        UiTools.showSimpleLD(RegisterActivity.this, R.string.loading);
        User user=new User();
        user.setUsername(name);
        user.setByName(name);
        user.setPassword(pwd);
        user.setSex("保密");
        user.setMoney(0);
        user.setSignature("他很懒，什么都没有写");

        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                UiTools.closeSimpleLD();
                if (e == null) {
                    DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, R.string.register_ok, 3);
                    dialogPrompt.showAndFinish(RegisterActivity.this);
                } else {
                    if (e.getErrorCode() == 202) {
                        DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, R.string.register_error_user_already_taken);
                        dialogPrompt.show();
                    } else {
                        DialogPrompt dialogPrompt = new DialogPrompt(RegisterActivity.this, getString(R.string.register_error) + "，错误码：" + e.getErrorCode() + "," + e.getMessage());
                        dialogPrompt.show();
                    }
                }
            }
        });
    }
}
