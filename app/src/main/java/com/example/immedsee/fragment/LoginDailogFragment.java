package com.example.immedsee.fragment;




import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.immedsee.R;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.activity.RegisterActivity;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * DoubleWay on 2019/4/8:11:15
 * 邮箱：13558965844@163.com
 */
public class LoginDailogFragment extends DialogFragment implements View.OnClickListener {

    public static final String USERNAME = "userName";
    public static final String USERPASSWORD = "userPassword";
    private EditText mUsername;
    private EditText mPassword;
    private Button btn;
   // private ImageView iv;
    private TextView toReg;
    private ProgressBar pb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_login, null);
        //iv= view.findViewById(R.id.login_iv);
        toReg= view.findViewById(R.id.login_register);
        mUsername= view.findViewById(R.id.login_et1);
        btn= view.findViewById(R.id.login_btn);
        mPassword= view.findViewById(R.id.login_et2);
       // iv.setOnClickListener(this);
        toReg.setOnClickListener(this);
        btn.setOnClickListener(this);

        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View view) {
         switch (view.getId()){
             case R.id.login_btn:
                 Log.d("login", "onClick: hhh");
                 if (getTargetFragment()== null){
                     return;
                 }
                 String userName = mUsername.getText().toString();
                 String password = mPassword.getText().toString();
                 if (userName.isEmpty()) {
                     DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_user_name);
                     dialogPrompt.show();
                     return;
                 }
                 if (password.isEmpty()) {
                     DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_password);
                     dialogPrompt.show();
                     return;
                 }
                 UiTools.showSimpleLD(getActivity(), R.string.loading_login);
                 BmobUser bombUser = new BmobUser();
                 bombUser.setUsername(userName);
                 bombUser.setPassword(password);
                 bombUser.login(new SaveListener<BmobUser>(){
                     @Override
                     public void done(BmobUser bmobUser, BmobException e) {
                         UiTools.closeSimpleLD();
                          if(e==null){
                              Toast.makeText(getActivity().getApplicationContext(), R.string.login_successful, Toast.LENGTH_LONG).show();
                              Intent intent= new Intent();
                              intent.putExtra(USERNAME, mUsername.getText().toString());
                              intent.putExtra(USERPASSWORD, mPassword.getText().toString());
                              getTargetFragment().onActivityResult(FragmentThree.REQUEST_CODE, Activity.RESULT_OK, intent);
                          }else {
                              if (e.getErrorCode() == 101){
                                  DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.login_error_name_or_password_incorrect);
                                  dialogPrompt.show();
                              }else {
                                  DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), getActivity().getString(R.string.login_error) + "：" + e.getErrorCode() + "，" + e.getMessage());
                                  dialogPrompt.show();
                              }
                          }
                     }
                 });

                 break;
             case R.id.login_register:
                 Intent toRegisterIntent=new Intent(getActivity().getApplicationContext(), RegisterActivity.class);
                 startActivity(toRegisterIntent);

         }
    }
}
