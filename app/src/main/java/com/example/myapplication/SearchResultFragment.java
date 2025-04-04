package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.VideoAdapter;
import com.example.myapplication.model.Video;

import java.util.ArrayList;
import java.util.List;

public class SearchResultFragment extends Fragment implements VideoAdapter.OnVideoClickListener {
    private static final String ARG_VIDEOS = "videos";
    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private TextView noResultsText;

    public static SearchResultFragment newInstance(List<Video> videos) {
        SearchResultFragment fragment = new SearchResultFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_VIDEOS, new ArrayList<>(videos));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recyclerView = view.findViewById(R.id.video_recycler_view);
        noResultsText = view.findViewById(R.id.no_results_text);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadSearchResults();
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(videoAdapter);
    }

    private void loadSearchResults() {
        if (getArguments() != null) {
            ArrayList<Video> videos = getArguments().getParcelableArrayList(ARG_VIDEOS);
            if (videos != null && !videos.isEmpty()) {
                videoAdapter.updateVideos(videos);
                noResultsText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            } else {
                noResultsText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        }
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