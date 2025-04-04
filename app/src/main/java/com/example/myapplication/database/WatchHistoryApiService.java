package com.example.myapplication.database;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.WatchHistory;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * 观看历史API服务接口
 */
public interface WatchHistoryApiService {
    
    /**
     * 记录新的观看历史
     * @param watchHistory 观看历史对象，包含userId, videoId, progress
     * @return 记录结果
     */
    @POST("/api/watch-history")
    Call<ApiResponse<WatchHistory>> recordWatchHistory(@Body WatchHistory watchHistory);
    
    /**
     * 更新视频观看进度
     * @param userId 用户ID
     * @param videoId 视频ID
     * @param progress 进度（秒）
     * @return 更新结果
     */
    @PUT("/api/watch-history/users/{userId}/videos/{videoId}/progress")
    Call<ApiResponse<WatchHistory>> updateWatchProgress(
            @Path("userId") Long userId,
            @Path("videoId") Long videoId,
            @Body WatchHistory progress);
    
    /**
     * 获取用户的所有观看历史(带视频信息)
     * @param userId 用户ID
     * @return 观看历史列表
     */
    @GET("/api/watch-history/users/{userId}")
    Call<ApiResponse<List<WatchHistory>>> getUserWatchHistory(@Path("userId") Long userId);
    
    /**
     * 分页获取用户观看历史
     * @param userId 用户ID
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @return 分页观看历史
     */
    @GET("/api/watch-history/users/{userId}/page")
    Call<ApiResponse<List<WatchHistory>>> getUserWatchHistoryPaged(
            @Path("userId") Long userId,
            @Query("page") int page,
            @Query("size") int size);
    
    /**
     * 获取用户最近的观看历史（最近7天）
     * @param userId 用户ID
     * @return 最近观看历史列表
     */
    @GET("/api/watch-history/users/{userId}/recent")
    Call<ApiResponse<List<WatchHistory>>> getUserRecentWatchHistory(@Path("userId") Long userId);
    
    /**
     * 删除特定观看记录
     * @param id 观看历史记录ID
     * @return 删除结果
     */
    @DELETE("/api/watch-history/{id}")
    Call<ApiResponse<Void>> deleteWatchHistory(@Path("id") Long id);
    
    /**
     * 删除用户观看特定视频的历史
     * @param userId 用户ID
     * @param videoId 视频ID
     * @return 删除结果
     */
    @DELETE("/api/watch-history/users/{userId}/videos/{videoId}")
    Call<ApiResponse<Void>> deleteUserVideoWatchHistory(
            @Path("userId") Long userId,
            @Path("videoId") Long videoId);
    
    /**
     * 获取用户观看的不同视频数量
     * @param userId 用户ID
     * @return 观看数量
     */
    @GET("/api/watch-history/users/{userId}/count")
    Call<ApiResponse<Integer>> getUserWatchHistoryCount(@Path("userId") Long userId);
} 