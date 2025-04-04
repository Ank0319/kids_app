package com.example.myapplication.database;

import com.google.gson.annotations.SerializedName;

/**
 * 统一API响应格式
 * @param <T> 响应数据类型
 */
public class ApiResponse<T> {
    
    @SerializedName("code")
    private int code;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("data")
    private T data;
    
    @SerializedName("timestamp")
    private long timestamp;
    
    // 构造函数
    public ApiResponse() {
    }
    
    public ApiResponse(int code, String message, T data, long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }
    
    // Getters and Setters
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * 检查响应是否成功
     * @return 如果响应码为200则返回true
     */
    public boolean isSuccess() {
        return code == 200;
    }
} 