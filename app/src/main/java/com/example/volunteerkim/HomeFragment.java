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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvUserName;
    private TextView tvTodoList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ViewPager2 viewPagerBanner;
    private Handler sliderHandler;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // TextView 초기화
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTodoList = view.findViewById(R.id.tv_todo_list);

        // ViewPager2 초기화
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner);

        // 닉네임 로드
        loadUserName();

        // 할일 목록 로드
        loadTodoList();

        // 버튼 클릭 리스너 설정
        setupButtonListeners(view);

        // 배너 슬라이더 설정
        setupBannerSlider();

        return view;
    }

    private void loadUserName() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");
                        tvUserName.setText(nickname != null ? nickname + " 봉사님 반갑습니다" : "봉사님 반갑습니다");
                    } else {
                        tvUserName.setText("봉사님 반갑습니다");
                        Toast.makeText(getContext(), "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("봉사님 반갑습니다");
                    Toast.makeText(getContext(), "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadTodoList() {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("events").get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        List<Event> eventsList = new ArrayList<>();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String today = sdf.format(new Date());

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String date = document.getId();
                            String event = document.getString("event");

                            try {
                                if (sdf.parse(date).compareTo(sdf.parse(today)) >= 0) {
                                    eventsList.add(new Event(date, event));
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        // 날짜순 정렬
                        Collections.sort(eventsList, Comparator.comparing(event -> {
                            try {
                                return sdf.parse(event.getDate());
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));

                        updateTodoList(eventsList);
                    } else {
                        tvTodoList.setText("등록된 일정이 없습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    tvTodoList.setText("일정을 불러오는 데 실패했습니다.");
                    Toast.makeText(getContext(), "일정을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTodoList(List<Event> eventsList) {
        if (eventsList.isEmpty()) {
            tvTodoList.setText("등록된 일정이 없습니다.");
            return;
        }

        StringBuilder todoText = new StringBuilder("");
        for (Event event : eventsList) {
            todoText.append(event.getDate()).append(": ").append(event.getEvent()).append("\n");
        }
        tvTodoList.setText(todoText.toString());
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

    // Event 클래스 정의
    private static class Event {
        private final String date;
        private final String event;

        public Event(String date, String event) {
            this.date = date;
            this.event = event;
        }

        public String getDate() {
            return date;
        }

        public String getEvent() {
            return event;
        }
    }
}
