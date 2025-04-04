package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.VideoAdapter;
import com.example.myapplication.model.Video;
import com.example.myapplication.model.ApiResponse;
import com.example.myapplication.model.VideoPageResponse;
import com.example.myapplication.database.ApiClient;
import com.example.myapplication.database.VideoApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntertainmentFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private ProgressBar progressBar;
    private VideoApiService videoApiService;

    public static EntertainmentFragment newInstance() {
        return new EntertainmentFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoApiService = ApiClient.getVideoApiService();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.video_recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadVideos();
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(videoAdapter);
    }

    private void loadVideos() {
        progressBar.setVisibility(View.VISIBLE);
        videoApiService.getEntertainmentVideos().enqueue(new Callback<ApiResponse<VideoPageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<VideoPageResponse>> call,
                                 @NonNull Response<ApiResponse<VideoPageResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getData().getContent();
                    videoAdapter.updateVideos(videos);
                } else {
                    Toast.makeText(getContext(), "加载视频失败", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<VideoPageResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onVideoClick(Video video) {
        Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
        intent.putExtra("video_url", video.getUrl());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("likes_count", video.getLikesCount());
        intent.putExtra("comments_count", video.getCommentsCount());
        intent.putExtra("video_id", video.getId());
        startActivity(intent);
    }
} 