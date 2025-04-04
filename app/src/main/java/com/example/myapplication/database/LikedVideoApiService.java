package com.example.myapplication.database;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.LikedVideo;
import com.example.myapplication.model.PageResponse;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 视频点赞相关的API服务接口
 */
public interface LikedVideoApiService {
    
    /**
     * 用户点赞视频
     * @param userId 用户ID
     * @param videoId 视频ID
     * @return 点赞结果
     */
    @POST("/api/liked-videos/users/{userId}/videos/{videoId}")
    Call<ApiResponse<Object>> likeVideo(
            @Path("userId") Long userId,
            @Path("videoId") Long videoId
    );
    
    /**
     * 用户取消点赞视频
     * @param userId 用户ID
     * @param videoId 视频ID
     * @return 取消点赞结果
     */
    @DELETE("/api/liked-videos/users/{userId}/videos/{videoId}")
    Call<ApiResponse<Object>> unlikeVideo(
            @Path("userId") Long userId,
            @Path("videoId") Long videoId
    );
    
    /**
     * 检查用户是否已点赞视频
     * @param userId 用户ID
     * @param videoId 视频ID
     * @return 检查结果，true为已点赞，false为未点赞
     */
    @GET("/api/liked-videos/users/{userId}/videos/{videoId}/check")
    Call<ApiResponse<Boolean>> checkLikeStatus(
            @Path("userId") Long userId,
            @Path("videoId") Long videoId
    );
    
    /**
     * 获取用户点赞的所有视频
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页条数
     * @return 点赞视频列表
     */
    @GET("/api/liked-videos/users/{userId}")
    Call<ApiResponse<PageResponse<LikedVideo>>> getLikedVideos(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size
    );
} 