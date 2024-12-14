package com.example.volunteerkim;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
