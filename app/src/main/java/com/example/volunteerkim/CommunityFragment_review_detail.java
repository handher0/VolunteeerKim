package com.example.volunteerkim;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.volunteerkim.databinding.FragmentCommunityReviewDetailBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommunityFragment_review_detail extends Fragment {
    private FragmentCommunityReviewDetailBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String postId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            loadPostDetails();
        }

        setupUI();
        return binding.getRoot();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadPostDetails() {
        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // 작성자 정보
                        binding.tvAuthor.setText(document.getString("author"));

                        // 작성 시간
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        if (timestamp != null) {
                            binding.tvTimestamp.setText(formatTimestamp(timestamp));
                        }

                        // 봉사 장소 및 주소
                        binding.tvPlace.setText(document.getString("place"));
                        binding.tvAddress.setText(document.getString("address"));

                        // 별점 설정
                        Double rating = document.getDouble("rating");
                        if(rating != null){
                            binding.ratingBar.setRating(rating.floatValue());
                        }

                        // 카테고리 설정
                        String category = document.getString("category");
                        if (category != null) {
                            binding.tvCategory.setText(category);
                        }

                        // 봉사 시간 및 날짜
                        String volunteerDate = document.getString("volunteerDate");
                        String startTime = document.getString("startTime");
                        String endTime = document.getString("endTime");
                        binding.tvVolunteerTime.setText(String.format("봉사 시간: %s - %s", startTime, endTime));
                        binding.tvVolunteerDate.setText("봉사 날짜: " + volunteerDate);

                        // 게시글 내용
                        binding.tvContent.setText(document.getString("content"));

                        // 이미지 표시
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            setupImageScrollView(imageUrls);
                        }
                    }
                });
    }

    private void setupImageScrollView(List<String> imageUrls) {
        LinearLayout imageContainer = binding.imageContainer;
        imageContainer.removeAllViews();

        for (String url : imageUrls) {
            ImageView imageView = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(300), dpToPx(300));
            params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Glide.with(this)
                    .load(url)
                    .into(imageView);

            imageContainer.addView(imageView);
        }
    }

    private String formatTimestamp(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void sendComment() {
        String commentText = binding.etComment.getText().toString().trim();
        if (commentText.isEmpty()) return;

        Map<String, Object> comment = new HashMap<>();
        comment.put("content", commentText);
        comment.put("author", "현재 로그인한 사용자");  // TODO: 실제 사용자 정보로 대체
        comment.put("timestamp", FieldValue.serverTimestamp());

        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    binding.etComment.setText("");
                    Toast.makeText(getContext(), "댓글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                });
    }
}
