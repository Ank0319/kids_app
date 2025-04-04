package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.LikedVideoApiService;
import com.example.myapplication.database.VideoApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.LikedVideo;
import com.example.myapplication.model.PageResponse;
import com.example.myapplication.model.Video;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LikesFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private static final String TAG = "LikesFragment";
    private RecyclerView likesRecyclerView;
    private LinearLayout emptyState;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    
    private LikedVideoApiService likedVideoApiService;
    private VideoApiService videoApiService;
    private long userId;
    private List<Video> likedVideos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_likes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化API服务
        likedVideoApiService = ApiClient.getLikedVideoApiService();
        videoApiService = ApiClient.getVideoApiService();
        
        // 获取当前用户ID
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_info", requireActivity().MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", "0");
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            userId = 0;
        }

        // 初始化视图
        likesRecyclerView = view.findViewById(R.id.likes_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);

        // 设置返回按钮
        btnBack.setOnClickListener(v -> {
            // 返回上一个Fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 设置RecyclerView
        likesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 加载喜欢的内容数据
        loadLikesData();
    }

    /**
     * 加载喜欢的内容数据
     * 通过API获取用户点赞的视频列表
     */
    private void loadLikesData() {
        if (userId == 0) {
            // 未登录状态显示空状态
            showEmptyState("请先登录");
            return;
        }
        
        // 显示加载中
        progressBar.setVisibility(View.VISIBLE);
        likesRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        
        // 调用API获取点赞列表
        likedVideoApiService.getLikedVideos(userId, 0, 50).enqueue(new Callback<ApiResponse<PageResponse<LikedVideo>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<LikedVideo>>> call, Response<ApiResponse<PageResponse<LikedVideo>>> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResponse<LikedVideo>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        PageResponse<LikedVideo> pageResponse = apiResponse.getData();
                        List<LikedVideo> likedVideoList = pageResponse.getContent();
                        
                        if (likedVideoList == null || likedVideoList.isEmpty()) {
                            showEmptyState("暂无点赞内容");
                            return;
                        }
                        
                        // 清空列表
                        likedVideos.clear();
                        
                        // 遍历获取每个视频的详情
                        for (LikedVideo likedVideo : likedVideoList) {
                            fetchVideoDetails(likedVideo);
                        }
                    } else {
                        showEmptyState("获取点赞列表失败: " + apiResponse.getMessage());
                    }
                } else {
                    showEmptyState("获取点赞列表失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<LikedVideo>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showEmptyState("网络错误，请检查网络连接");
                Log.e(TAG, "获取点赞列表失败", t);
            }
        });
    }

    /**
     * 获取视频详情
     */
    private void fetchVideoDetails(LikedVideo likedVideo) {
        videoApiService.getVideoById(likedVideo.getVideoId()).enqueue(new Callback<ApiResponse<Video>>() {
            @Override
            public void onResponse(Call<ApiResponse<Video>> call, Response<ApiResponse<Video>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Video> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        Video video = apiResponse.getData();
                        // 添加到列表
                        likedVideos.add(video);
                        
                        // 更新UI
                        updateVideoList();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {
                Log.e(TAG, "获取视频详情失败: " + likedVideo.getVideoId(), t);
            }
        });
    }
    
    /**
     * 更新视频列表
     */
    private void updateVideoList() {
        if (getActivity() == null || !isAdded()) return;
        
        if (likedVideos.isEmpty()) {
            showEmptyState("暂无点赞内容");
            return;
        }
        
        likesRecyclerView.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        
        // 设置适配器
        VideoAdapter videoAdapter = new VideoAdapter(getContext(), likedVideos, this);
        likesRecyclerView.setAdapter(videoAdapter);
    }
    
    /**
     * 显示空状态
     */
    private void showEmptyState(String message) {
        if (getActivity() == null || !isAdded()) return;
        
        likesRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        
        // 更新空状态消息
        TextView emptyMessage = emptyState.findViewById(R.id.empty_message);
        if (emptyMessage != null) {
            emptyMessage.setText(message);
        }
    }
    
    @Override
    public void onVideoClick(Video video) {
        // 打开视频播放页面
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("video_url", video.getUrl());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("likes_count", video.getLikesCount());
        intent.putExtra("comments_count", video.getCommentsCount());
        intent.putExtra("video_id", video.getId());
        startActivity(intent);
    }

    public static LikesFragment newInstance() {
        return new LikesFragment();
    }
} 