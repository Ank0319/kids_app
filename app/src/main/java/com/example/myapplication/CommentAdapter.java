package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.model.Comment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 评论适配器
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Context context;
    private List<Comment> commentList;
    private static final String API_BASE_URL = "http://192.168.19.221:8080/";
    private static final SimpleDateFormat INPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat OUTPUT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        
        // 设置用户昵称
        holder.nickname.setText(comment.getUserNickname());
        
        // 设置评论内容
        holder.content.setText(comment.getContent());
        
        // 设置评论时间
        String formattedTime = formatTime(comment.getCreatedAt());
        holder.time.setText(formattedTime);
        
        // 加载用户头像
        String avatarUrl = comment.getUserAvatarUrl();
        if (!TextUtils.isEmpty(avatarUrl)) {
            // 判断是否是完整URL
            if (!avatarUrl.startsWith("http://") && !avatarUrl.startsWith("https://")) {
                avatarUrl = API_BASE_URL + avatarUrl;
            }
            
            Glide.with(context)
                .load(avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(holder.avatar);
        } else {
            // 使用默认头像
            holder.avatar.setImageResource(R.drawable.avatar);
        }
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }
    
    /**
     * 更新评论列表
     */
    public void updateComments(List<Comment> newComments) {
        this.commentList = newComments;
        notifyDataSetChanged();
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(String timeStr) {
        if (TextUtils.isEmpty(timeStr)) {
            return "";
        }
        
        try {
            Date date = INPUT_FORMAT.parse(timeStr);
            return date != null ? OUTPUT_FORMAT.format(date) : timeStr;
        } catch (ParseException e) {
            return timeStr;
        }
    }

    /**
     * 评论ViewHolder
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nickname;
        TextView time;
        TextView content;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.comment_avatar);
            nickname = itemView.findViewById(R.id.comment_nickname);
            time = itemView.findViewById(R.id.comment_time);
            content = itemView.findViewById(R.id.comment_content);
        }
    }
} 