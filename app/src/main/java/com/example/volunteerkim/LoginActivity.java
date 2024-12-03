package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // 로그 태그
    private static final int RC_SIGN_IN = 9001; // Google Sign-In 요청 코드

    private EditText etEmail, etPassword; // 이메일과 비밀번호 필드
    private FirebaseAuth mAuth; // Firebase Authentication 인스턴스
    private GoogleSignInClient mGoogleSignInClient; // Google Sign-In 클라이언트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        mAuth = FirebaseAuth.getInstance(); // FirebaseAuth 초기화

        etEmail = findViewById(R.id.et_email); // 이메일 입력 필드
        etPassword = findViewById(R.id.et_password); // 비밀번호 입력 필드

        // Google Sign-In 옵션 구성
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // google-services.json에서 가져옴
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso); // Google Sign-In 클라이언트 초기화

        // 로그인 버튼 클릭 리스너
        findViewById(R.id.btn_login).setOnClickListener(v -> loginUser());

        // 회원가입 버튼 클릭 리스너
        findViewById(R.id.btn_sign_in).setOnClickListener(v -> {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
        });

        // Google 로그인 버튼 클릭 리스너
        findViewById(R.id.btn_google_login).setOnClickListener(v -> signInWithGoogle());
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

    private void signInWithGoogle() {
        // Google 로그아웃 및 세션 초기화
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Log.d(TAG, "Google sign-out successful.");
            // 계정 선택 화면 표시
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google Sign-In 결과 처리
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign-In 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    Log.d(TAG, "Google sign-in successful: " + account.getEmail());
                    firebaseAuthWithGoogle(account.getIdToken());
                }
            } catch (ApiException e) {
                Log.e(TAG, "Google sign-in failed. Status code: " + e.getStatusCode());
                Toast.makeText(this, "Google sign-in failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Google 로그인 성공
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Firebase auth successful for user: " + user.getEmail());
                            // 닉네임 입력 화면으로 이동
                            Intent intent = new Intent(this, NicknameActivity.class);
                            intent.putExtra("uid", user.getUid()); // UID 전달
                            intent.putExtra("email", user.getEmail()); // 이메일 전달
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        // Google 로그인 실패
                        Log.e(TAG, "Firebase auth failed: " + task.getException());
                        Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
