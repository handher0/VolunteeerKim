// src/main/java/com/example/volunteerkim/SignupActivity.java
package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etNickname;
    private Button btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // FirebaseAuth 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        // XML에서 레이아웃 요소 가져오기
        etEmail = findViewById(R.id.et_register_id);
        etPassword = findViewById(R.id.et_register_pw);
        etNickname = findViewById(R.id.et_register_nickname);
        btnRegister = findViewById(R.id.btn_register_button);

        // 회원가입 버튼 클릭 시 동작
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();

        // 입력값 검증
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required.");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required.");
            return;
        }
        if (TextUtils.isEmpty(nickname)) {
            etNickname.setError("Nickname is required.");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters.");
            return;
        }

        // Firebase Auth로 회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 회원가입 성공
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // LoginActivity로 이동
                        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish(); // 회원가입 완료 후 SignupActivity 종료
                    } else {
                        // 회원가입 실패
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError("This email is already registered.");
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e("SignupActivity", "Registration error: ", task.getException());
                        }
                    }
                });
    }
}
