package com.example.volunteerkim;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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

    public HomeFragment() {
        // Required empty public constructor
    }

    // newInstance 메서드 추가
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

        // 닉네임 로드
        loadUserName();

        // 버튼 클릭 리스너 설정
        setupButtonListeners(view);

        return view;
    }

    private void loadUserName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid(); // 현재 사용자 UID 가져오기

            // Firestore에서 닉네임 가져오기
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname");
                            if (nickname != null) {
                                tvUserName.setText(nickname + " 봉사님 반갑습니다");
                            }
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
        // 총 봉사시간 버튼
        view.findViewById(R.id.btn_total_time).setOnClickListener(v -> navigateToFragment(new UtilTotalTimeFragment()));

        // 봉사 달력 버튼
        view.findViewById(R.id.btn_donation).setOnClickListener(v -> navigateToFragment(new UtilCalenderFragment()));

        // 봉사 랭킹 버튼
        view.findViewById(R.id.btn_ranking).setOnClickListener(v -> navigateToFragment(new UtilRankingFragment()));

        // 봉사 추천 버튼
        view.findViewById(R.id.btn_recommendation).setOnClickListener(v -> navigateToFragment(new UtilRecomendationFragment()));
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null); // 뒤로 가기 스택에 추가
        transaction.commit();
    }
}
