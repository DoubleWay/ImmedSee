package com.example.immedsee.entity;

/**
 * DoubleWay on 2019/2/25:16:19
 * 邮箱：13558965844@163.com
 */
public class Target {
    private int id;
    private String name;
    private String content;

    public Target(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
