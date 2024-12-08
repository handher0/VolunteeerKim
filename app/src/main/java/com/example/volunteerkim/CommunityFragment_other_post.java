package com.example.volunteerkim;

import android.app.Activity;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityOtherPostBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            boardType = getArguments().getString("boardType");
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
            if (isEmpty(binding.etTitle.getText().toString()) ||
                    isEmpty(binding.etContent.getText().toString()) ||
                    isEmpty(binding.etTimeStart.getText().toString()) ||
                    isEmpty(binding.etTimeEnd.getText().toString())) {
                Toast.makeText(getContext(), "모든 입력란에 내용을 넣어주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            submitPost();
        });
    }

    private void submitPost() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
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
                        // Convert String dates to Date objects
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        try {
                            Date startDate = dateFormat.parse(binding.etTimeStart.getText().toString());
                            Date endDate = dateFormat.parse(binding.etTimeEnd.getText().toString());
                            post.setRecruitmentStart(startDate);
                            post.setRecruitmentEnd(endDate);
                        } catch (ParseException e) {
                            Log.e("DateParse", "Date parsing failed", e);
                            Toast.makeText(getContext(), "날짜 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        post.setTimestamp(new Timestamp(new Date()));

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
                    Toast.makeText(getContext(), "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "게시글 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
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
}