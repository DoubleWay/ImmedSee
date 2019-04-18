package com.example.immedsee.dao;

import java.io.Serializable;

import cn.bmob.v3.BmobObject;

/**
 * DoubleWay on 2019/4/10:10:51
 * 邮箱：13558965844@163.com
 *
 */
public class Post extends BmobObject implements Serializable {
    //通过implements Serializable接口 将Post序列化，可以通过Intent来传递对象

   /* private String authorId;//帖子作者的id，就是用户的唯一的id
    private String authorName;//帖子作者的名字，就是用户表里的ByName*/
    private  User author;
    private String postContent;//帖子的内容
    private String postTitle;//帖子的标题
    private boolean isEnd;//是否结贴；
    private double postMoney;//悬赏贴的价钱
    private Integer deleteTag=0;//是否删除帖子

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public double getPostMoney() {
        return postMoney;
    }

    public void setPostMoney(double postMoney) {
        this.postMoney = postMoney;
    }

    public int getDeleteTag() {
        return deleteTag;
    }

    public void setDeleteTag(int deleteTag) {
        this.deleteTag = deleteTag;
    }
}
