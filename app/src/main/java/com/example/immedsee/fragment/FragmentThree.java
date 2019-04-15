package com.example.immedsee.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.immedsee.R;
import com.example.immedsee.Utils.AppUtils;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPromptPermission;
import com.example.immedsee.Utils.PermissionUtils;
import com.example.immedsee.activity.MineActivity;
import com.example.immedsee.dao.User;

import java.io.File;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * DoubleWay on 2019/2/15:16:31
 * 邮箱：13558965844@163.com
 */
public class FragmentThree extends Fragment {

    public static final int REQUEST_CODE = 1;
    private final int REQUEST_CODE_UPDATE = 104;
    private final int REQUEST_CODE_PERMISSIONS = 1005;
    RelativeLayout logout;

    private LoginDailogFragment fragment;
    private CircleImageView loginImage;
    private TextView userName;
    private TextView userSignature;
    private TextView loginText;
    private String[] permission_login = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE
    };
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(getActivity().getApplicationContext(), "054691472ad9df302769cef111cd2442");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_three,container,false);
        logout=(RelativeLayout) view.findViewById(R.id.log_out);
        userName=(TextView)view.findViewById(R.id.username);
        userSignature=(TextView)view.findViewById(R.id.signature);
        loginText=(TextView)view.findViewById(R.id.login_text);
        loginImage=(CircleImageView)view.findViewById(R.id.icon_image);
        if (PermissionUtils.checkSelfPermission(getActivity(), permission_login, REQUEST_CODE_PERMISSIONS)) {
            initData();
        }
        //退出登陆
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BmobUser.logOut();   //清除缓存用户对象
                AppUtils.setAvatarFilePath("");
                Constant.user = null;
                userSignature.setText("");
                userName.setText("");
                loginText.setVisibility(View.VISIBLE);
                setDefaultAvatar();
            }
        });
        loginImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constant.user==null) {
                    fragment = new LoginDailogFragment();
                    fragment.setTargetFragment(FragmentThree.this, REQUEST_CODE);
                    fragment.show(getFragmentManager(), "login");
                }else {
                    Intent toMineIntent=new Intent(getActivity(), MineActivity.class);
                    startActivityForResult(toMineIntent,REQUEST_CODE_UPDATE);
                }
            }
        });
        return view;
    }

    private void initData() {
        File fileBase = new File(Constant.imagePath);
        if (!fileBase.exists()) {
            fileBase.mkdirs();
        }
        setUserInfo();
    }

    private void setUserInfo() {
        Constant.user = User.getCurrentUser(User.class);//取用缓存的数据
        if (Constant.user != null){
            Log.d("fragmentthree", "setUserInfo: "+Constant.user.getUsername());
         userName.setText(Constant.user.getByName());
            String path = AppUtils.getAvatarFilePath();
            File file=new File(path);
            if(file.exists()){
                setAvatar(path);
            }else {
                //头像文件不存在，有可能是图片被删除了，或者没有设置头像
                BmobFile avatarFile = Constant.user.getAvatar();
                if (avatarFile != null) {//如果是有头像的就下载下来,用于缓存头像
                    avatarFile.download(new File(Constant.imagePath + File.separator + avatarFile.getFilename()), new DownloadFileListener() {
                        @Override
                        public void done(String s, BmobException e) {
                            if (e == null) {
                                //下载成功
                                setAvatar(s);
                                AppUtils.setAvatarFilePath(s);
                                if (Looper.myLooper() == Looper.getMainLooper()) {
                                    Log.i("FragmentThree", "主线程");
                                } else {
                                    Log.i("FragmentThree", "子线程");
                                }
                            }
                        }

                        @Override
                        public void onProgress(Integer integer, long l) {

                        }
                    });
                } else {
                    setDefaultAvatar();
                }
            }
            if (Constant.user.getSignature() != null && !Constant.user.getSignature().equals("")) {
                userSignature.setText(Constant.user.getSignature());
            }
           loginText.setVisibility(View.GONE);

        }

    }

    /**
     * 加载默认头像
     */
    private void setDefaultAvatar() {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(R.drawable.header_icon)
                .apply(options)
                .into(loginImage);
    }

    /**
     *
     * 加载头像
     */
    private void setAvatar(String path) {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(path)
                .apply(options)
                .into(loginImage);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE ){
            /*String username= data.getStringExtra(LoginDailogFragment.USERNAME);
            String userPassword= data.getStringExtra(LoginDailogFragment.USERPASSWORD);
            userName.setText(username);*/
            setUserInfo();
            fragment.dismiss();

        }
        if(requestCode == REQUEST_CODE_UPDATE){
            setUserInfo();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean isOK = true;
            for (int grantResult : grantResults) {
                if (PackageManager.PERMISSION_GRANTED != grantResult) {
                    isOK = false;
                    break;
                }
            }
            if (isOK) {
                initData();
            } else {
                // 用户授权拒绝之后，友情提示一下就可以了

                // 这里应该弹出dialog详细说明一下
                // Toast.makeText(this,
                // "您拒绝了所需录音权限的申请，将不能进行操作，请在设置或安全中心开启该项权限后重新操作",
                // Toast.LENGTH_LONG).show();
                DialogPromptPermission dialogPromptPermission = new DialogPromptPermission(getContext());
                dialogPromptPermission.setPromptText("您拒绝了应用所需权限的申请，继续操作将导致部分功能无法正常使用，请在设置或安全中心开启相应的权限后重新操作");
                dialogPromptPermission.show();
            }
        }
    }
}
