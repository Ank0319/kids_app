package com.example.myapplication.database;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * API客户端 - 管理Retrofit实例
 */
public class ApiClient {
    private static final String BASE_URL = "http://192.168.80.221:8080/"; // API文档中的基础URL
    private static Retrofit retrofit = null;
    private static UserApiService userApiService = null;
    private static VideoApiService videoApiService = null;
    private static LikedVideoApiService likedVideoApiService = null;
    private static CommentApiService commentApiService = null;
    private static WatchHistoryApiService watchHistoryApiService = null;
    
    /**
     * 获取Retrofit实例
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // 创建HTTP日志拦截器
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            // 创建OkHttpClient
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);
            
            // 创建Retrofit实例
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 获取用户API服务
     */
    public static UserApiService getUserApiService() {
        if (userApiService == null) {
            userApiService = getClient().create(UserApiService.class);
        }
        return userApiService;
    }

    public static VideoApiService getVideoApiService() {
        if (videoApiService == null) {
            videoApiService = getClient().create(VideoApiService.class);
        }
        return videoApiService;
    }
    
    /**
     * 获取视频点赞API服务
     */
    public static LikedVideoApiService getLikedVideoApiService() {
        if (likedVideoApiService == null) {
            likedVideoApiService = getClient().create(LikedVideoApiService.class);
        }
        return likedVideoApiService;
    }
    
    /**
     * 获取评论API服务
     */
    public static CommentApiService getCommentApiService() {
        if (commentApiService == null) {
            commentApiService = getClient().create(CommentApiService.class);
        }
        return commentApiService;
    }
    
    /**
     * 获取观看历史API服务
     */
    public static WatchHistoryApiService getWatchHistoryApiService() {
        if (watchHistoryApiService == null) {
            watchHistoryApiService = getClient().create(WatchHistoryApiService.class);
        }
        return watchHistoryApiService;
    }
} 