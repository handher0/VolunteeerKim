package com.example.volunteerkim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import com.example.volunteerkim.databinding.FragmentCommunityReviewPostBinding;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunityFragment_review_post extends Fragment {
    private FragmentCommunityReviewPostBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1;
    private List<Uri> selectedImages = new ArrayList<>();
    private static final int MAX_IMAGES = 5;
    private AlertDialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewPostBinding.inflate(inflater, container, false);

        // 카테고리 목록 설정
        String[] categories = new String[]{
                "교육지원(학습 지도, 진로 상담, 디지털 교육 등)",
                "환경보호(환경 정화, 보호 활동 등)",
                "돌봄서비스(아동 돌봄, 요양원 돌봄, 장애인 보조 등)",
                "문화행사(예체능 강의, 문화 체험 지원 등)",
                "재난구호(재난 지원, 긴급 구호 활동 등)",
                "보건의료(병원 도우미, 의약품 정리 등, 헌혈 제외)",
                "생활편의(취약 계층 지원, 주거 환경 개선 등)",
                "농어촌봉사(농어촌 지역 지원 봉사 등)",
                "시설봉사(고아원 방문, 동물 보호 등)",
                "헌혈(헌혈 캠페인)",
        };


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerCategory.setAdapter(adapter);

        // 선택 이벤트 처리
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        setupButtons();
        return binding.getRoot();
    }

    private void setupButtons() {
        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.ratingBar.setNumStars(5);
        binding.ratingBar.setStepSize(0.5f);

        binding.etVolunteerDate.setOnClickListener(v -> showDatePickerDialog());

        binding.etTimeStart.setOnClickListener(v -> showTimePickerDialog(binding.etTimeStart));
        binding.etTimeEnd.setOnClickListener(v -> showTimePickerDialog(binding.etTimeEnd));

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

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 입력값 검증
                if (isEmpty(binding.etSearch.getText().toString()) ||
                        isEmpty(binding.etAddress.getText().toString()) ||
                        isEmpty(binding.etContent.getText().toString()) ||
                        isEmpty(binding.etTimeStart.getText().toString()) ||
                        isEmpty(binding.etTimeEnd.getText().toString()) ||
                        isEmpty(binding.etVolunteerDate.getText().toString()) ||
                        binding.ratingBar.getRating() == 0) {

                    Toast.makeText(getContext(), "모든 입력란에 내용을 넣어주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                submitPost();
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString();
                if (!query.isEmpty()) {
                    searchPlace(query);
                    hideKeyboard(v);
                }
                return true;
            }
            return false;
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void submitPost() {
        if (binding == null) return;

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

                        // 먼저 게시물 생성
                        ReviewPost post = new ReviewPost();
                        post.setPlace(binding.etSearch.getText().toString());
                        post.setAddress(binding.etAddress.getText().toString());
                        post.setAuthor(nickname);
                        post.setCategory(binding.spinnerCategory.getSelectedItem().toString());
                        post.setContent(binding.etContent.getText().toString());
                        post.setVolunteerDate(binding.etVolunteerDate.getText().toString());
                        post.setStartTime(binding.etTimeStart.getText().toString());
                        post.setEndTime(binding.etTimeEnd.getText().toString());
                        post.setRating(binding.ratingBar.getRating());
                        post.setTimestamp(new Timestamp(new Date()));

                        Log.d("PostDebug", "selectedImages 크기: " + selectedImages.size());

                        // `VolunteerCalendar` 업데이트 추가
                        DataMigration.updateVolunteerCalendar(FirebaseFirestore.getInstance(), nickname, post);
                        UserDataManager.updateUserSummary(FirebaseFirestore.getInstance(), nickname, post);
                        // 이미지 처리 로직은 기존대로
                        if (selectedImages.isEmpty()) {
                            Log.d("PostDebug", "이미지 없는 게시물 저장");
                            post.setHasImages(false);
                            post.setImageUrls(new ArrayList<>());
                            savePost(post);
                            return;
                        }

                        post.setHasImages(true);
                        post.setImageUrls(new ArrayList<>());
                        Log.d("PostDebug", "이미지 있는 게시물 저장 시작");

                        // 게시물 먼저 저장
                        Community_CRUD.saveReviewPost(post, task -> {
                            if (task.isSuccessful()) {
                                String postId = Community_CRUD.getPostId();
                                Log.d("PostDebug", "게시물 ID: " + postId);
                                if (postId != null && !postId.isEmpty()) {
                                    uploadImages(postId);
                                } else {
                                    Log.e("PostDebug", "게시물 ID가 null 또는 비어있음");
                                    hideLoadingDialog();
                                }
                            } else {
                                Log.e("PostDebug", "게시물 저장 실패", task.getException());
                                hideLoadingDialog();
                            }
                        });
                    }
                });
    }


    private void searchPlace(String query) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://openapi.naver.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NaverSearchAPI api = retrofit.create(NaverSearchAPI.class);
        Call<SearchResult> call = api.searchPlace(query,5);

        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SearchResult result = response.body();
                    if (!result.items.isEmpty()) {
                        showPlaceSelectionDialog(result.items);
                    } else {
                        Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Error Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showPlaceSelectionDialog(List<PlaceItem> places) {
        String[] items = new String[places.size()];
        for (int i = 0; i < places.size(); i++) {
            items[i] = removeHtmlTags(places.get(i).title) + "\n"
                    + removeHtmlTags(places.get(i).address
                    + "\n─────────────────");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("장소 선택")
                .setItems(items, (dialog, which) -> {
                    binding.etSearch.setText(removeHtmlTags(places.get(which).title));
                    binding.etAddress.setText(removeHtmlTags(places.get(which).address));
                })
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String removeHtmlTags(String html) {
        return html.replaceAll("<[^>]*>", "");
    }

    private void showTimePickerDialog(EditText timeEditText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute1) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                    timeEditText.setText(time);
                },
                hour,
                minute,
                true  // 24시간 형식 사용
        );
        timePickerDialog.show();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d",
                            selectedYear, selectedMonth + 1, selectedDay);
                    binding.etVolunteerDate.setText(date);
                },
                year, month, day
        );
        datePickerDialog.show();
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
                            // URI 영구 권한 요청
                            requireActivity().getContentResolver().takePersistableUriPermission(
                                    imageUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                            selectedImages.add(imageUri);
                        }
                    } else if (data.getData() != null) {
                        Uri imageUri = data.getData();
                        // URI 영구 권한 요청
                        requireActivity().getContentResolver().takePersistableUriPermission(
                                imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                        selectedImages.add(imageUri);
                    }
                    if (binding == null) return;
                    binding.imageCountText.setText(selectedImages.size() + "/5");
                }
            }
    );

    private void savePost(ReviewPost post) {
        Community_CRUD.saveReviewPost(post, task -> {
            if (isAdded() && getContext() != null) {  // Fragment 상태 확인
                if (task.isSuccessful()) {
                    hideLoadingDialog();
                    Toast.makeText(getContext(), "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();  // 토스트 메시지 표시 후 화면 종료
                } else {
                    Toast.makeText(getContext(), "리뷰 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void uploadImages(String postId) {
        if (selectedImages.isEmpty()) return;

        List<String> uploadedUrls = new ArrayList<>();
        AtomicInteger uploadCount = new AtomicInteger(0);

        for (Uri imageUri : selectedImages) {
            String fileName = UUID.randomUUID().toString();
            StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                    .child("review_images")
                    .child(postId)
                    .child(fileName);

            imageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // 파일 업로드 성공 후 바로 다운로드 URL 요청
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    uploadedUrls.add(uri.toString());
                                    Log.d("Storage", "이미지 URL 획득: " + uri.toString());

                                    // 모든 이미지가 업로드되고 URL을 받아왔을 때만 업데이트
                                    if (uploadCount.incrementAndGet() == selectedImages.size()) {
                                        Log.d("Storage", "모든 URL 수집 완료, 개수: " + uploadedUrls.size());
                                        updatePostWithImageUrls(postId, uploadedUrls);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Storage", "이미지 업로드 실패", e);
                    });
        }
    }

    private void updatePostWithImageUrls(String postId, List<String> imageUrls) {
        // 여러 필드를 동시에 업데이트
        Map<String, Object> updates = new HashMap<>();
        updates.put("imageUrls", imageUrls);
        updates.put("hasImages", true);

        FirebaseFirestore.getInstance()
                .collection("Boards")
                .document("Review")
                .collection("Posts")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    hideLoadingDialog();
                    if (getContext() != null) {  // Context null 체크 추가
                        Toast.makeText(getContext(), "리뷰 등록 완료", Toast.LENGTH_SHORT).show();
                    }
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(getContext(), "리뷰 등록 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 문자열이 비어있는지 확인하는 헬퍼 메서드
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
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

