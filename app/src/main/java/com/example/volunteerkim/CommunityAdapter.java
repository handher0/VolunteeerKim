package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.OtherViewHolder> {
    private List<OtherPost> posts;

    public CommunityAdapter(List<OtherPost> posts) {
        this.posts = posts;
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTitle, tvAuthor, tvTimestamp, tvContent;
        ImageView ivThumbnail;

        OtherViewHolder(View view) {
            super(view);
            tvStatus = view.findViewById(R.id.tv_status);
            tvTitle = view.findViewById(R.id.tv_title);
            tvAuthor = view.findViewById(R.id.tv_author);
            tvTimestamp = view.findViewById(R.id.tv_timestamp);
            tvContent = view.findViewById(R.id.tv_content);
            ivThumbnail = view.findViewById(R.id.iv_thumbnail);
        }
    }

    @NonNull
    @Override
    public OtherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post_other, parent, false);
        return new OtherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OtherViewHolder holder, int position) {
        OtherPost post = posts.get(position);

        // 모집 상태 체크 및 표시
        Date currentDate = new Date();
        if (post.getRecruitmentEnd() != null && currentDate.before(post.getRecruitmentEnd())) {
            holder.tvStatus.setText("모집중");
            holder.tvStatus.setBackgroundResource(R.color.selectedGreen);
        } else {
            holder.tvStatus.setText("마감");
            holder.tvStatus.setBackgroundResource(R.color.unselectedGreen);
        }

        holder.tvTitle.setText(post.getTitle());
        holder.tvAuthor.setText(post.getAuthor());

        if (post.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(post.getTimestamp().toDate()));
        }

        holder.tvContent.setText(post.getContent());

        if (post.isHasImages() && post.getImageUrls() != null && !post.getImageUrls().isEmpty()) {
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrls().get(0))
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
