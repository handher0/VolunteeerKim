package com.example.volunteerkim;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Community_CRUD {
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static String postId;

    public static void saveReviewPost(ReviewPost reviewPost, OnCompleteListener<Void> listener) {
        DocumentReference newPostRef = db.collection("Boards")
                .document("Review")
                .collection("Posts")
                .document();

        postId = newPostRef.getId();
        Map<Object, Object> post = new HashMap<>();

        post.put("place", reviewPost.getPlace());
        post.put("address", reviewPost.getAddress());
        post.put("author", reviewPost.getAuthor());
        post.put("category", reviewPost.getCategory());
        post.put("content", reviewPost.getContent());
        post.put("startTime", reviewPost.getStartTime());
        post.put("endTime", reviewPost.getEndTime());
        post.put("imageUrls", reviewPost.getImageUrls());
        post.put("hasImages", reviewPost.getHasImages());
        post.put("timestamp", FieldValue.serverTimestamp());
        post.put("rating", reviewPost.getRating());

        newPostRef.set(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PostDebug", "게시글 추가 성공: " + postId);
                    if (listener != null) {
                        listener.onComplete(Tasks.forResult(null));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("PostDebug", "게시글 추가 실패", e);
                    if (listener != null) {
                        listener.onComplete(Tasks.forException(e));
                    }
                });
    }

    public static String getPostId() {
        return postId;
    }
}



