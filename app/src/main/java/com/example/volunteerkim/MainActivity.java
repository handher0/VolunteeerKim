package com.example.volunteerkim;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 초기 화면을 HomeFragment로 설정
        if (savedInstanceState == null) { // Activity가 새로 생성된 경우에만 실행
            transferTo(HomeFragment.newInstance("param1", "param2"));
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 네비게이션 아이템 선택 리스너
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.page_1) {
                    transferTo(HomeFragment.newInstance("param1", "param2"));
                    return true;
                }
                if (itemId == R.id.page_2) {
                    transferTo(CommunityFragment_review.newInstance("param1", "param2"));
                    return true;
                }
                if (itemId == R.id.page_4) {
                    transferTo(ChatFragment.newInstance("param1", "param2"));
                    return true;
                }
                if (itemId == R.id.page_5) {
                    transferTo(MyPageFragment.newInstance("param1", "param2"));
                    return true;
                }
                return false;
            }
        });

        // 네비게이션 아이템 재선택 리스너
        bottomNavigationView.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                // 재선택 시 동작 처리 (현재는 아무 동작도 하지 않음)
            }
        });
    }

    // 프래그먼트를 교체하는 메서드
    private void transferTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
