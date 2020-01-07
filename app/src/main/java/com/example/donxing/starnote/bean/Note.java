package com.example.donxing.starnote.bean;

import android.graphics.Path;

import java.io.Serializable;
import java.util.ArrayList;

public class Note implements Serializable {
    private int id;         //笔记ID
    private String content; //笔记内容
    private String groupName;    //笔记所属组的名字
    private String createTime; //笔记创建时间
    private String title;  //   这里的title 指的是  笔记的第一行 一般都是纲要 用于显示纲要
    private String subContent; //这里的subContent指的是 笔记的第二行，用于反应除了用户的开头   相当于内容的缩写
    private static ArrayList<Path> mPaths = new ArrayList<Path>();


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubContent() {
        return subContent;
    }

    public void setSubContent(String subContent) {
        this.subContent = subContent;
    }

    public ArrayList<Path> getmPaths() { return mPaths; }

    public void setmPaths(ArrayList<Path> newmPaths) { this.mPaths = newmPaths;}
}
