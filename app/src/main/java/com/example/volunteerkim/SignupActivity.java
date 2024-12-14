package com.example.volunteerkim;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_DRIVE_FILE = 2;

    private EditText etId, etPassword, etPasswordConfirm, etEmail;
    private CheckBox cbTerms1;
    private Button btnCheckId, btnNext, btnUploadPhoto;
    private ImageView ivProfilePhoto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private GoogleSignInClient googleSignInClient;

    private Uri selectedImageUri;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_signup);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference("profile_photos");

        // Google Sign-In 클라이언트 초기화
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.readonly"))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // UI 요소 초기화
        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        etPasswordConfirm = findViewById(R.id.et_password_confirm);
        etEmail = findViewById(R.id.et_email);
        cbTerms1 = findViewById(R.id.cb_terms_1);
        btnCheckId = findViewById(R.id.btn_check_id);
        btnNext = findViewById(R.id.btn_next);
        btnUploadPhoto = findViewById(R.id.btn_upload_photo);
        ivProfilePhoto = findViewById(R.id.iv_profile_photo);

        // ID 중복 확인 버튼
        btnCheckId.setOnClickListener(v -> checkDuplicateId());

        // 구글 드라이브에서 사진 업로드 버튼
        btnUploadPhoto.setOnClickListener(v -> signInToGoogleDrive());

        // 회원가입 버튼
        btnNext.setOnClickListener(v -> registerUser());
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

    private void checkDuplicateId() {
        String id = etId.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            etId.setError("ID is required.");
            return;
        }

        db.collection("users")
                .whereEqualTo("nickname", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            etId.setError("This ID is already taken.");
                            Toast.makeText(this, "This ID is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "ID is available.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to check ID: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String id = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

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
        if (!cbTerms1.isChecked()) {
            Toast.makeText(this, "Please agree to the terms.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        uid = mAuth.getCurrentUser().getUid();

                        if (selectedImageUri != null) {
                            uploadProfilePhoto(id, email);
                        } else {
                            saveUserToFirestore(id, email, null);
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etEmail.setError("This email is already registered.");
                            etEmail.requestFocus();
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void uploadProfilePhoto(String nickname, String email) {
        StorageReference fileRef = storageRef.child(uid + ".jpg");
        fileRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> saveUserToFirestore(nickname, email, uri.toString()))
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to upload photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserToFirestore(String nickname, String email, @Nullable String photoUrl) {
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        user.put("email", email);
        user.put("photoUrl", photoUrl); // 사진 URL
        user.put("time", 0); // 초기 봉사시간

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User profile created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
