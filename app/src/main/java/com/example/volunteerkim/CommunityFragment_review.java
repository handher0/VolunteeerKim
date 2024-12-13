package com.example.volunteerkim;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.volunteerkim.databinding.FragmentCommunityReviewBinding;
import com.example.volunteerkim.databinding.ItemPostBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CommunityFragment_review extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentCommunityReviewBinding binding;
    private List<ReviewPost> reviewList = new ArrayList<>();
    private List<OtherPost> otherList = new ArrayList<>();
    private CommunityAdapter otherAdapter;
    private MyAdapter reviewAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentBoardType = "Review";


    public static CommunityFragment_review newInstance(String param1, String param2) {
        CommunityFragment_review fragment = new CommunityFragment_review();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewBinding.inflate(inflater, container, false);
        setupRecyclerView();
        setupButtons();
        loadPosts(currentBoardType);
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        reviewAdapter = new MyAdapter(reviewList);
        otherAdapter = new CommunityAdapter(otherList, currentBoardType);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // 현재 게시판 타입에 따라 적절한 어댑터 설정
        if (currentBoardType.equals("Review")) {
            binding.recyclerView.setAdapter(reviewAdapter);
        } else {
            binding.recyclerView.setAdapter(otherAdapter);
        }
    }

    private void loadPosts(String boardType) {
        currentBoardType = boardType;
        otherAdapter = new CommunityAdapter(otherList, currentBoardType);

        switch (boardType) {
            case "Review":
                updateButtonStates(binding.btnReview);
                break;
            case "Help":
                updateButtonStates(binding.btnHelp);
                break;
            case "Mate":
                updateButtonStates(binding.btnMate);
                break;
            case "Free":
                updateButtonStates(binding.btnFree);
                break;
        }

        db.collection("Boards")
                .document(boardType)
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (binding == null || !isAdded()) return;

                    if (error != null || value == null) return;

                    if (boardType.equals("Review")) {
                        reviewList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                ReviewPost post = createReviewPost(doc);
                                reviewList.add(post);
                            } catch (Exception e) {
                                Log.e("Firestore", "데이터 변환 실패: " + doc.getId(), e);
                            }
                        }
                        binding.recyclerView.setAdapter(reviewAdapter);
                        reviewAdapter.notifyDataSetChanged();
                    } else {
                        otherList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                OtherPost post = createOtherPost(doc);
                                otherList.add(post);
                            } catch (Exception e) {
                                Log.e("Firestore", "데이터 변환 실패: " + doc.getId(), e);
                            }
                        }
                        binding.recyclerView.setAdapter(otherAdapter);
                        otherAdapter.notifyDataSetChanged();
                    }
                });
    }

    private ReviewPost createReviewPost(DocumentSnapshot doc) {
        ReviewPost post = new ReviewPost();
        post.setPostId(doc.getId());
        post.setAuthor(doc.getString("author"));
        post.setContent(doc.getString("content"));
        post.setHasImages(doc.getBoolean("hasImages"));
        post.setImageUrls((List<String>) doc.get("imageUrls"));
        Double rating = doc.getDouble("rating");
        post.setRating(rating != null ? rating.floatValue() : 0.0f);
        post.setPlace(doc.getString("place"));
        post.setAddress(doc.getString("address"));
        post.setCategory(doc.getString("category"));
        post.setTimestamp(doc.getTimestamp("timestamp"));
        return post;
    }

    private OtherPost createOtherPost(DocumentSnapshot doc) {
        OtherPost post = new OtherPost();
        post.setPostId(doc.getId());
        post.setAuthor(doc.getString("author"));
        post.setContent(doc.getString("content"));
        post.setHasImages(doc.getBoolean("hasImages"));
        post.setImageUrls((List<String>) doc.get("imageUrls"));
        post.setTitle(doc.getString("title"));
        post.setTimestamp(doc.getTimestamp("timestamp"));
        Timestamp startTimestamp = doc.getTimestamp("recruitmentStart");
        Timestamp endTimestamp = doc.getTimestamp("recruitmentEnd");
        post.setRecruitmentStart(startTimestamp != null ? startTimestamp.toDate() : null);
        post.setRecruitmentEnd(endTimestamp != null ? endTimestamp.toDate() : null);

        return post;
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemPostBinding binding;

        private MyViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<ReviewPost> list;

        private MyAdapter(List<ReviewPost> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPostBinding binding = ItemPostBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ReviewPost post = list.get(position);
            holder.binding.tvName.setText(post.getPlace());
            holder.binding.tvAddress.setText(post.getAddress());
            holder.binding.ratingBar.setRating(post.getRating());
            holder.binding.tvAuthor.setText(post.getAuthor());
            // 타임스탬프 포맷팅 및 표시
            if (post.getTimestamp() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
                holder.binding.tvTimestamp.setText(sdf.format(post.getTimestamp().toDate()));
            }

            // 아이템 전체 클릭 리스너 설정
            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("postId", post.getPostId());

                CommunityFragment_review_detail detailFragment = new CommunityFragment_review_detail();
                detailFragment.setArguments(args);

                // Fragment 전환
                requireActivity().getSupportFragmentManager()
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
            return list.size();
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setupButtons();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateButtonStates(View selectedButton) {
        // 모든 버튼 기본 상태로 초기화
        binding.btnReview.setBackgroundColor(Color.TRANSPARENT);
        binding.btnReview.setTextColor(getResources().getColor(R.color.black));

        binding.btnHelp.setBackgroundColor(Color.TRANSPARENT);
        binding.btnHelp.setTextColor(getResources().getColor(R.color.black));

        binding.btnMate.setBackgroundColor(Color.TRANSPARENT);
        binding.btnMate.setTextColor(getResources().getColor(R.color.black));

        binding.btnFree.setBackgroundColor(Color.TRANSPARENT);
        binding.btnFree.setTextColor(getResources().getColor(R.color.black));

        // 선택된 버튼만 연두색 배경과 흰색 텍스트로 변경
        selectedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedGreen));
        ((TextView) selectedButton).setTextColor(Color.WHITE);
    }

    private void setupButtons() {
        binding.btnAdd.setOnClickListener(v -> {
            Fragment targetFragment = currentBoardType.equals("Review") ?
                    new CommunityFragment_review_post() :
                    new CommunityFragment_other_post();

            if (!currentBoardType.equals("Review")) {
                Bundle args = new Bundle();
                args.putString("boardType", currentBoardType);
                targetFragment.setArguments(args);
            }

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.fragment_container, targetFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnReview.setOnClickListener(v -> {
            updateButtonStates(v);
            loadPosts("Review");
        });

        binding.btnHelp.setOnClickListener(v -> {
            updateButtonStates(v);
            loadPosts("Help");
        });

        binding.btnMate.setOnClickListener(v -> {
            updateButtonStates(v);
            loadPosts("Mate");
        });

        binding.btnFree.setOnClickListener(v -> {
            updateButtonStates(v);
            loadPosts("Free");
        });
    }

}
