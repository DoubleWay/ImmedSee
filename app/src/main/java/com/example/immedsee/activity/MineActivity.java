package com.example.immedsee.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.example.immedsee.Utils.UiTools;
import com.example.immedsee.Utils.UniqueCodeUtils;
import com.example.immedsee.Utils.UriToPathUtil;
import com.example.immedsee.dao.User;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Set;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class MineActivity extends AppCompatActivity implements View.OnClickListener {
    private RelativeLayout reUserImage;
    private CircleImageView userImage;
    private TextView userId;
    private RelativeLayout reUserNickName;
    private TextView userNickName;
    private RelativeLayout reUserSex;
    private TextView userSex;
    private RelativeLayout reUserSignature;
    private TextView userSignature;
    private TextView userRegisterDate;
    private BmobFile bfile;
    private final int REQUEST_CODE_SELECT_FILE = 105;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mine_toolBar);
        toolbar.setTitle(" ");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        reUserImage = (RelativeLayout) findViewById(R.id.userImage);
        userImage = (CircleImageView) findViewById(R.id.mine_userImage);
        userId = (TextView) findViewById(R.id.mine_userId);
        reUserNickName = (RelativeLayout) findViewById(R.id.userNickName);
        userNickName = (TextView) findViewById(R.id.mine_userNickName);
        reUserSex = (RelativeLayout) findViewById(R.id.userSex);
        userSex = (TextView) findViewById(R.id.mine_userSex);
        reUserSignature = (RelativeLayout) findViewById(R.id.userSignature);
        userSignature = (TextView) findViewById(R.id.mine_userSignature);
        userRegisterDate = (TextView) findViewById(R.id.mine_userRegisterDate);

        reUserImage.setOnClickListener(this);
        reUserNickName.setOnClickListener(this);
        reUserSex.setOnClickListener(this);
        reUserSignature.setOnClickListener(this);

        initData();
    }

    private void initData() {
        userId.setText(Constant.user.getObjectId());
        userNickName.setText(Constant.user.getByName());
        userRegisterDate.setText(Constant.user.getCreatedAt());
        File file = new File(AppUtils.getAvatarFilePath());
        if (file.exists()) {
            setAvatar(AppUtils.getAvatarFilePath());
        } else {
            BmobFile avatarFile = Constant.user.getAvatar();
            if (avatarFile != null) {//如果是有头像的就下载下来
                avatarFile.download(new File(Constant.imagePath + File.separator + avatarFile.getFilename()), new DownloadFileListener() {
                    @Override
                    public void done(String s, BmobException e) {
                        if (e == null) {
                            //下载成功
                            setAvatar(s);
                            AppUtils.setAvatarFilePath(s);
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
        if (Constant.user.getSex() != null) {
            userSex.setText(Constant.user.getSex());
        }
    }

    private void setDefaultAvatar() {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(R.drawable.header_icon)
                .apply(options)
                .into(userImage);
    }

    private void setAvatar(String path) {
        RequestOptions options = new RequestOptions();
        options.circleCrop();
        Glide.with(this)
                .load(path)
                .apply(options)
                .into(userImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mine_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mine_change:
                UiTools.showSimpleLD(this, R.string.loading);
                if (bfile != null){
                    bfile.uploadblock(new UploadFileListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {//上传文件成功
                                updateUserData();
                            } else {
                                Toast.makeText(MineActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onProgress(Integer value) {
                            super.onProgress(value);
                        }
                    });
                }else {
                    updateUserData();
                }
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void updateUserData() {
        String usernickname = userNickName.getText().toString();
        String usersex =userSex.getText().toString();
        String usersignature = userSignature.getText().toString();
        User user = new User();
        user.setByName(usernickname);
        user.setSex(usersex);
        user.setSignature(usersignature);
        if(bfile!=null){
            user.setAvatar(bfile);
        }
        user.update(Constant.user.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                UiTools.closeSimpleLD();

                if (e == null) {
                    if (bfile != null) {
                        AppUtils.setAvatarFilePath("");
                    }
                    Toast.makeText(MineActivity.this, R.string.update_complete, Toast.LENGTH_SHORT).show();
                    MineActivity.this.setResult(RESULT_OK);
                    MineActivity.this.finish();
                } else {
                    Toast.makeText(MineActivity.this, R.string.update_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.userImage:
                Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
                choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(choosePicIntent, REQUEST_CODE_SELECT_FILE);
                break;
            case R.id.userNickName:
              changeNickName();
                break;
            case R.id.userSignature:
                changeSignature();
                break;
            case R.id.userSex:
                changeSex();
        }

    }

    private void changeSex() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MineActivity.this);
        adb.setTitle("选择");
        final String[] items = {"男生", "女生",  "保密", "未知"};
        adb.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int index) {
                userSex.setText(items[index]);
            }
        });
        adb.show();
    }

    private void changeSignature() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MineActivity.this);
        adb.setTitle("修改签名");
        final EditText et = new EditText(MineActivity.this);
        if (Constant.user.getSignature() != null) {
            et.setText(Constant.user.getSignature());
        }
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
                userSignature.setText(et.getText().toString());
            }
        });
        adb.show();
    }

    private void changeNickName() {
        AlertDialog.Builder adb = new AlertDialog.Builder(MineActivity.this);
        adb.setTitle("修改昵称");
        final EditText et = new EditText(MineActivity.this);
        if (Constant.user.getSignature() != null) {
            et.setText(Constant.user.getByName());
        }
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
                    userNickName.setText(et.getText().toString());
            }
        });
        adb.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_FILE) {
                Uri uri = data.getData();

                if (uri != null) {
                    //给生成的文件加上随机数来区别，以解决图片裁剪后界面上没有变化的bug
                    File fileTemp = new File(Constant.basePath + File.separator + "temp"+UniqueCodeUtils.genSimplePWD()+".png");
                    Log.d("this", "onActivityResult: hhhhhhhhh"+ UniqueCodeUtils.genSimplePWD());
                    if (fileTemp.exists()) {
                        fileTemp.delete();
                    }
                    UCrop.of(uri, Uri.fromFile(fileTemp))
                           .withAspectRatio(1, 1)
                            .withMaxResultSize(512, 512)
                            .start(this);
                    /*bfile = new BmobFile(new File(UriToPathUtil.getImageAbsolutePath(this,uri)));
                     setAvatar(UriToPathUtil.getImageAbsolutePath(this,uri));*/
                    /*Glide.with(this).load(uri).into(userImage);*/
                }
            }
            if (requestCode == UCrop.REQUEST_CROP) {
                Log.d("this", "处理完成");
                final Uri resultUri = UCrop.getOutput(data);
                //这里的resultUri.getPath()获取到的是图片的绝对路径
                Log.d("this", "resultUri.getPath()=" + resultUri.getPath());

                setAvatar(resultUri.getPath());

                bfile = new BmobFile(new File(resultUri.getPath()));
            } else if (resultCode == UCrop.RESULT_ERROR) {
                Throwable cropError = UCrop.getError(data);
                Log.e("this", "剪裁错误：" + cropError.getMessage());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bfile != null) {
            bfile.getLocalFile().delete();
        }
    }
}