package com.example.volunteerkim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.volunteerkim.databinding.FragmentCommunityOtherPostBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class CommunityFragment_other_post extends Fragment {
    private FragmentCommunityOtherPostBinding binding;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int MAX_IMAGES = 5;
    private String boardType;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityOtherPostBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            boardType = getArguments().getString("boardType");
        }
        // Free 게시판일 경우 모집 기간 입력 UI 숨기기
        if (boardType.equals("Free")) {
            binding.etTimeStart.setVisibility(View.GONE);
            binding.etTimeEnd.setVisibility(View.GONE);
            // 관련 레이블도 숨기기
            binding.goneForFree1.setVisibility(View.GONE);
            binding.goneForFree2.setVisibility(View.GONE);
        }

        setupButtons();
        return binding.getRoot();
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.etTimeStart.setOnClickListener(v -> showDatePickerDialog(binding.etTimeStart));
        binding.etTimeEnd.setOnClickListener(v -> showDatePickerDialog(binding.etTimeEnd));

        binding.btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imagePickerLauncher.launch(intent);
        });

        binding.imageCountText.setText("0/5");
        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSubmit.setOnClickListener(v -> {
            String title = binding.etTitle.getText().toString().trim();
            String content = binding.etContent.getText().toString().trim();

            // 제목과 내용이 비어있는지만 체크
            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            submitPost();
        });
    }

    private void submitPost() {
        showLoadingDialog();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            hideLoadingDialog();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nickname = documentSnapshot.getString("nickname");
                        if (nickname == null) nickname = "익명";

                        OtherPost post = new OtherPost();
                        post.setTitle(binding.etTitle.getText().toString());
                        post.setContent(binding.etContent.getText().toString());
                        post.setAuthor(nickname);
                        post.setTimestamp(new Timestamp(new Date()));

                        // Free 게시판이 아닐 때만 모집 기간 설정
                        if (!boardType.equals("Free")) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            try {
                                Date startDate = dateFormat.parse(binding.etTimeStart.getText().toString());
                                Date endDate = dateFormat.parse(binding.etTimeEnd.getText().toString());
                                post.setRecruitmentStart(startDate);
                                post.setRecruitmentEnd(endDate);
                            } catch (ParseException e) {
                                Log.e("DateParse", "Date parsing failed", e);
                                Toast.makeText(getContext(), "날짜 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                                hideLoadingDialog();
                                return;
                            }
                        }

                        if (selectedImages.isEmpty()) {
                            post.setHasImages(false);
                            post.setImageUrls(new ArrayList<>());
                            savePost(post);
                            return;
                        }

                        post.setHasImages(true);
                        post.setImageUrls(new ArrayList<>());
                        savePostWithImages(post);
                    }
                });
    }

    private void savePost(OtherPost post) {
        FirebaseFirestore.getInstance()
                .collection("Boards")
                .document(boardType)
                .collection("Posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    hideLoadingDialog();
                    Toast.makeText(getContext(), "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    hideLoadingDialog();
                });
    }

    private void savePostWithImages(OtherPost post) {
        FirebaseFirestore.getInstance()
                .collection("Boards")
                .document(boardType)
                .collection("Posts")
                .add(post)
                .addOnSuccessListener(documentReference -> {
                    String postId = documentReference.getId();
                    uploadImages(postId);
                });
    }

    private void uploadImages(String postId) {
        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : selectedImages) {
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child(boardType + "_images")
                    .child(postId)
                    .child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    uploadedUrls.add(uri.toString());
                                    if (uploadCount.incrementAndGet() == selectedImages.size()) {
                                        updatePostWithImageUrls(postId, uploadedUrls);
                                    }
                                });
                    });
        }
    }

    private void updatePostWithImageUrls(String postId, List<String> imageUrls) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrls", imageUrls);
        updates.put("hasImages", true);

        FirebaseFirestore.getInstance()
                .collection("Boards")
                .document(boardType)
                .collection("Posts")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    hideLoadingDialog();
                    Toast.makeText(getContext(), "게시글 등록 완료", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시글 등록 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    selectedImages.clear();

                    if (data.getClipData() != null) {
                        ClipData clipData = data.getClipData();
                        int count = Math.min(clipData.getItemCount(), MAX_IMAGES);
                        for (int i = 0; i < count; i++) {
                            Uri imageUri = clipData.getItemAt(i).getUri();
                            requireActivity().getContentResolver().takePersistableUriPermission(
                                    imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            selectedImages.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        requireActivity().getContentResolver().takePersistableUriPermission(
                                imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        selectedImages.add(imageUri);
                    }

                    binding.imageCountText.setText(selectedImages.size() + "/5");
                }
            }
    );

    private void showDatePickerDialog(EditText dateEditText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, day) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, day);
                    dateEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false);  // 뒤로가기 버튼으로 닫을 수 없게 설정
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
