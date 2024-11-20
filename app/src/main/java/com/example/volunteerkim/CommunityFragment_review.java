package com.example.volunteerkim;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.volunteerkim.databinding.FragmentCommunityReviewBinding;
import com.example.volunteerkim.databinding.ItemPostBinding;

import java.util.ArrayList;
import java.util.List;


public class CommunityFragment_review extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CommunityFragment_review() {
        // Required empty public constructor
    }

    public static CommunityFragment_review newInstance(String param1, String param2) {
        CommunityFragment_review fragment = new CommunityFragment_review();
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

     private FragmentCommunityReviewBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityReviewBinding.inflate(inflater, container, false);

        List<PostItem> list = new ArrayList<>();
        list.add(new PostItem("청운 지킴 생활관", "서울특별시 동작구 상도제3동 290-1", 5.0f));
        list.add(new PostItem("숭실대학교 정보과학관", "서울특별시 동작구 상도제3동 290-1", 1.0f));
        list.add(new PostItem("다솜 아동복지센터", "서울특별시 동작구 상도제3동 290-1", 5.0f));
        list.add(new PostItem("무슨 노인 요양원", "서울특별시 동작구 상도제3동 290-1", 5.0f));
        list.add(new PostItem("동작구 아동다문화센터", "서울특별시 동작구 상도제3동 290-1", 5.0f));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(new MyAdapter(list));

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.btnAdd.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new CommunityFragment_review_post())
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class PostItem {
        String name;
        String address;
        float rating;

        PostItem(String name, String address, float rating) {
            this.name = name;
            this.address = address;
            this.rating = rating;
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private ItemPostBinding binding;

        private MyViewHolder(ItemPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<PostItem> list;

        private MyAdapter(List<PostItem> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemPostBinding binding = ItemPostBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            PostItem item = list.get(position);
            holder.binding.tvName.setText(item.name);
            holder.binding.tvAddress.setText(item.address);
            holder.binding.ratingBar.setRating(item.rating);
            holder.binding.btnDetail.setText("상세보기/리뷰보기");
            holder.binding.btnDetail.setOnClickListener(v -> {
                // 상세보기/리뷰보기 버튼 클릭 처리
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}
