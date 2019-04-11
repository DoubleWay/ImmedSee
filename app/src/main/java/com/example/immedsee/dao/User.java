package com.example.immedsee.dao;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * DoubleWay on 2019/4/8:15:48
 * 邮箱：13558965844@163.com
 * 用户表
 */
public class User extends BmobUser {

    private String byName; //别名
    private String sex;//性别
    private double money;//赏金
    private BmobFile avatar;//头像
    private String signature;//个性签名
    public String getByName() {
        return byName;
    }

    public void setByName(String byName) {
        this.byName = byName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
