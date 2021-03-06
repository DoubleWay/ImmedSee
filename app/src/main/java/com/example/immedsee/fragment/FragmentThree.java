package com.example.immedsee.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.immedsee.R;
import com.example.immedsee.Utils.AppUtils;
import com.example.immedsee.Utils.Constant;
import com.example.immedsee.Utils.DialogPrompt;
import com.example.immedsee.Utils.DialogPromptPermission;
import com.example.immedsee.Utils.JudgeUtils;
import com.example.immedsee.Utils.PermissionUtils;
import com.example.immedsee.Utils.UriToPathUtil;
import com.example.immedsee.activity.MineActivity;
import com.example.immedsee.activity.PostAuthorActivity;
import com.example.immedsee.dao.User;

import java.io.File;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * DoubleWay on 2019/2/15:16:31
 * 邮箱：13558965844@163.com
 */
public class FragmentThree extends Fragment {

    public static final int REQUEST_CODE = 1;
    public static final int REQUEST_CODE_PASSWORD_RESET = 2;
    private final int REQUEST_CODE_UPDATE = 104;
    private final int REQUEST_CODE_PERMISSIONS = 1005;
    public final int IMAGE_CHOOSE=3;
    private RelativeLayout logout;
    private RelativeLayout myPost;
    private RelativeLayout  myMoney;
    private RelativeLayout myPasswordReset;
    private RelativeLayout myPasswordForget;
    private LoginDailogFragment fragment;
    private PasswordResetDialogFragment passwordResetDialogFragment;
    private CircleImageView loginImage;
    private TextView userName;
    private TextView userSignature;
    private TextView loginText;
    private ImageView bingImage;
    private RelativeLayout changeBackground;
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
        myPost=(RelativeLayout)view.findViewById(R.id.user_post);
        myMoney=(RelativeLayout)view.findViewById(R.id.user_money);
        myPasswordReset=(RelativeLayout)view.findViewById(R.id.myPassword_reset);
        myPasswordForget=(RelativeLayout)view.findViewById(R.id.myPassword_forget);
        userName=(TextView)view.findViewById(R.id.username);
        userSignature=(TextView)view.findViewById(R.id.signature);
        loginText=(TextView)view.findViewById(R.id.login_text);
        bingImage=(ImageView)view.findViewById(R.id.bing_image);
        loginImage=(CircleImageView)view.findViewById(R.id.icon_image);
        changeBackground=(RelativeLayout)view.findViewById(R.id.background_change);
        if (PermissionUtils.checkSelfPermission(getActivity(), permission_login, REQUEST_CODE_PERMISSIONS)) {
            initData();
        }
        //切换背景图片
       changeBackground.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent choosePicIntent = new Intent();
              // choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
               choosePicIntent.setType("image/*");
               choosePicIntent.setAction(Intent.ACTION_PICK);
               startActivityForResult(choosePicIntent,IMAGE_CHOOSE);
              // Glide.with(getContext()).load(R.drawable.backgroundtest).into(bingImage);
           }
       });
        //退出登陆
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(BmobUser.getCurrentUser(User.class)!=null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("您确定退出登陆？");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            BmobUser.logOut();   //清除缓存用户对象
                            AppUtils.setAvatarFilePath("");
                            Constant.user = null;
                            userSignature.setText("");
                            userName.setText("");
                            loginText.setVisibility(View.VISIBLE);
                            setDefaultAvatar();
                        }
                    });
                    builder.show();
                }else {
                    Toast.makeText(getContext(),"您还没有登陆，请先登陆后再操作",Toast.LENGTH_SHORT).show();
                }
            }
        });

     //登陆或者进入信息详情
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
 //查看我的帖子
        myPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constant.user!=null) {
                    Intent toAuthorInfoIntent = new Intent(getContext(), PostAuthorActivity.class);
                    toAuthorInfoIntent.putExtra("user", Constant.user);
                    startActivity(toAuthorInfoIntent);
                }else {
                    Toast.makeText(getContext(),"您还没有登陆，请先登陆后再操作",Toast.LENGTH_SHORT).show();
                }
            }
        });
//查询剩余的财富
        myMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constant.user!=null) {
                    BmobQuery<User> query = new BmobQuery<>();
                    query.getObject(Constant.user.getObjectId(), new QueryListener<User>() {
                        @Override
                        public void done(User user, BmobException e) {
                            Log.d("FragmentThree", "done: " + user.getMoney());
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("财富");
                            builder.setMessage("您的即视币剩余为：" + user.getMoney());
                            builder.show();
                        }
                    });
                }else {
                    Toast.makeText(getContext(),"您还没有登陆，请先登陆后再操作",Toast.LENGTH_SHORT).show();
                }
            }
        });
//修改登陆密码
        myPasswordReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Constant.user!=null) {
                    passwordResetDialogFragment = new PasswordResetDialogFragment();
                    passwordResetDialogFragment.setTargetFragment(FragmentThree.this, REQUEST_CODE_PASSWORD_RESET);
                    passwordResetDialogFragment.show(getFragmentManager(), "passwordReset");
                }else {
                    Toast.makeText(getContext(),"您还没有登陆，请先登陆后再操作",Toast.LENGTH_SHORT).show();
                }
            }
        });
  //忘记密码功能
        myPasswordForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
                adb.setTitle("请输入您绑定的邮箱");
                final EditText et = new EditText(getContext());
                adb.setView(et);
                adb.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.cancel();
                            }
                        });
                adb.setPositiveButton(R.string.comfirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(JudgeUtils.isEmail(et.getText().toString())){
                            BmobUser.resetPasswordByEmail(et.getText().toString(), new UpdateListener() {
                                @Override
                                public void done(BmobException e) {
                                     if(e==null){
                                         DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),"重置密码请求成功，请到" + et.getText().toString() + "邮箱进行密码重置操作");
                                         dialogPrompt.show();
                                         //Toast.makeText(getContext(),"重置密码请求成功，请到" + et.getText().toString() + "邮箱进行密码重置操作",Toast.LENGTH_SHORT).show();
                                     }else if(e.getErrorCode()==205) {
                                         DialogPrompt dialogPrompt=new DialogPrompt(getActivity(),"没有找到绑定此邮箱的用户");
                                         dialogPrompt.show();
                                        // Log.d("Bombemail", "done: "+e.toString());
                                     }
                                }
                            });
                        }else {
                            Toast.makeText(getContext(),"您输入的邮箱格式有问题，请重新输入",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                adb.show();
            }
        });


        return view;
    }

    private void initData() {
        //从本地缓存中读取图片，如果有则设置为背景
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String bingPic = prefs.getString("bing_pic", null);
        if(bingPic!=null){
            Glide.with(getContext()).load(bingPic).into(bingImage);
        }
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
        if(requestCode==REQUEST_CODE_PASSWORD_RESET){
            passwordResetDialogFragment.dismiss();
            Log.d("passwordReset", "onActivityResult:hhhh ");
        }
        if(resultCode==RESULT_OK&&requestCode==IMAGE_CHOOSE){
            Uri originalUri = data.getData();
            if(originalUri!=null) {
                /**
                 * 从相册选出图片存到本地缓存中
                 */
                String bingPic = UriToPathUtil.getRealFilePath(getContext(), originalUri);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                Glide.with(getContext()).load(bingPic).into(bingImage);
            }
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
