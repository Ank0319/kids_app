package com.example.myapplication.database;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Comment;
import com.example.myapplication.model.PageResponse;

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
 * 评论API服务接口
 */
public interface CommentApiService {
    
    /**
     * 获取视频的所有评论（包含用户信息）
     * @param videoId 视频ID
     * @return 评论列表
     */
    @GET("/api/comments/videos/{videoId}")
    Call<ApiResponse<List<Comment>>> getVideoComments(@Path("videoId") Long videoId);
    
    /**
     * 分页获取视频评论
     * @param videoId 视频ID
     * @param page 页码，从0开始
     * @param size 每页数量
     * @return 分页评论列表
     */
    @GET("/api/comments/videos/{videoId}/page")
    Call<ApiResponse<PageResponse<Comment>>> getVideoCommentsPage(
            @Path("videoId") Long videoId,
            @Query("page") int page,
            @Query("size") int size
    );
    
    /**
     * 创建评论
     * @param comment 评论信息
     * @return 创建结果
     */
    @POST("/api/comments")
    Call<ApiResponse<Comment>> createComment(@Body Comment comment);
    
    /**
     * 获取评论详情
     * @param commentId 评论ID
     * @return 评论详情
     */
    @GET("/api/comments/{id}")
    Call<ApiResponse<Comment>> getCommentDetail(@Path("id") Long commentId);
    
    /**
     * 更新评论
     * @param commentId 评论ID
     * @param comment 更新的评论内容
     * @return 更新结果
     */
    @PUT("/api/comments/{id}")
    Call<ApiResponse<Comment>> updateComment(
            @Path("id") Long commentId,
            @Body Comment comment
    );
    
    /**
     * 删除评论
     * @param commentId 评论ID
     * @return 删除结果
     */
    @DELETE("/api/comments/{id}")
    Call<ApiResponse<Void>> deleteComment(@Path("id") Long commentId);
    
    /**
     * 获取用户的所有评论
     * @param userId 用户ID
     * @return 评论列表
     */
    @GET("/api/comments/users/{userId}")
    Call<ApiResponse<List<Comment>>> getUserComments(@Path("userId") Long userId);
    
    /**
     * 获取视频评论数量
     * @param videoId 视频ID
     * @return 评论数量
     */
    @GET("/api/comments/videos/{videoId}/count")
    Call<ApiResponse<Integer>> getVideoCommentsCount(@Path("videoId") Long videoId);
} 