package com.example.volunteerkim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

public class HomeFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private TextView tvUserName;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ViewPager2 viewPagerBanner;
    private Handler sliderHandler;
    private RecyclerView recyclerReviews;
    private RecyclerView recyclerMates;
    private MyAdapter reviewAdapter;
    private CommunityAdapter mateAdapter;
    private List<ReviewPost> reviewList = new ArrayList<>();
    private List<OtherPost> mateList = new ArrayList<>();


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // TextView 초기화
        tvUserName = view.findViewById(R.id.tv_user_name);

        // ViewPager2 초기화
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);

        // 닉네임 로드
        loadUserName();

        // 버튼 클릭 리스너 설정
        setupButtonListeners(view);

        // 배너 슬라이더 설정
        setupBannerSlider();

        setupRecyclerViews(view);
        loadReviewPosts();
        loadMatePosts();


        return view;
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname");
                            tvUserName.setText(nickname != null ? nickname + " 봉사님 반갑습니다" : "봉사님 반갑습니다");
                        } else {
                            tvUserName.setText("봉사님 반갑습니다");
                            Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        tvUserName.setText("봉사님 반갑습니다");
                        Toast.makeText(getContext(), "Failed to load user data.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            tvUserName.setText("봉사님 반갑습니다");
        }
    }

    private void setupButtonListeners(View view) {
        view.findViewById(R.id.btn_total_time).setOnClickListener(v -> navigateToFragment(new UtilTotalTimeFragment()));
        view.findViewById(R.id.btn_donation).setOnClickListener(v -> navigateToFragment(new UtilCalenderFragment()));
        view.findViewById(R.id.btn_ranking).setOnClickListener(v -> navigateToFragment(new UtilRankingFragment()));
        view.findViewById(R.id.btn_recommendation).setOnClickListener(v -> navigateToFragment(new UtilRecomendationFragment()));
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupRecyclerViews(View view) {
        // 리뷰 RecyclerView 설정
        recyclerReviews = view.findViewById(R.id.recycler_reviews);
        recyclerReviews.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        reviewAdapter = new MyAdapter(reviewList);
        recyclerReviews.setAdapter(reviewAdapter);

        // 리뷰 RecyclerView에 SnapHelper 추가
        LinearSnapHelper snapHelperReview = new LinearSnapHelper();
        snapHelperReview.attachToRecyclerView(recyclerReviews);

        // 메이트 RecyclerView 설정
        recyclerMates = view.findViewById(R.id.recycler_mates);
        recyclerMates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mateAdapter = new CommunityAdapter(mateList, "Mate");
        recyclerMates.setAdapter(mateAdapter);

        // 메이트 RecyclerView에 SnapHelper 추가
        LinearSnapHelper snapHelperMate = new LinearSnapHelper();
        snapHelperMate.attachToRecyclerView(recyclerMates);
    }

    private void loadReviewPosts() {
        db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    reviewList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        try {
                            ReviewPost post = createReviewPost(doc);
                            reviewList.add(post);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "리뷰 게시글 로드 오류", e);
                        }
                    }
                    reviewAdapter.notifyDataSetChanged();
                });
    }

    private void loadMatePosts() {
        db.collection("Boards")
                .document("Mate")
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)  // 최근 5개만 표시
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    mateList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        try {
                            OtherPost post = createOtherPost(doc);
                            mateList.add(post);
                        } catch (Exception e) {
                            Log.e("HomeFragment", "메이트 게시글 로드 오류", e);
                        }
                    }
                    mateAdapter.notifyDataSetChanged();
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

    private class MyAdapter extends RecyclerView.Adapter<HomeFragment.MyViewHolder> {
        private List<ReviewPost> list;

        private MyAdapter(List<ReviewPost> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public HomeFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPostBinding binding = ItemPostBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new HomeFragment.MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull HomeFragment.MyViewHolder holder, int position) {
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






    private void setupBannerSlider() {
        int[] bannerImages = {R.drawable.ad1, R.drawable.ad2, R.drawable.ad3, R.drawable.ad4, R.drawable.ad5};
        BannerAdapter bannerAdapter = new BannerAdapter(requireContext(), bannerImages);
        viewPagerBanner.setAdapter(bannerAdapter);

        sliderHandler = new Handler(Looper.getMainLooper());
        viewPagerBanner.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 2000);
            }
        });
    }

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int currentItem = viewPagerBanner.getCurrentItem();
            int itemCount = viewPagerBanner.getAdapter() != null ? viewPagerBanner.getAdapter().getItemCount() : 0;
            if (itemCount > 0) {
                viewPagerBanner.setCurrentItem((currentItem + 1) % itemCount, true);
            }
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
}
