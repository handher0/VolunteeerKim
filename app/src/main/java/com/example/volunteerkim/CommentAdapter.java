package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments;
    private OnReplyClickListener replyClickListener;

    public CommentAdapter(List<Comment> comments, OnReplyClickListener listener) {
        this.comments = comments;
        this.replyClickListener = listener;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorText, contentText, timestampText;
        RecyclerView replyRecyclerView;
        Button btnReply;

        CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorText = itemView.findViewById(R.id.tv_comment_author);
            contentText = itemView.findViewById(R.id.tv_comment_content);
            timestampText = itemView.findViewById(R.id.tv_comment_timestamp);
            replyRecyclerView = itemView.findViewById(R.id.recyclerView_replies);
            btnReply = itemView.findViewById(R.id.btn_reply);
        }

    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.authorText.setText(comment.getAuthor());
        holder.contentText.setText(comment.getContent());
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(comment.getTimestamp().toDate());
            holder.timestampText.setText(formattedDate);
        }

        // 대댓글 RecyclerView 설정
        holder.replyRecyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));

        // 대댓글이 있는 경우에만 어댑터 설정
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            ReplyAdapter replyAdapter = new ReplyAdapter(comment.getReplies());
            holder.replyRecyclerView.setAdapter(replyAdapter);
            holder.replyRecyclerView.setVisibility(View.VISIBLE);
        } else {
            holder.replyRecyclerView.setVisibility(View.GONE);
        }

        // 답글 버튼 클릭 리스너 설정
        holder.btnReply.setOnClickListener(v -> {
            if (replyClickListener != null) {
                replyClickListener.onReplyClick(comment.getCommentId(), comment.getAuthor());
            }
        });

    }

    @Override
    public int getItemCount() {
        return comments != null ? comments.size() : 0;  
    }
}
