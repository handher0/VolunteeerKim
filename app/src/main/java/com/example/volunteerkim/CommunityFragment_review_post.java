package com.example.volunteerkim;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

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
import android.widget.Toast;

import com.example.volunteerkim.databinding.FragmentCommunityReviewPostBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CommunityFragment_review_post extends Fragment {

    private FragmentCommunityReviewPostBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewPostBinding.inflate(inflater, container, false);

        // 카테고리 목록 설정
        String[] categories = new String[]{
                "상담",
                "교육",
                "문화행사",
                "환경보호",
                "생활편의",
                "주거환경",
                "보건의료",
                "농어촌봉사",
                "시설봉사",
                "기타"};

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
                // 선택된 카테고리 처리
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 아무것도 선택되지 않았을 때 처리
            }
        });

        // 뒤로가기 버튼
        binding.btnBack.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // 별점 설정
        binding.ratingBar.setNumStars(5);
        binding.ratingBar.setStepSize(0.5f);

        // 인증사진 추가 버튼
        binding.btnAddPhoto.setOnClickListener(v -> {
            // 사진 추가 로직 구현
        });

        // 취소, 등록 버튼
        binding.btnCancel.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        binding.btnSubmit.setOnClickListener(v -> {
            submitPost();
        });


        binding.btnSubmit.setOnClickListener(v -> {
            submitPost();
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = binding.etSearch.getText().toString();
                if (!query.isEmpty()) {
                    searchLocation(query);
                    // 키보드 숨기기
                    InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }


    private void submitPost() {
        ReviewPost post = new ReviewPost();
        post.setPlace(binding.etSearch.getText().toString());
        post.setCategory(binding.spinnerCategory.toString());
        post.setContent(binding.etContent.getText().toString());
        post.setStartTime(Integer.parseInt(binding.etTimeStart.getText().toString()));
        post.setEndTime(Integer.parseInt(binding.etTimeEnd.getText().toString()));
        post.setRating(binding.ratingBar.getRating());

        Community_CRUD.saveReviewPost(post, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                } else {
                    Toast.makeText(getContext(), "리뷰 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchLocation(String query) {
        // Naver Maps Geocoding API 호출
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NaverMapAPI api = retrofit.create(NaverMapAPI.class);
        Call<SearchResult> call = api.searchLocation(query);

        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SearchResult result = response.body();
                    if (!result.SearchAddress.isEmpty()) {
                        showAddressSelectionDialog(result.SearchAddress);
                    }
                    else {
                        Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "검색에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("API Error", "Error Code: " + response.code());
                    Log.e("API Error", "Error Body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddressSelectionDialog(List<SearchAddress> SearchAddress) {
        String[] items = new String[SearchAddress.size()];
        for (int i = 0; i < SearchAddress.size(); i++) {
            String address = "";
            if (SearchAddress.get(i).roadAddress != null && !SearchAddress.get(i).roadAddress.isEmpty()) {
                address = SearchAddress.get(i).roadAddress;
            } else if (SearchAddress.get(i).jibunAddress != null && !SearchAddress.get(i).jibunAddress.isEmpty()) {
                address = SearchAddress.get(i).jibunAddress;
            }
            items[i] = address;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("주소 선택")
                .setItems(items, (dialog, which) -> {
                    binding.etAddress.setText(items[which]);
                })
                .show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

