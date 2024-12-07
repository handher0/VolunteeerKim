package com.example.volunteerkim;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.volunteerkim.databinding.FragmentCommunityBoardBinding;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommunityBoardFragment extends Fragment {
    private FragmentCommunityBoardBinding binding;
    private String boardType;
    private FirebaseFirestore db;
    private CommunityAdapter adapter;
    private List<OtherPost> postList = new ArrayList<>();

    public static CommunityBoardFragment newInstance(String boardType) {
        CommunityBoardFragment fragment = new CommunityBoardFragment();
        Bundle args = new Bundle();
        args.putString("boardType", boardType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            boardType = getArguments().getString("boardType");
        }
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCommunityBoardBinding.inflate(inflater, container, false);
        setupRecyclerView();
        loadPosts();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new CommunityAdapter(postList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    public void loadPosts() {
        db.collection("Boards")
                .document(boardType)
                .collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    postList.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            OtherPost post = doc.toObject(OtherPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}