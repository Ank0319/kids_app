package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.LikedVideoApiService;
import com.example.myapplication.database.WatchHistoryApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.WatchHistory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoPlayerActivity extends AppCompatActivity {
    private static final String TAG = "VideoPlayerActivity";
    private PlayerView playerView;
    private ExoPlayer player;
    private ProgressBar progressBar;
    private ImageView btnLike;
    private ImageView btnComment;
    private TextView tvLikesCount;
    private TextView tvCommentsCount;
    private boolean isLiked = false;
    private long videoId;
    private long userId;
    private LikedVideoApiService likedVideoApiService;
    private WatchHistoryApiService watchHistoryApiService;
    private boolean watchHistoryRecorded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        // 获取视频信息
        String videoUrl = getIntent().getStringExtra("video_url");
        String videoTitle = getIntent().getStringExtra("video_title");
        int likesCount = getIntent().getIntExtra("likes_count", 0);
        int commentsCount = getIntent().getIntExtra("comments_count", 0);
        videoId = getIntent().getLongExtra("video_id", 0);

        if (videoUrl == null || videoTitle == null || videoId == 0) {
            Toast.makeText(this, "视频信息不完整", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 设置标题
        setTitle(videoTitle);

        // 初始化API服务
        likedVideoApiService = ApiClient.getLikedVideoApiService();
        watchHistoryApiService = ApiClient.getWatchHistoryApiService();

        // 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("user_info", MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", "0");
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            userId = 0;
        }

        // 初始化视图
        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progress_bar);
        btnLike = findViewById(R.id.btn_like);
        btnComment = findViewById(R.id.btn_comment);
        tvLikesCount = findViewById(R.id.tv_likes_count);
        tvCommentsCount = findViewById(R.id.tv_comments_count);

        // 设置点赞和评论数量
        tvLikesCount.setText(String.valueOf(likesCount));
        tvCommentsCount.setText(String.valueOf(commentsCount));

        // 检查该视频是否已点赞
        checkLikeStatus();

        // 设置点赞按钮点击事件
        btnLike.setOnClickListener(v -> {
            if (userId == 0) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
                return;
            }
            
            toggleLikeStatus();
        });

        // 设置评论按钮点击事件
        btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(this, CommentActivity.class);
            intent.putExtra("video_id", videoId);
            intent.putExtra("video_title", videoTitle);
            startActivity(intent);
        });

        // 初始化播放器
        initializePlayer(videoUrl);
        
        // 记录观看历史
        recordWatchHistory();
    }

    private void initializePlayer(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // 创建媒体项
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);

        // 添加播放器监听器
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) {
                    progressBar.setVisibility(View.VISIBLE);
                } else if (state == Player.STATE_READY) {
                    progressBar.setVisibility(View.GONE);
                    
                    // 记录观看历史（确保视频已准备好播放）
                    if (!watchHistoryRecorded) {
                        recordWatchHistory();
                    }
                }
            }

            @Override
            public void onPlayerError(com.google.android.exoplayer2.PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, 
                    "播放出错: " + error.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });

        // 准备播放器
        player.prepare();
        player.play();
    }
    
    /**
     * 记录观看历史
     */
    private void recordWatchHistory() {
        if (userId == 0 || videoId == 0) {
            return; // 未登录或没有有效视频ID时不记录
        }
        
        // 创建观看历史对象
        WatchHistory watchHistory = new WatchHistory();
        watchHistory.setUserId(userId);
        watchHistory.setVideoId(videoId);
        watchHistory.setProgress(0); // 初始进度为0
        
        // 调用API记录观看历史
        watchHistoryApiService.recordWatchHistory(watchHistory).enqueue(new Callback<ApiResponse<WatchHistory>>() {
            @Override
            public void onResponse(Call<ApiResponse<WatchHistory>> call, Response<ApiResponse<WatchHistory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<WatchHistory> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        watchHistoryRecorded = true;
                        Log.d(TAG, "观看历史记录成功");
                    } else {
                        Log.e(TAG, "观看历史记录失败: " + apiResponse.getMessage());
                    }
                } else {
                    Log.e(TAG, "观看历史记录失败: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<WatchHistory>> call, Throwable t) {
                Log.e(TAG, "观看历史记录网络错误", t);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }

    /**
     * 检查当前视频的点赞状态
     */
    private void checkLikeStatus() {
        if (userId == 0) return; // 未登录不检查
        
        likedVideoApiService.checkLikeStatus(userId, videoId).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        isLiked = apiResponse.getData();
                        btnLike.setSelected(isLiked);
                        Log.d(TAG, "点赞状态检查: " + (isLiked ? "已点赞" : "未点赞"));
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Log.e(TAG, "检查点赞状态失败", t);
            }
        });
    }

    private void toggleLikeStatus() {
        if (isLiked) {
            // 已点赞，执行取消点赞
            likedVideoApiService.unlikeVideo(userId, videoId).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Object> apiResponse = response.body();
                        if (apiResponse.getCode() == 200) {
                            isLiked = false;
                            btnLike.setSelected(false);
                            
                            // 更新点赞数量
                            int currentLikes = Integer.parseInt(tvLikesCount.getText().toString());
                            tvLikesCount.setText(String.valueOf(currentLikes - 1));
                            
                            Toast.makeText(VideoPlayerActivity.this, "已取消点赞", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VideoPlayerActivity.this, 
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "取消点赞失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "取消点赞失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    Log.e(TAG, "取消点赞失败", t);
                    Toast.makeText(VideoPlayerActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // 未点赞，执行点赞
            likedVideoApiService.likeVideo(userId, videoId).enqueue(new Callback<ApiResponse<Object>>() {
                @Override
                public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Object> apiResponse = response.body();
                        if (apiResponse.getCode() == 200) {
                            isLiked = true;
                            btnLike.setSelected(true);
                            
                            // 更新点赞数量
                            int currentLikes = Integer.parseInt(tvLikesCount.getText().toString());
                            tvLikesCount.setText(String.valueOf(currentLikes + 1));
                            
                            Toast.makeText(VideoPlayerActivity.this, "点赞成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VideoPlayerActivity.this, 
                                apiResponse.getMessage() != null ? apiResponse.getMessage() : "点赞失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "点赞失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                    Log.e(TAG, "点赞失败", t);
                    Toast.makeText(VideoPlayerActivity.this, "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
} 