package com.example.immedsee.Utils;

import android.os.Environment;

import com.example.immedsee.dao.User;

import java.io.File;

/**
 *获得文件路径的工具类
 */

public class Constant {
    //获得外部存储器的第一层的文件对象
    public static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "Test";
    //File.separator,与系统有关的默认名称分隔符
    public static String imagePath = basePath + File.separator + "images";
    public static User user;
}
