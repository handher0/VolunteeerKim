package com.example.volunteerkim;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Community_CRUD {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void saveReviewPost(ReviewPost reviewPost, OnCompleteListener<Void> listener) {


        DocumentReference newPostRef = db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document();

        Map<Object, Object> post = new HashMap<>();

        post.put("place", reviewPost.getPlace());
        post.put("author", reviewPost.getAuthor());
        post.put("category", reviewPost.getCategory());
        post.put("content", reviewPost.getContent());
        post.put("startTime", reviewPost.getStartTime());
        post.put("endTime", reviewPost.getEndTime());
        post.put("rating", reviewPost.getRating());
        post.put("timestamp", FieldValue.serverTimestamp());

        newPostRef.set(post).addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "게시글 추가 성공: " + newPostRef.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "게시글 추가 실패", e);
                });
    }


}


