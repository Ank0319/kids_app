package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.VideoApiService;
import com.example.myapplication.database.WatchHistoryApiService;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.Video;
import com.example.myapplication.model.WatchHistory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private static final String TAG = "HistoryFragment";

    private RecyclerView historyRecyclerView;
    private LinearLayout emptyState;
    private TextView emptyStateText;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private long userId;
    private List<Video> historyVideos = new ArrayList<>();
    private WatchHistoryApiService watchHistoryApiService;
    private VideoApiService videoApiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化API服务
        watchHistoryApiService = ApiClient.getWatchHistoryApiService();
        videoApiService = ApiClient.getVideoApiService();

        // 获取当前用户ID
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userIdStr = sharedPreferences.getString("user_id", "0");
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            userId = 0;
        }

        // 初始化视图
        historyRecyclerView = view.findViewById(R.id.history_recycler_view);
        emptyState = view.findViewById(R.id.empty_state);
        btnBack = view.findViewById(R.id.btn_back);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyStateText = view.findViewById(R.id.empty_state_text);

        // 设置返回按钮
        btnBack.setOnClickListener(v -> {
            // 返回上一个Fragment
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // 设置RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // 加载历史记录数据
        loadHistoryData();
    }

    /**
     * 加载历史记录数据
     */
    private void loadHistoryData() {
        if (userId == 0) {
            showEmptyState("请先登录后查看观看历史");
            return;
        }
        
        showLoading();
        
        // 调用API获取用户的观看历史
        watchHistoryApiService.getUserWatchHistory(userId).enqueue(new Callback<ApiResponse<List<WatchHistory>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<WatchHistory>>> call, Response<ApiResponse<List<WatchHistory>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<WatchHistory>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        List<WatchHistory> watchHistoryList = apiResponse.getData();
                        
                        if (watchHistoryList.isEmpty()) {
                            hideLoading();
                            showEmptyState("暂无观看历史记录");
                            return;
                        }
                        
                        // 清空列表
                        historyVideos.clear();
                        
                        // 遍历获取每个视频的详情
                        for (WatchHistory watchHistory : watchHistoryList) {
                            fetchVideoDetails(watchHistory.getVideoId());
                        }
                    } else {
                        hideLoading();
                        showEmptyState("获取观看历史失败: " + apiResponse.getMessage());
                    }
                } else {
                    hideLoading();
                    showEmptyState("获取观看历史失败");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<WatchHistory>>> call, Throwable t) {
                hideLoading();
                showEmptyState("网络错误，请检查网络连接");
                Log.e(TAG, "加载观看历史失败", t);
            }
        });
    }
    
    /**
     * 获取视频详情
     */
    private void fetchVideoDetails(Long videoId) {
        videoApiService.getVideoById(videoId).enqueue(new Callback<ApiResponse<Video>>() {
            @Override
            public void onResponse(Call<ApiResponse<Video>> call, Response<ApiResponse<Video>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Video> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getData() != null) {
                        Video video = apiResponse.getData();
                        // 添加到列表
                        historyVideos.add(video);
                        
                        // 更新UI
                        updateVideoList();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Video>> call, Throwable t) {
                Log.e(TAG, "获取视频详情失败: " + videoId, t);
            }
        });
    }
    
    /**
     * 更新视频列表
     */
    private void updateVideoList() {
        if (getActivity() == null || !isAdded()) return;
        
        if (!historyVideos.isEmpty()) {
            hideLoading();
            historyRecyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            
            // 设置适配器
            VideoAdapter videoAdapter = new VideoAdapter(getContext(), historyVideos, this);
            historyRecyclerView.setAdapter(videoAdapter);
        }
    }
    
    /**
     * 显示加载中
     */
    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        historyRecyclerView.setVisibility(View.GONE);
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
        historyRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        if (emptyStateText != null) {
            emptyStateText.setText(message);
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

    public static HistoryFragment newInstance() {
        return new HistoryFragment();
    }
} 