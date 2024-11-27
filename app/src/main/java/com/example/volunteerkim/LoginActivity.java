package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // 로그 태그
    private EditText etEmail, etPassword; // 이메일과 비밀번호 필드
    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화

        etEmail = findViewById(R.id.et_email); // 이메일 입력 필드
        etPassword = findViewById(R.id.et_password); // 비밀번호 입력 필드

        // 로그인 버튼 클릭 리스너
        findViewById(R.id.btn_login).setOnClickListener(v -> loginUser());

        // 회원가입 버튼 클릭 리스너
        findViewById(R.id.btn_sign_in).setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim(); // 이메일 입력 값
        String password = etPassword.getText().toString().trim(); // 비밀번호 입력 값

        Log.d(TAG, "Login attempt with email: " + email); // 로그: 입력된 이메일 확인

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required."); // 이메일 미입력 시 에러 표시
            Log.e(TAG, "Login failed: Email is empty."); // 로그: 이메일 미입력
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required."); // 비밀번호 미입력 시 에러 표시
            Log.e(TAG, "Login failed: Password is empty."); // 로그: 비밀번호 미입력
            return;
        }

        // Firebase Authentication으로 이메일과 비밀번호 인증
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공
                        Log.d(TAG, "Login successful for email: " + email); // 로그: 로그인 성공
                        Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                        // 메인 화면으로 이동
                        Intent intent = new Intent(this, MainActivity.class);
                        startActivity(intent);
                        Log.d(TAG, "Navigating to MainActivity."); // 로그: 메인 액티비티로 이동 시도
                        finish(); // 현재 액티비티 종료
                    } else {
                        // 로그인 실패
                        Log.e(TAG, "Login failed: " + task.getException()); // 로그: 로그인 실패 원인
                        Toast.makeText(getApplicationContext(), "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
