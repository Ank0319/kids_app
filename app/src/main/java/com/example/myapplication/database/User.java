package com.example.myapplication.database;

import com.google.gson.annotations.SerializedName;

/**
 * 用户数据模型类
 */
public class User {
    
    @SerializedName("userId")
    private String userId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("nickname")
    private String nickname;
    
    @SerializedName("signature")
    private String signature;
    
    @SerializedName("avatarUrl")
    private String avatarUrl;
    
    @SerializedName("createTime")
    private String createTime;
    
    @SerializedName("updateTime")
    private String updateTime;
    
    // 构造函数
    public User() {
    }
    
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public User(String username, String password, String nickname, String signature, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.signature = signature;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    
    public String getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
} 