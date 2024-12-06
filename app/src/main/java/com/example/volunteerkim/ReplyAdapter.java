package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {
    private List<Reply> replies;

    public ReplyAdapter(List<Reply> replies) {
        this.replies = replies;
    }

    static class ReplyViewHolder extends RecyclerView.ViewHolder {
        private TextView authorText;
        private TextView contentText;
        private TextView timestampText;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);
            authorText = itemView.findViewById(R.id.tv_reply_author);
            contentText = itemView.findViewById(R.id.tv_reply_content);
            timestampText = itemView.findViewById(R.id.tv_reply_timestamp);
        }
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reply, parent, false);
        return new ReplyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        Reply reply = replies.get(position);
        holder.authorText.setText(reply.getAuthor());
        holder.contentText.setText(reply.getContent());

        // timestamp null 체크 추가
        if (reply.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
            String formattedDate = sdf.format(reply.getTimestamp().toDate());
            holder.timestampText.setText(formattedDate);
        } else {
            holder.timestampText.setText("방금 전");  // 또는 다른 기본값
        }
    }

    @Override
    public int getItemCount() {
        return replies != null ? replies.size() : 0;
    }
}
