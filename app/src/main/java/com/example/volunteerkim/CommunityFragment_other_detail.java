package com.example.volunteerkim;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.volunteerkim.databinding.FragmentCommunityOtherDetailBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommunityFragment_other_detail extends Fragment {
    private FragmentCommunityOtherDetailBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CommentAdapter commentAdapter;
    private String postId;
    private List<Comment> commentList = new ArrayList<>();
    private String boardType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityOtherDetailBinding.inflate(inflater, container, false);

//        binding.contentLayout.setVisibility(View.GONE);
//        binding.progressBar.setVisibility(View.VISIBLE);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            boardType = getArguments().getString("boardType");
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
        Log.d("CommunityOtherDetail", "Loading post details for postId: " + postId + ", boardType: " + boardType);
        db.collection("Boards")
                .document(boardType)
                .collection("Posts")
                .document(postId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Log.d("CommunityOtherDetail", "Post document loaded successfully");
                        if (boardType.equals("Free")) {
                            binding.tvStatus.setVisibility(View.GONE);
                            binding.tvRecruitmentPeriod.setVisibility(View.GONE);
                        } else {
                            binding.tvStatus.setVisibility(View.VISIBLE);
                            binding.tvRecruitmentPeriod.setVisibility(View.VISIBLE);
                        }
                        // 작성자 정보
                        String author = document.getString("author");
                        binding.tvAuthor.setText(author != null ? author : "익명");

                        // 작성 시간
                        Timestamp timestamp = document.getTimestamp("timestamp");
                        if (timestamp != null) {
                            binding.tvTimestamp.setText(formatTimestamp(timestamp));
                        }

                        // 모집 상태 체크 및 표시
                        Date currentDate = new Date();
                        Timestamp endTimestamp = document.getTimestamp("recruitmentEnd");
                        if (endTimestamp != null && currentDate.before(endTimestamp.toDate())) {
                            binding.tvStatus.setText("모집중");
                            binding.tvStatus.setBackgroundResource(R.color.selectedGreen);
                        } else {
                            binding.tvStatus.setText("마감");
                            binding.tvStatus.setBackgroundResource(R.color.unselectedGray);
                        }

                        // 제목과 내용
                        binding.tvTitle.setText(document.getString("title"));
                        binding.tvContent.setText(document.getString("content"));

                        // 모집 기간
                        Timestamp startTimestamp = document.getTimestamp("recruitmentStart");
                        if (startTimestamp != null && endTimestamp != null) {
                            String period = String.format("모집기간: %s ~ %s",
                                    formatDate(startTimestamp),
                                    formatDate(endTimestamp));
                            binding.tvRecruitmentPeriod.setText(period);
                        }

                        // 이미지 표시
                        List<String> imageUrls = (List<String>) document.get("imageUrls");
                        if (imageUrls != null && !imageUrls.isEmpty()) {
                            setupImageScrollView(imageUrls);
                        }
                        Log.d("OtherDetail", "Data binding completed");
                        // 데이터 로딩이 완료되면 컨텐츠 표시
//                        binding.progressBar.setVisibility(View.GONE);
//                        binding.contentLayout.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("OtherDetail", "Document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OtherDetail", "Error loading post details", e);
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

    private String formatDate(Timestamp timestamp) {
        Date date = timestamp.toDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private int dpToPx(int dp) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    // 댓글 관련 메서드들은 리뷰 게시판과 동일하게 구현
    private void loadComments() {
        db.collection("Boards")
                .document(boardType)
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
                            .document(boardType)
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
                                .document(boardType)
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

    private void setupReplyListener(Comment comment, String commentId) {
        db.collection("Boards")
                .document(boardType)
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
