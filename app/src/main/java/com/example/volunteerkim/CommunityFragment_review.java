package com.example.volunteerkim;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.volunteerkim.databinding.FragmentCommunityReviewBinding;
import com.example.volunteerkim.databinding.ItemPostBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;


public class CommunityFragment_review extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private FragmentCommunityReviewBinding binding;
    private List<ReviewPost> postList = new ArrayList<>();
    private MyAdapter adapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

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
        loadPosts();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new MyAdapter(postList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadPosts() {
        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "데이터 로드 실패", error);
                        return;
                    }
                    postList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        try {
                            ReviewPost post = new ReviewPost();
                            post.setPostId(doc.getId());
                            post.setPlace(doc.getString("place"));
                            post.setAddress(doc.getString("address"));
                            post.setCategory(doc.getString("category"));
                            post.setContent(doc.getString("content"));
                            post.setStartTime(String.valueOf(doc.get("startTime")));  // Long -> String 변환
                            post.setEndTime(String.valueOf(doc.get("endTime")));      // Long -> String 변환
                            post.setRating(doc.getDouble("rating").floatValue());
                            post.setHasImages(doc.getBoolean("hasImages"));
                            post.setImageUrls((List<String>) doc.get("imageUrls"));
                            postList.add(post);
                        } catch (Exception e) {
                            Log.e("Firestore", "데이터 변환 실패: " + doc.getId(), e);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemPostBinding binding;

        private MyViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ReviewPost post) {
            binding.tvName.setText(post.getPlace());
            binding.tvAddress.setText(post.getAddress());
            binding.ratingBar.setRating(post.getRating());
            binding.btnDetail.setOnClickListener(v -> {
                // 상세보기 화면으로 이동
                // TODO: 상세보기 Fragment로 전환
            });
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
        binding.btnAdd.setOnClickListener(v -> {
            CommunityFragment_review_post reviewPostFragment = new CommunityFragment_review_post();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // 새 화면 들어올 때
                            R.anim.slide_out_left,  // 현재 화면 나갈 때
                            R.anim.slide_in_left,   // 뒤로가기 시 들어올 때
                            R.anim.slide_out_right  // 뒤로가기 시 나갈 때
                    )
                    .replace(R.id.fragment_container, new CommunityFragment_review_post())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
