package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.R;
import com.example.myapplication.model.Video;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<Video> videos;
    private OnVideoClickListener listener;
    private static final String API_BASE_URL = "http://192.168.32.221:8080/";
    private static final String VIDEO_BASE_URL = "https://kids-vedio.oss-cn-beijing.aliyuncs.com/";

    public interface OnVideoClickListener {
        void onVideoClick(Video video);
    }

    public VideoAdapter(List<Video> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.titleText.setText(video.getTitle());
        holder.durationText.setText(formatDuration(video.getDuration()));
        holder.likesText.setText(String.valueOf(video.getLikesCount()));
        holder.commentsText.setText(String.valueOf(video.getCommentsCount()));

        // 配置Glide请求选项
        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.placeholder_video)
                .error(R.drawable.error_video);

        // 加载视频缩略图或视频第一帧
        String imageUrl;
        if (video.getThumbnailUrl() != null) {
            // 如果有缩略图，使用API基础URL
            imageUrl = API_BASE_URL + video.getThumbnailUrl();
        } else {
            // 如果没有缩略图，使用视频URL（阿里云OSS地址）
            imageUrl = VIDEO_BASE_URL + video.getUrl();
        }

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .apply(options)
                .into(holder.thumbnailImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                // 设置完整的视频URL
                video.setUrl(VIDEO_BASE_URL + video.getUrl());
                listener.onVideoClick(video);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public void updateVideos(List<Video> newVideos) {
        this.videos = newVideos;
        notifyDataSetChanged();
    }

    private String formatDuration(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView titleText;
        TextView durationText;
        TextView likesText;
        TextView commentsText;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnail_image);
            titleText = itemView.findViewById(R.id.title_text);
            durationText = itemView.findViewById(R.id.duration_text);
            likesText = itemView.findViewById(R.id.likes_text);
            commentsText = itemView.findViewById(R.id.comments_text);
        }
    }
} 