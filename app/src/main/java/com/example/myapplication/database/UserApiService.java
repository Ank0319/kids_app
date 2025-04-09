package com.example.myapplication.database;

import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.LoginRequest;
import com.example.myapplication.model.User;
import com.example.myapplication.model.PasswordChangeRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * API服务接口 - 用于与Spring Boot后端通信
 */
public interface UserApiService {
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GET("api/users/{id}")
    Call<ApiResponse<User>> getUserById(@Path("id") String userId);
    
    /**
     * 根据用户名获取用户
     * @param username 用户名
     * @return 用户信息
     */
    @GET("api/users/username/{username}")
    Call<ApiResponse<User>> getUserByUsername(@Path("username") String username);
    
    /**
     * 创建用户（注册）
     * @param user 用户信息
     * @return 创建的用户信息
     */
    @POST("api/users")
    Call<ApiResponse<User>> createUser(@Body User user);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param user 更新的用户信息
     * @return 更新后的用户信息
     */
    @PUT("api/users/{id}")
    Call<ApiResponse<User>> updateUser(@Path("id") String userId, @Body User user);
    
    /**
     * 获取所有用户
     * @return 用户列表
     */
    @GET("api/users")
    Call<ApiResponse<List<User>>> getAllUsers();
    
    /**
     * 用户注册
     * @param user 用户注册信息
     * @return 注册成功的用户信息
     */
    @POST("api/users/register")
    Call<ApiResponse<User>> register(@Body User user);
    
    /**
     * 用户登录
     * @param loginRequest 登录请求信息（包含account和password）
     * @return 登录成功的用户信息
     */
    @POST("api/auth/login")
    Call<ApiResponse<User>> login(@Body LoginRequest loginRequest);
    
    /**
     * 修改密码
     * @param request 密码修改请求信息
     * @return API响应
     */
    @POST("api/auth/change-password")
    Call<ApiResponse<Void>> changePassword(@Body PasswordChangeRequest request);
} 