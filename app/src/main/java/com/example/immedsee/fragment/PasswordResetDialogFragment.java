package com.example.immedsee.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.immedsee.R;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.dao.User;

import org.json.JSONException;
import org.json.JSONObject;

import cn.bmob.v3.AsyncCustomEndpoints;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

/**
 * DoubleWay on 2019/4/19:09:27
 * 邮箱：13558965844@163.com
 */
public class PasswordResetDialogFragment extends DialogFragment implements View.OnClickListener {
      private EditText oldPassword;
      private EditText newPassword;
      private EditText newPassword2;
      private Button passwordReset;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        View view=LayoutInflater.from(getContext()).inflate(R.layout.dialog_password_reset,null);
        oldPassword=(EditText)view.findViewById(R.id.old_password);
        newPassword=(EditText)view.findViewById(R.id.new_password);
        newPassword2=(EditText)view.findViewById(R.id.new_password2);
        passwordReset=(Button)view.findViewById(R.id.password_reset_btn);
        passwordReset.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View view) {
            String oldPass=oldPassword.getText().toString();
            String newPass=newPassword.getText().toString();
            String newPass2=newPassword2.getText().toString();
            if(oldPass.isEmpty()){
                DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_old_please);
                dialogPrompt.show();
                return;
            }
            if (newPass.isEmpty()){
                DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_new_please);
                dialogPrompt.show();
                return;
            }
            if(newPass2.isEmpty()){
                DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_confirm_please);
                dialogPrompt.show();
                return;
            }
            if(!newPass.equals(newPass2)){
                DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_new_different);
                dialogPrompt.show();
                return;
            }
        User user= BmobUser.getCurrentUser(User.class);
       /* AsyncCustomEndpoints as=new AsyncCustomEndpoints();
        JSONObject prams=new JSONObject();
        try {
            prams.put("id",Constant.user.getObjectId());

        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        user.updateCurrentUserPassword(oldPass, newPass, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_reset_successful,3);
                    dialogPrompt.show();
                    Intent intent= new Intent();
                    getTargetFragment().onActivityResult(FragmentThree.REQUEST_CODE_PASSWORD_RESET, Activity.RESULT_OK, intent);
                    return;
                }else if(e.getErrorCode()==210){
                    DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_old_different);
                    dialogPrompt.show();
                }
                  else {
                    Log.d("passwordReset", "done: "+e.getMessage());
                    Log.d("passwordReset", "done: "+e.toString());
                    DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.password_reset_error);
                    dialogPrompt.show();
                }
            }
        });

    }
}
