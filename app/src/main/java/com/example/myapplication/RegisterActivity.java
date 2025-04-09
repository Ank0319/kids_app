package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.EmailVerificationApiService;
import com.example.myapplication.database.UserApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.EmailVerification;
import com.example.myapplication.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    
    private EditText etUsername;
    private EditText etPassword;
    private EditText etEmail;
    private EditText etVerificationCode;
    private Button btnRegister;
    private Button btnSendCode;
    private TextView tvLoginPrompt;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    
    private UserApiService userApiService;
    private EmailVerificationApiService emailVerificationApiService;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        // 初始化视图
        initViews();
        
        // 初始化API服务
        userApiService = ApiClient.getUserApiService();
        emailVerificationApiService = ApiClient.getEmailVerificationApiService();
        
        // 设置点击事件
        setupClickListeners();
    }
    
    private void initViews() {
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        etEmail = findViewById(R.id.et_email);
        etVerificationCode = findViewById(R.id.et_verification_code);
        btnRegister = findViewById(R.id.btn_register);
        btnSendCode = findViewById(R.id.btn_send_code);
        tvLoginPrompt = findViewById(R.id.tv_login_prompt);
        progressBar = findViewById(R.id.progress_bar);
        btnBack = findViewById(R.id.btn_back);
    }
    
    private void setupClickListeners() {
        // 发送验证码按钮
        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (validateEmail(email)) {
                sendVerificationCode(email);
            }
        });
        
        // 注册按钮
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                registerUser();
            }
        });
        
        // 跳转到登录页面
        tvLoginPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());
        
        // 长按邮箱输入框，填充测试邮箱
        etEmail.setOnLongClickListener(v -> {
            // 生成一个包含随机数的测试邮箱，避免重复注册
            String randomEmail = "test" + System.currentTimeMillis() % 10000 + "@example.com";
            etEmail.setText(randomEmail);
            Toast.makeText(this, "已填充测试邮箱: " + randomEmail, Toast.LENGTH_SHORT).show();
            return true;
        });
    }
    
    /**
     * 验证输入
     */
    private boolean validateInput() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        
        // 验证用户名
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("请输入用户名");
            return false;
        }
        
        if (username.length() < 4 || username.length() > 50) {
            etUsername.setError("用户名长度应为4-50个字符");
            return false;
        }
        
        // 验证密码
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("请输入密码");
            return false;
        }
        
        if (password.length() < 6 || password.length() > 100) {
            etPassword.setError("密码长度应为6-100个字符");
            return false;
        }
        
        // 验证邮箱
        if (!validateEmail(email)) {
            return false;
        }
        
        // 验证验证码
        if (TextUtils.isEmpty(verificationCode)) {
            etVerificationCode.setError("请输入验证码");
            return false;
        }
        
        if (verificationCode.length() != 6) {
            etVerificationCode.setError("验证码应为6位数字");
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证邮箱格式
     */
    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("请输入邮箱");
            return false;
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("请输入有效的邮箱地址");
            return false;
        }
        
        return true;
    }
    
    /**
     * 发送验证码
     */
    private void sendVerificationCode(String email) {
        // 显示加载状态
        progressBar.setVisibility(View.VISIBLE);
        btnSendCode.setEnabled(false);
        
        // 创建邮箱验证对象
        EmailVerification emailVerification = new EmailVerification(email);
        
        // 调用API发送验证码
        emailVerificationApiService.sendVerificationCode(emailVerification).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(RegisterActivity.this, "验证码已发送，请查收邮件", Toast.LENGTH_SHORT).show();
                        // 启动倒计时
                        startCountDownTimer();
                    } else {
                        btnSendCode.setEnabled(true);
                        // 处理API返回的错误
                        String errorMessage = apiResponse.getMessage();
                        if (errorMessage != null && errorMessage.contains("数据完整性违反")) {
                            // 数据完整性错误，可能是邮箱已被注册
                            Toast.makeText(RegisterActivity.this, "该邮箱已被注册，请更换邮箱", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, 
                                    errorMessage != null ? errorMessage : "发送验证码失败", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    btnSendCode.setEnabled(true);
                    try {
                        // 尝试解析错误响应体
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "错误响应: " + errorBody);
                            
                            if (errorBody.contains("数据完整性违反") || errorBody.contains("唯一约束")) {
                                Toast.makeText(RegisterActivity.this, "该邮箱已被注册，请更换邮箱", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析错误响应失败", e);
                    }
                    
                    Toast.makeText(RegisterActivity.this, "发送验证码失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSendCode.setEnabled(true);
                
                // 记录详细错误信息
                Log.e(TAG, "发送验证码失败: " + t.getMessage(), t);
                
                if (t instanceof java.net.SocketTimeoutException) {
                    Toast.makeText(RegisterActivity.this, "服务器响应超时，请稍后重试", Toast.LENGTH_SHORT).show();
                } else if (t instanceof java.net.ConnectException) {
                    Toast.makeText(RegisterActivity.this, "无法连接到服务器，请检查网络设置", Toast.LENGTH_SHORT).show();
                } else if (t instanceof java.net.UnknownHostException) {
                    Toast.makeText(RegisterActivity.this, "无法解析服务器地址，请检查网络连接", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RegisterActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    /**
     * 启动倒计时
     */
    private void startCountDownTimer() {
        if (isTimerRunning && countDownTimer != null) {
            countDownTimer.cancel();
        }
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                btnSendCode.setText(String.format("%d秒后重新获取", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnSendCode.setEnabled(true);
                btnSendCode.setText("获取验证码");
            }
        }.start();
    }
    
    /**
     * 注册用户
     */
    private void registerUser() {
        // 显示加载状态
        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);
        
        // 获取输入信息
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String verificationCode = etVerificationCode.getText().toString().trim();
        
        // 创建用户对象
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setVerificationCode(verificationCode);
        
        // 添加调试日志
        Log.d(TAG, "注册请求: username=" + username + ", email=" + email + 
              ", verificationCode=" + verificationCode + ", password长度=" + password.length());
        
        // 将用户对象转为JSON并输出用于调试
        try {
            String userJson = new com.google.gson.Gson().toJson(user);
            Log.d(TAG, "注册请求JSON: " + userJson);
        } catch (Exception e) {
            Log.e(TAG, "JSON序列化失败", e);
        }
        
        // 调用API注册用户
        userApiService.register(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                
                // 记录请求URL和请求体
                Log.d(TAG, "请求URL: " + call.request().url());
                Log.d(TAG, "请求方法: " + call.request().method());
                try {
                    if (call.request().body() != null) {
                        Log.d(TAG, "请求ContentType: " + call.request().body().contentType());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "无法获取请求体信息", e);
                }
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    Log.d(TAG, "响应成功: " + apiResponse.getCode() + " - " + apiResponse.getMessage());
                    
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(RegisterActivity.this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
                        // 跳转到登录页面
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // 处理API返回的错误
                        String errorMessage = apiResponse.getMessage();
                        Log.e(TAG, "API错误: " + errorMessage);
                        
                        if (errorMessage != null && errorMessage.contains("数据完整性违反")) {
                            // 数据完整性错误，可能是用户名或邮箱重复
                            Toast.makeText(RegisterActivity.this, "用户名或邮箱已被注册，请更换", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, 
                                    errorMessage != null ? errorMessage : "注册失败", 
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.e(TAG, "响应失败: HTTP " + response.code());
                    
                    try {
                        // 尝试解析错误响应体
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "错误响应体: " + errorBody);
                            
                            // 尝试将错误响应解析为ApiResponse
                            try {
                                ApiResponse<?> errorResponse = new com.google.gson.Gson().fromJson(
                                        errorBody, ApiResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null) {
                                    String errorMessage = errorResponse.getMessage();
                                    Log.e(TAG, "解析出的错误信息: " + errorMessage);
                                    
                                    if (errorMessage.contains("数据完整性违反") || 
                                        errorMessage.contains("唯一约束") || 
                                        errorMessage.contains("已存在")) {
                                        Toast.makeText(RegisterActivity.this, 
                                                "用户名或邮箱已被注册，请更换", Toast.LENGTH_LONG).show();
                                        return;
                                    } else if (errorMessage.contains("验证码") || 
                                               errorMessage.contains("verification code")) {
                                        Toast.makeText(RegisterActivity.this, 
                                                "验证码错误或已过期，请重新获取", Toast.LENGTH_LONG).show();
                                        return;
                                    } else {
                                        Toast.makeText(RegisterActivity.this, 
                                                errorMessage, Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "解析错误响应为ApiResponse失败", e);
                            }
                            
                            // 如果无法解析为ApiResponse，使用简单的字符串检查
                            if (errorBody.contains("数据完整性违反") || errorBody.contains("唯一约束") || 
                                errorBody.contains("已存在") || errorBody.contains("already exists")) {
                                Toast.makeText(RegisterActivity.this, 
                                        "用户名或邮箱已被注册，请更换", Toast.LENGTH_LONG).show();
                                return;
                            } else if (errorBody.contains("验证码") || errorBody.contains("verification code")) {
                                Toast.makeText(RegisterActivity.this, 
                                        "验证码错误或已过期，请重新获取", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "解析错误响应失败", e);
                    }
                    
                    // 默认错误提示
                    Toast.makeText(RegisterActivity.this, "注册失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Log.e(TAG, "请求失败: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
} 