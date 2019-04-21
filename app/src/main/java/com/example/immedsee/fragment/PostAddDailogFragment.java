package com.example.immedsee.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.immedsee.R;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.dao.Post;
import com.example.immedsee.dao.User;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * DoubleWay on 2019/4/10:14:29
 * 邮箱：13558965844@163.com
 */
public class PostAddDailogFragment extends DialogFragment implements View.OnClickListener {
    private EditText postTitle;
    private EditText postContent;
    private EditText postMoney;
    private Button btnOk;
    private Button btnCancle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //设置点击dialog外部不取消
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().setCancelable(false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setCancelable(true);
        View view= LayoutInflater.from(getActivity()).inflate(R.layout.dialog_post_add, null);
        postTitle=view.findViewById(R.id.post_ed_title);
        postContent=view.findViewById(R.id.post_ed_cotents);
        postMoney=view.findViewById(R.id.post_ed_money);
        btnOk=view.findViewById(R.id.ok);
        btnCancle=view.findViewById(R.id.cancle);
        btnOk.setOnClickListener(this);
        btnCancle.setOnClickListener(this);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ok:
                if (getTargetFragment()== null){
                    return;
                }
                String title=postTitle.getText().toString();
                String content=postContent.getText().toString();
                String moneyString=postMoney.getText().toString();
                if(moneyString.isEmpty()){
                    DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_your_money);
                    dialogPrompt.show();
                    return;
                }
                double money=Double.parseDouble(moneyString);//将输入的String转化为double
                Log.d("poasadd", "onClick: "+money);
                User user= BmobUser.getCurrentUser(User.class);
                Log.d("poasadd", "onClick: "+user.getUsername());
                Log.d("poasadd", "onClick: "+user.getMoney());
                if (title.isEmpty()) {
                    DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_your_title);
                    dialogPrompt.show();
                    return;
                }
                if (content.isEmpty()) {
                    DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_your_content);
                    dialogPrompt.show();
                    return;
                }
                if(money==0){
                    DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_input_your_money);
                    dialogPrompt.show();
                    return;
                }
                if(money>user.getMoney()){
                    DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_check_your_money);
                    dialogPrompt.show();
                    return;
                }
                if (user==null) {
                DialogPrompt dialogPrompt = new DialogPrompt(getActivity(), R.string.please_login);
                dialogPrompt.show();
                return;
            }
                UiTools.showSimpleLD(getActivity(), R.string.postSend_loading);
                user.setMoney(user.getMoney()-money);
                user.update(user.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        Log.d("poasadd", "done: 修改money成功");
                    }
                });
                Post post=new Post();
                post.setPostTitle(title);
                post.setPostContent(content);
                post.setPostMoney(money);
                post.setEnd(false);
                post.setDeleteTag(0);
                post.setAuthor(user);
                post.save(new SaveListener<String>() {
                    @Override
                    public void done(String s, BmobException e) {
                        UiTools.closeSimpleLD();
                        if(e==null){
                            DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.postUP_success);
                            dialogPrompt.show();
                            Intent intent= new Intent();
                            getTargetFragment().onActivityResult(FragmentTwo.REQUEST_CODE, Activity.RESULT_OK, intent);
                        }else {
                            Log.d("PostAddDailogFragment", "done: "+e.toString());
                            DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),R.string.postUP_error);
                            dialogPrompt.show();
                        }
                    }
                });
                break;
            case R.id.cancle:
               getDialog().dismiss();
               break;
        }

    }
}
