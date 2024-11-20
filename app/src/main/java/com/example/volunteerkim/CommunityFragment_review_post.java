package com.example.volunteerkim;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.volunteerkim.databinding.FragmentCommunityReviewPostBinding;

public class CommunityFragment_review_post extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private FragmentCommunityReviewPostBinding binding;

    private String mParam1;
    private String mParam2;

    public CommunityFragment_review_post() {
    }

    public static CommunityFragment_review_post newInstance(String param1, String param2) {
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

        return binding.getRoot();
    }

    private void submitPost() {
        String location = binding.etSearch.getText().toString();
        String name = binding.etName.getText().toString();
        String category = binding.etCategory.getText().toString();
        String address = binding.etAddress.getText().toString();
        String timeStart = binding.etTimeStart.getText().toString();
        String timeEnd = binding.etTimeEnd.getText().toString();
        float rating = binding.ratingBar.getRating();

        // Firebase에 데이터 저장 로직 구현
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}