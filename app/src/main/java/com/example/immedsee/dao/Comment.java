package com.example.immedsee.dao;

import java.util.List;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

/**
 * DoubleWay on 2019/4/12:09:14
 * 邮箱：13558965844@163.com
 */
public class Comment extends BmobObject {
    private String content;
    private List<BmobFile> imageContent;
    private User user;
    private Post post;
    private boolean isSolvde=false; //这个评论是否解决了问题
    private Integer deleteTag=0;//这个评论是否被删除

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<BmobFile> getImageContent() {
        return imageContent;
    }

    public void setImageContent(List<BmobFile> imageContent) {
        this.imageContent = imageContent;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public boolean isSolvde() {
        return isSolvde;
    }

    public void setSolvde(boolean solvde) {
        isSolvde = solvde;
    }

    public int getDeleteTag() {
        return deleteTag;
    }

    public void setDeleteTag(int deleteTag) {
        this.deleteTag = deleteTag;
    }
}
