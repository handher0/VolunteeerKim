package com.example.volunteerkim;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
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
import android.widget.EditText;
import android.widget.Toast;
import com.example.volunteerkim.databinding.FragmentCommunityReviewPostBinding;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
                "상담", "교육", "문화행사", "환경보호", "생활편의",
                "주거환경", "보건의료", "농어촌봉사", "시설봉사", "기타"
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

        binding.etTimeStart.setOnClickListener(v -> showTimePickerDialog(binding.etTimeStart));
        binding.etTimeEnd.setOnClickListener(v -> showTimePickerDialog(binding.etTimeEnd));


        binding.btnAddPhoto.setOnClickListener(v -> {
            // 사진 추가 로직 구현
        });

        binding.btnCancel.setOnClickListener(v -> requireActivity().onBackPressed());

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
                requireActivity().onBackPressed();
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
        ReviewPost post = new ReviewPost();
        post.setPlace(binding.etSearch.getText().toString());
        post.setAddress(binding.etAddress.getText().toString());
        post.setCategory(binding.spinnerCategory.getSelectedItem().toString());
        post.setContent(binding.etContent.getText().toString());
        post.setStartTime(binding.etTimeStart.getText().toString());
        post.setEndTime(binding.etTimeEnd.getText().toString());
        post.setRating(binding.ratingBar.getRating());

        Community_CRUD.saveReviewPost(post, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            } else {
                Toast.makeText(getContext(), "리뷰 등록에 실패했습니다.", Toast.LENGTH_SHORT).show();
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

}

