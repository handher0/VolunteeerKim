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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etId, etPassword, etPasswordConfirm, etEmail;
    private CheckBox cbTerms1;
    private Button btnCheckId, btnNext, btnUploadPhoto;
    private ImageView ivProfilePhoto;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

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

        // 로컬 파일(갤러리 등)에서 사진 업로드 버튼
        btnUploadPhoto.setOnClickListener(v -> selectPhotoFromDevice());

        // 회원가입 버튼
        btnNext.setOnClickListener(v -> registerUser());
    }

    private void selectPhotoFromDevice() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        photoPickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> photoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivProfilePhoto.setImageURI(selectedImageUri); // 이미지 미리보기 설정
                    Toast.makeText(this, "사진이 선택되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void checkDuplicateId() {
        String id = etId.getText().toString().trim();
        if (TextUtils.isEmpty(id)) {
            etId.setError("ID를 입력해주세요.");
            return;
        }

        db.collection("users")
                .whereEqualTo("nickname", id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            etId.setError("이미 존재하는 ID입니다.");
                            Toast.makeText(this, "이미 존재하는 ID입니다. 다른 ID를 선택해주세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "사용 가능한 ID입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "ID 확인 중 오류가 발생했습니다: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser() {
        String id = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(id)) {
            etId.setError("ID를 입력해주세요.");
            etId.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("이메일을 입력해주세요.");
            etEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("올바른 이메일을 입력해주세요.");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("비밀번호를 입력해주세요.");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("비밀번호는 최소 6자 이상이어야 합니다.");
            etPassword.requestFocus();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("비밀번호가 일치하지 않습니다.");
            etPasswordConfirm.requestFocus();
            return;
        }
        if (!cbTerms1.isChecked()) {
            Toast.makeText(this, "약관에 동의해주세요.", Toast.LENGTH_SHORT).show();
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
                            etEmail.setError("이미 등록된 이메일입니다.");
                            etEmail.requestFocus();
                        } else {
                            Toast.makeText(SignupActivity.this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(this, "사진 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "사진 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "사용자 정보 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
