package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.UserApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;
    private ProgressDialog progressDialog;
    
    private UserApiService userApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // 初始化API服务
        userApiService = ApiClient.getUserApiService();
        
        // 初始化视图
        initViews();
        
        // 设置点击事件
        setupClickListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        
        // 创建进度对话框
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("登录中...");
        progressDialog.setCancelable(false);
    }
    
    private void setupClickListeners() {
        // 登录按钮点击事件
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        
        // 注册按钮点击事件
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到注册页面
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        
        // 忘记密码点击事件
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 跳转到忘记密码页面
                Toast.makeText(LoginActivity.this, "忘记密码功能暂未实现", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void attemptLogin() {
        // 重置错误提示
        etUsername.setError(null);
        etPassword.setError(null);
        
        // 获取输入值
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // 验证输入
        boolean cancel = false;
        View focusView = null;
        
        // 检查密码
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            focusView = etPassword;
            cancel = true;
        }
        
        // 检查用户名
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            focusView = etUsername;
            cancel = true;
        }
        
        if (cancel) {
            // 存在错误，设置焦点到错误字段
            focusView.requestFocus();
        } else {
            // 显示进度对话框
            progressDialog.show();
            
            // 创建用户对象用于登录
            User loginUser = new User();
            loginUser.setUsername(username);
            loginUser.setPassword(password);
            
            // 执行登录请求
            userApiService.login(loginUser).enqueue(new Callback<ApiResponse<User>>() {
                @Override
                public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                    progressDialog.dismiss();
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<User> apiResponse = response.body();
                        if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                            // 登录成功
                            User user = apiResponse.getData();
                            
                            // 保存用户信息
                            saveUserInfo(user);
                            
                            // 提示信息
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            
                            // 返回主界面
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // 登录失败
                            Toast.makeText(LoginActivity.this, 
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "登录失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // API调用失败
                        Toast.makeText(LoginActivity.this, "登录失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                    progressDialog.dismiss();
                    Log.e(TAG, "登录失败", t);
                    Toast.makeText(LoginActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    
    private void saveUserInfo(User user) {
        // 保存用户信息到SharedPreferences
        SharedPreferences sp = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        
        editor.putString("user_id", user.getId() != null ? user.getId().toString() : "0");
        editor.putString("username", user.getUsername());
        editor.putString("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        editor.putString("avatarUrl", user.getAvatarUrl());
        editor.putString("signature", user.getSignature() != null ? user.getSignature() : "这个人很懒，什么都没留下");
        editor.putBoolean("is_login", true);
        
        // 确保所有数据都成功提交
        editor.apply();
        
        Log.d(TAG, "保存用户信息: ID=" + user.getId() 
               + ", 用户名=" + user.getUsername()
               + ", 昵称=" + user.getNickname()
               + ", 个性签名=" + user.getSignature()
               + ", 头像URL=" + user.getAvatarUrl());
    }
} 