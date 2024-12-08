package com.example.volunteerkim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunityAdapter extends RecyclerView.Adapter<CommunityAdapter.OtherViewHolder> {
    private List<OtherPost> posts;
    private String boardType;

    public CommunityAdapter(List<OtherPost> posts, String boardType) {
        this.posts = posts;
        this.boardType = boardType;
    }

    static class OtherViewHolder extends RecyclerView.ViewHolder {
        TextView tvStatus, tvTitle, tvAuthor, tvTimestamp, tvContent, tvRecruitmentPeriod;
        ImageView ivThumbnail;

        OtherViewHolder(View view) {
            super(view);
            tvStatus = view.findViewById(R.id.tv_status);
            tvTitle = view.findViewById(R.id.tv_title);
            tvAuthor = view.findViewById(R.id.tv_author);
            tvTimestamp = view.findViewById(R.id.tv_timestamp);
            tvContent = view.findViewById(R.id.tv_content);
            ivThumbnail = view.findViewById(R.id.iv_thumbnail);
            tvRecruitmentPeriod = view.findViewById(R.id.tv_recruitment_period);
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

        // Free 게시판일 경우 모집 상태와 기간 숨기기
        if (boardType.equals("Free")) {
            holder.tvStatus.setVisibility(View.GONE);
            holder.tvRecruitmentPeriod.setVisibility(View.GONE);
        } else {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvRecruitmentPeriod.setVisibility(View.VISIBLE);

            // 모집 상태 체크 및 표시
            Date currentDate = new Date();
            if (post.getRecruitmentStart() != null && post.getRecruitmentEnd() != null) {
                // 모집 기간 표시
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.getDefault());
                String period = String.format("%s~%s",
                        sdf.format(post.getRecruitmentStart()),
                        sdf.format(post.getRecruitmentEnd()));
                holder.tvRecruitmentPeriod.setText(period);


                // 모집 상태 체크 - 마감일만 체크하도록 수정
                if (currentDate.before(post.getRecruitmentEnd())) {
                    holder.tvStatus.setText("모집중");
                    holder.tvStatus.setBackgroundResource(R.color.selectedGreen);
                } else {
                    holder.tvStatus.setText("마감");
                    holder.tvStatus.setBackgroundResource(R.color.unselectedGray);
                }
            }
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

        // 아이템 클릭 리스너 추가
        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("postId", post.getPostId());
            args.putString("boardType", boardType);

            CommunityFragment_other_detail detailFragment = new CommunityFragment_other_detail();
            detailFragment.setArguments(args);

            // Fragment 전환
            ((FragmentActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // 새 화면 들어올 때
                            R.anim.slide_out_left,   // 현재 화면 나갈 때
                            R.anim.slide_in_left,    // 뒤로가기 시 들어올 때
                            R.anim.slide_out_right   // 뒤로가기 시 나갈 때
                    )
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
