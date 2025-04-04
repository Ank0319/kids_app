package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

/**
 * 用户点赞视频的数据模型
 */
public class LikedVideo {
    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("videoId")
    private Long videoId;

    @SerializedName("likedAt")
    private String likedAt;
    
    // 可能的扩展字段 - 如果API响应中包含视频详情
    @SerializedName("video")
    private Video video;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public String getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(String likedAt) {
        this.likedAt = likedAt;
    }
    
    public Video getVideo() {
        return video;
    }
    
    public void setVideo(Video video) {
        this.video = video;
    }
} 