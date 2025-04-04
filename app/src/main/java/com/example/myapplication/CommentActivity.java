package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.CommentApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Comment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 评论列表活动
 */
public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "CommentActivity";
    
    private RecyclerView commentListView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList = new ArrayList<>();
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView emptyMessage;
    private TextView commentCountView;
    private EditText commentInput;
    private Button submitButton;
    
    private CommentApiService commentApiService;
    private long videoId;
    private long userId;
    private String videoTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        
        // 获取视频ID和标题
        videoId = getIntent().getLongExtra("video_id", 0);
        videoTitle = getIntent().getStringExtra("video_title");
        
        if (videoId == 0) {
            Toast.makeText(this, "视频信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", "0");
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            userId = 0;
        }
        
        // 初始化API服务
        commentApiService = ApiClient.getCommentApiService();
        
        // 初始化视图
        initViews();
        
        // 设置标题
        TextView titleTextView = findViewById(R.id.title_text);
        titleTextView.setText(videoTitle != null ? "评论列表 - " + videoTitle : "评论列表");
        
        // 加载评论列表
        loadComments();
    }
    
    /**
     * 初始化视图
     */
    private void initViews() {
        progressBar = findViewById(R.id.progress_bar);
        emptyState = findViewById(R.id.empty_state);
        emptyMessage = findViewById(R.id.empty_message);
        commentCountView = findViewById(R.id.comment_count);
        commentInput = findViewById(R.id.comment_input);
        submitButton = findViewById(R.id.btn_submit);
        
        // 设置返回按钮
        ImageButton backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());
        
        // 设置评论列表
        commentListView = findViewById(R.id.comment_list);
        commentListView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList);
        commentListView.setAdapter(commentAdapter);
        
        // 设置发送按钮点击事件
        submitButton.setOnClickListener(v -> submitComment());
    }
    
    /**
     * 加载评论列表
     */
    private void loadComments() {
        showLoading();
        
        commentApiService.getVideoComments(videoId).enqueue(new Callback<ApiResponse<List<Comment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Comment>>> call, Response<ApiResponse<List<Comment>>> response) {
                hideLoading();
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Comment>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        updateCommentsList(apiResponse.getData());
                    } else {
                        showEmptyState("获取评论失败: " + apiResponse.getMessage());
                    }
                } else {
                    showEmptyState("获取评论失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Comment>>> call, Throwable t) {
                hideLoading();
                showEmptyState("网络错误，请检查网络连接");
                Log.e(TAG, "加载评论失败", t);
            }
        });
        
        // 获取评论数量
        commentApiService.getVideoCommentsCount(videoId).enqueue(new Callback<ApiResponse<Integer>>() {
            @Override
            public void onResponse(Call<ApiResponse<Integer>> call, Response<ApiResponse<Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Integer> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        updateCommentCount(apiResponse.getData());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Integer>> call, Throwable t) {
                Log.e(TAG, "获取评论数量失败", t);
            }
        });
    }
    
    /**
     * 更新评论列表
     */
    private void updateCommentsList(List<Comment> comments) {
        if (comments.isEmpty()) {
            showEmptyState("暂无评论，快来发表第一条评论吧");
            return;
        }
        
        commentList.clear();
        commentList.addAll(comments);
        commentAdapter.notifyDataSetChanged();
        
        commentListView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
    }
    
    /**
     * 更新评论数量
     */
    private void updateCommentCount(int count) {
        commentCountView.setText("共" + count + "条评论");
    }
    
    /**
     * 提交评论
     */
    private void submitComment() {
        if (userId == 0) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String content = commentInput.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "评论内容不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // 创建评论对象
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setVideoId(videoId);
        comment.setContent(content);
        
        // 禁用发送按钮
        submitButton.setEnabled(false);
        
        // 发送评论
        commentApiService.createComment(comment).enqueue(new Callback<ApiResponse<Comment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Comment>> call, Response<ApiResponse<Comment>> response) {
                // 启用发送按钮
                submitButton.setEnabled(true);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Comment> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        // 清空输入框
                        commentInput.setText("");
                        
                        // 刷新评论列表
                        loadComments();
                        
                        Toast.makeText(CommentActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(CommentActivity.this, "评论失败: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CommentActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Comment>> call, Throwable t) {
                // 启用发送按钮
                submitButton.setEnabled(true);
                
                Toast.makeText(CommentActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "发送评论失败", t);
            }
        });
    }
    
    /**
     * 显示加载中
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        commentListView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
    }
    
    /**
     * 隐藏加载中
     */
    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }
    
    /**
     * 显示空状态
     */
    private void showEmptyState(String message) {
        commentListView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        emptyMessage.setText(message);
    }
} 