package com.example.volunteerkim;

import android.app.AlertDialog;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.volunteerkim.databinding.FragmentCommunityReviewDetailBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommunityFragment_review_detail extends Fragment {
    private FragmentCommunityReviewDetailBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CommentAdapter commentAdapter;
    private String postId;
    private List<Comment> commentList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewDetailBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            loadPostDetails();
        }

        setupRecyclerView();
        setupCommentInput();
        loadComments();

        setupUI();
        return binding.getRoot();
    }

    private void setupUI() {
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void loadPostDetails() {
        db.collection("Boards")
                    .document("Review")
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Log.d("CommunityOtherDetail", "Post document loaded successfully");
                        // 작성자 정보
                        String author = document.getString("author");
                        binding.tvAuthor.setText(author != null ? author : "익명");

                        // 현재 로그인한 사용자의 닉네임 가져오기
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            db.collection("users")
                                    .document(currentUser.getUid())
                                    .get()
                                    .addOnSuccessListener(userDoc -> {
                                        String userNickname = userDoc.getString("nickname");
                                        // 게시글 작성자와 현재 사용자의 닉네임 비교
                                        if (userNickname != null && userNickname.equals(author)) {
                                            binding.btnDelete.setVisibility(View.VISIBLE);
                                            binding.btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
                                        } else {
                                            binding.btnDelete.setVisibility(View.GONE);
                                        }
                                    });
                        }

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
        if (!isAdded()) return;
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

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter(commentList, new OnReplyClickListener() {
            @Override
            public void onReplyClick(String commentId, String authorName) {
                setupReplyInput(commentId, authorName);
            }
        });
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewComments.setAdapter(commentAdapter);
    }

    private void setupCommentInput() {
        binding.btnSendComment.setOnClickListener(v -> {
            String commentText = binding.etComment.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(getContext(), "댓글을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname == null) nickname = "익명";

                        Map<String, Object> comment = new HashMap<>();
                        comment.put("content", commentText);
                        comment.put("author", nickname);
                        comment.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("Boards")
                                .document("Review")
                                .collection("Posts")
                                .document(postId)
                                .collection("Comments")
                                .add(comment)
                                .addOnSuccessListener(documentReference -> {
                                    binding.etComment.setText("");
                                    Toast.makeText(getContext(), "댓글이 등록되었습니다", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "댓글 등록에 실패했습니다", Toast.LENGTH_SHORT).show();
                                });
                    });
        });
    }


    private void setupReplyInput(String commentId, String parentAuthor) {
        // 답글 입력 UI 표시
        binding.etComment.setHint(parentAuthor + "님에게 답글 작성");

        // 기존 클릭 리스너 제거하고 새로운 one-time 리스너 설정
        binding.btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String replyText = binding.etComment.getText().toString().trim();
                if (replyText.isEmpty()) {
                    Toast.makeText(getContext(), "답글을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 답글 저장 로직...
                saveReply(commentId, replyText);

                // 답글 작성 완료 후 일반 댓글 모드로 복귀
                binding.etComment.setHint("댓글을 입력하세요");
                setupCommentInput();  // 일반 댓글 입력 리스너로 복귀
            }
        });
    }

    private void loadComments() {
        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "댓글 로드 실패", error);
                        return;
                    }

                    commentList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = new Comment();
                            comment.setCommentId(doc.getId());
                            comment.setContent(doc.getString("content"));
                            comment.setAuthor(doc.getString("author"));
                            comment.setTimestamp(doc.getTimestamp("timestamp"));

                            // 대댓글 로드
                            setupReplyListener(comment, doc.getId());

                            commentList.add(comment);
                        }
                    }
                    commentAdapter.notifyDataSetChanged();
                });
    }

    // 답글 저장을 위한 별도 메서드
    private void saveReply(String commentId, String replyText) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String nickname = documentSnapshot.getString("nickname");
                    if (nickname == null) nickname = "익명";

                    Map<String, Object> reply = new HashMap<>();
                    reply.put("content", replyText);
                    reply.put("author", nickname);
                    reply.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("Boards")
                            .document("Review")
                            .collection("Posts")
                            .document(postId)
                            .collection("Comments")
                            .document(commentId)
                            .collection("Replies")
                            .add(reply)
                            .addOnSuccessListener(documentReference -> {
                                binding.etComment.setText("");
                                Toast.makeText(getContext(), "답글이 등록되었습니다", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void setupReplyListener(Comment comment, String commentId) {
        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(postId)
                .collection("Comments")
                .document(commentId)
                .collection("Replies")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    List<Reply> replies = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Reply reply = new Reply();
                            reply.setReplyId(doc.getId());
                            reply.setContent(doc.getString("content"));
                            reply.setAuthor(doc.getString("author"));
                            reply.setTimestamp(doc.getTimestamp("timestamp"));
                            replies.add(reply);
                        }
                    }
                    comment.setReplies(replies);
                    commentAdapter.notifyDataSetChanged();
                });
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("게시글 삭제")
                .setMessage("정말 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deletePost())
                .setNegativeButton("취소", null)
                .show();
    }

    private void deletePost() {
        String deletedPostId = postId;  // 삭제할 게시글 ID 저장

        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(deletedPostId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // 이미지 URL 목록 가져오기
                        List<String> imageUrls = (List<String>) document.get("imageUrls");

                        // 게시글 문서 삭제
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // 게시글 삭제 성공 후 Storage의 이미지도 삭제
                                    if (imageUrls != null && !imageUrls.isEmpty()) {
                                        for (String imageUrl : imageUrls) {
                                            FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
                                        }
                                    }

                                    // Posts 컬렉션에서 해당 게시글 ID 문서도 삭제
                                    db.collection("Posts")
                                            .document(deletedPostId)
                                            .delete()
                                            .addOnSuccessListener(unused -> {
                                                Toast.makeText(getContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                requireActivity().onBackPressed();
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("DeletePost", "Posts 문서 삭제 실패", e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "게시글 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

}
