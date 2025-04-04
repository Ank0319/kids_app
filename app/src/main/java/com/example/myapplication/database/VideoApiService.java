package com.example.myapplication.database;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Video;
import com.example.myapplication.model.VideoPageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface VideoApiService {
    @GET("api/videos/type/learning")
    Call<ApiResponse<VideoPageResponse>> getLearningVideos();

    @GET("api/videos/type/entertainment")
    Call<ApiResponse<VideoPageResponse>> getEntertainmentVideos();

    @GET("api/videos/search")
    Call<ApiResponse<VideoPageResponse>> searchVideos(@Query("title") String title);
    
    /**
     * 获取视频详情
     * @param videoId 视频ID
     * @return 视频详情
     */
    @GET("api/videos/{videoId}")
    Call<ApiResponse<Video>> getVideoById(@Path("videoId") Long videoId);
} 