package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RankingsAdapter extends RecyclerView.Adapter<RankingsAdapter.ViewHolder> {

    private List<UtilRankingFragment.UserScore> userScores;

    public RankingsAdapter(List<UtilRankingFragment.UserScore> userScores) {
        this.userScores = userScores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup itemView = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rankings_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UtilRankingFragment.UserScore userScore = userScores.get(position);
        holder.rank.setText(String.valueOf(position + 1));
        holder.nickname.setText(userScore.getNickname());
        holder.score.setText(String.format("%.1f 시간", userScore.getScore()));
    }

    @Override
    public int getItemCount() {
        return userScores.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rank, nickname, score;

        public ViewHolder(ViewGroup itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.rank);
            nickname = itemView.findViewById(R.id.nickname);
            score = itemView.findViewById(R.id.score);
        }
    }
}
