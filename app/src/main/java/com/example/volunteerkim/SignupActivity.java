package com.example.volunteerkim;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etId, etPassword, etPasswordConfirm, etEmail;
    private CheckBox cbTerms1, cbTerms2;
    private Button btnCheckId, btnNext;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);

        // FirebaseAuth 초기화
        mAuth = FirebaseAuth.getInstance();

        // XML에서 UI 요소 초기화
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        etEmail = findViewById(R.id.et_email);
        cbTerms1 = findViewById(R.id.cb_terms_1);
        cbTerms2 = findViewById(R.id.cb_terms_2);
        btnCheckId = findViewById(R.id.btn_check_id);
        btnNext = findViewById(R.id.btn_next);

        // ID 중복 확인 버튼
        btnCheckId.setOnClickListener(v -> checkDuplicateId());

        // 회원가입 버튼
        btnNext.setOnClickListener(v -> registerUser());
    }

    private void checkDuplicateId() {
        // ID 입력값 가져오기
        String id = etId.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            etId.setError("ID is required.");
            return;
        }

        // Firestore 초기화
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Firestore에서 ID가 존재하는지 확인
        db.collection("users")
                .whereEqualTo("nickname", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // ID가 이미 존재할 경우
                            etId.setError("This ID is already taken.");
                            Toast.makeText(this, "This ID is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                        } else {
                            // ID가 사용 가능할 경우
                            Toast.makeText(this, "ID is available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Firestore 요청 실패
                        Toast.makeText(this, "Failed to check ID: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void registerUser() {
        String id = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        // 입력값 검증
        if (TextUtils.isEmpty(id)) {
            etId.setError("ID is required.");
            etId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required.");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email.");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required.");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters.");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("Passwords do not match.");
            etPasswordConfirm.requestFocus();
            return;
        }
        if (!cbTerms1.isChecked() || !cbTerms2.isChecked()) {
            Toast.makeText(this, "Please agree to all terms and conditions.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication을 사용한 회원가입
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Firestore 초기화
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Firestore에서 닉네임 중복 확인
                        db.collection("users")
                                .whereEqualTo("nickname", id)
                                .get()
                                .addOnCompleteListener(nicknameTask -> {
                                    if (nicknameTask.isSuccessful()) {
                                        if (!nicknameTask.getResult().isEmpty()) {
                                            // 닉네임 중복 발견
                                            Toast.makeText(SignupActivity.this, "This nickname is already taken.", Toast.LENGTH_SHORT).show();
                                            FirebaseAuth.getInstance().getCurrentUser().delete(); // 이메일 등록 취소
                                        } else {
                                            // 닉네임 중복 없음, Firestore에 UID와 닉네임 저장
                                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                            saveUserToFirestore(uid, id);
                                        }
                                    } else {
                                        // Firestore 닉네임 확인 실패
                                        Toast.makeText(SignupActivity.this, "Failed to check nickname: " + nicknameTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().getCurrentUser().delete(); // 이메일 등록 취소
                                    }
                                });
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError("This email is already registered.");
                            etEmail.requestFocus();
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

        private void saveUserToFirestore(String uid, String nickname) {
        // Firestore 초기화
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 사용자 데이터를 Map으로 생성
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("time", 0); // 초기 봉사시간 0으로 설정

        // Firestore에 UID를 키로 사용하여 저장
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "User profile created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // 회원가입 성공 후 종료 또는 화면 이동
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
