package com.example.volunteerkim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String user1id; // 현재 로그인한 사용자 닉네임
    private String uid; // 현재 로그인한 사용자 UID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_VISIBLE
        );

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // 현재 로그인한 사용자 UID 가져오기
        uid = auth.getCurrentUser().getUid();

        // Firestore에서 닉네임 가져오기
        loadNickname();

        // 초기 화면을 HomeFragment로 설정
        if (savedInstanceState == null) { // Activity가 새로 생성된 경우에만 실행
            transferTo(new HomeFragment());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 네비게이션 아이템 선택 리스너
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.page_1) {
                    transferTo(new HomeFragment());
                    return true;
                }
                if (itemId == R.id.page_2) {
                    transferTo(new CommunityFragment_review());
                    return true;
                }
                if (itemId == R.id.page_3) {
                    // 페이지 3 클릭 시 외부 링크로 이동
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.vms.or.kr/main.do"));
                    startActivity(browserIntent);
                    return true;
                }
                if (itemId == R.id.page_4) {
                    if (user1id != null) {
                        // user1id가 이미 로드된 경우 바로 ChatFragment로 이동
                        transferTo(ChatFragment.newInstance(user1id));
                    } else {
                        // 닉네임이 아직 로드되지 않은 경우 처리
                        Toast.makeText(MainActivity.this, "Loading user data, please wait...", Toast.LENGTH_SHORT).show();
                        // 닉네임 로드 후 ChatFragment로 이동
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        user1id = documentSnapshot.getString("nickname");
                                        if (user1id != null) {
                                            transferTo(ChatFragment.newInstance(user1id));
                                        } else {
                                            Toast.makeText(MainActivity.this, "Nickname not found.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show());
                    }
                    return true;
                }

                if (itemId == R.id.page_5) {
                    transferTo(MyPageFragment.newInstance(uid));
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

    /**
     * Firestore에서 닉네임 가져오기
     */
    private void loadNickname() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        user1id = documentSnapshot.getString("nickname");
                        if (user1id == null) {
                            Toast.makeText(this, "Nickname not found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show());
    }

    // 프래그먼트를 교체하는 메서드
    private void transferTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }
}
