package com.example.volunteerkim;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.volunteerkim.databinding.FragmentCommunityReviewPostBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class CommunityFragment_review_post extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentCommunityReviewPostBinding binding;

    private String mParam1;
    private String mParam2;

    public CommunityFragment_review_post() {
    }

    public CommunityFragment_review_post newInstance(String param1, String param2) {
        CommunityFragment_review_post fragment = new CommunityFragment_review_post();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}