package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NicknameActivity extends AppCompatActivity {

    private EditText etNickname;
    private Button btnSave;
    private FirebaseFirestore db;

    private String uid, email, displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // Intent로 전달된 데이터 가져오기
        uid = getIntent().getStringExtra("uid");
        email = getIntent().getStringExtra("email");

        etNickname = findViewById(R.id.et_nickname); // 닉네임 입력 필드
        btnSave = findViewById(R.id.btn_save); // 저장 버튼

        btnSave.setOnClickListener(v -> saveNickname());
    }

    private void saveNickname() {
        String nickname = etNickname.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)) {
            etNickname.setError("Nickname is required.");
            etNickname.requestFocus();
            return;
        }

        // Firestore에 사용자 데이터 저장
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("email", email);
        user.put("displayName", displayName);
        user.put("time", 0); // 초기 봉사시간 설정

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User profile created successfully!", Toast.LENGTH_SHORT).show();

                    // 메인 화면으로 이동
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
