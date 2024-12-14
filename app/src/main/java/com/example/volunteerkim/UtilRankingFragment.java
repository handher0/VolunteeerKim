package com.example.volunteerkim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UtilRankingFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private RankingsAdapter adapter;
    private List<UserScore> userScores = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            navigateToFragment(new HomeFragment());
        });
        fetchRankings();

        return view;
    }

    private void fetchRankings() {
        db.collection("UserProfiles").get()
                .addOnSuccessListener(querySnapshot -> {
                    userScores.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String nickname = document.getId();
                        Number education = (Number) document.get("Summary.categoryHours.overall.education");
                        Number environment = (Number) document.get("Summary.categoryHours.overall.environment");
                        Number care = (Number) document.get("Summary.categoryHours.overall.care");
                        Number medical = (Number) document.get("Summary.categoryHours.overall.medical");

                        float total = 0;
                        if (education != null) total += education.floatValue();
                        if (environment != null) total += environment.floatValue();
                        if (care != null) total += care.floatValue();
                        if (medical != null) total += medical.floatValue();

                        userScores.add(new UserScore(nickname, total/60));
                    }

                    // 랭킹을 점수에 따라 내림차순으로 정렬
                    Collections.sort(userScores, (u1, u2) -> Float.compare(u2.getScore(), u1.getScore()));

                    adapter = new RankingsAdapter(userScores);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> Log.e("RankingsFragment", "Error fetching data", e));
    }

    // UserScore 클래스 정의
    public static class UserScore {
        private String nickname;
        private float score;

        public UserScore(String nickname, float score) {
            this.nickname = nickname;
            this.score = score;
        }

        public String getNickname() {
            return nickname;
        }

        public float getScore() {
            return score;
        }
    }

    private void navigateToFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        // 애니메이션 설정
        transaction.setCustomAnimations(
//                R.anim.slide_in_right,  // 새로운 Fragment 들어올 때
//                R.anim.slide_out_left,  // 현재 Fragment 나갈 때
                R.anim.slide_in_left,   // 뒤로가기할 때 들어오는 애니메이션
                R.anim.slide_out_right  // 뒤로가기할 때 나가는 애니메이션
        );

        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
