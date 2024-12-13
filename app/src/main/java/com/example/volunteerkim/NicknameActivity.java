package com.example.volunteerkim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class NicknameActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_DRIVE_FILE = 2;

    private EditText etNickname;
    private Button btnSave, btnUploadPhoto;
    private ImageView ivProfilePhoto;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private GoogleSignInClient googleSignInClient;

    private String uid, email, displayName;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname);

        // Firestore와 Storage 초기화
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_photos");

        // Google Sign-In 클라이언트 초기화
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.readonly"))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // Intent로 전달된 데이터 가져오기
        uid = getIntent().getStringExtra("uid");
        email = getIntent().getStringExtra("email");

        // 뷰 초기화
        etNickname = findViewById(R.id.et_nickname);
        btnSave = findViewById(R.id.btn_save);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        // 사진 업로드 버튼 클릭 이벤트 (Google Drive에서 파일 선택)
        btnUploadPhoto.setOnClickListener(v -> signInToGoogleDrive());

        // 닉네임 저장 버튼 클릭 이벤트
        btnSave.setOnClickListener(v -> saveNickname());
    }

    private void signInToGoogleDrive() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == PICK_DRIVE_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            ivProfilePhoto.setImageURI(selectedImageUri); // 이미지 미리보기 설정
            Toast.makeText(this, "File selected from Google Drive!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                openDrivePicker();
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openDrivePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_DRIVE_FILE);
    }

    private void saveNickname() {
        String nickname = etNickname.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)) {
            etNickname.setError("Nickname is required.");
            etNickname.requestFocus();
            return;
        }

        if (selectedImageUri == null) {
            // 사진이 선택되지 않은 경우, 기본 로직으로 저장
            saveToFirestore(nickname, null);
        } else {
            // 선택된 Google Drive 이미지 Firebase Storage에 업로드
            StorageReference fileRef = storageRef.child(uid + ".jpg");
            fileRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        saveToFirestore(nickname, photoUrl); // Firestore에 데이터 저장
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to upload photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void saveToFirestore(String nickname, @Nullable String photoUrl) {
        // Firestore에 사용자 데이터 저장
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("email", email);
        user.put("displayName", displayName);
        user.put("photoUrl", photoUrl); // 사진 URL (null일 수 있음)
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
