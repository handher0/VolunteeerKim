package com.example.volunteerkim;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.volunteerkim.databinding.ActivityCommunityCrudBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class CommunityCrudActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCommunityCrudBinding binding = ActivityCommunityCrudBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //Create
        binding.btCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference newPostRef = db.collection("Boards")
                        .document("Review")
                        .collection("Posts")
                        .document();

                Map<Object, Object> post = new HashMap<>();

                post.put("place", binding.etVplace.getText().toString());
                post.put("startTime", Integer.parseInt(binding.etStartTime.getText().toString()));
                post.put("endTime", Integer.parseInt(binding.etEndTime.getText().toString()));
                post.put("photoURL", binding.etPhoto.getText().toString());
                post.put("stars", Integer.parseInt(binding.etStar.getText().toString()));
                post.put("content", binding.etContent.getText().toString());
                post.put("info", binding.etInfo.getText().toString());
                post.put("postId", newPostRef.getId());
                post.put("timestamp", FieldValue.serverTimestamp());

                newPostRef.set(post).addOnSuccessListener(documentReference -> {
                            Log.d("Firestore", "게시글 추가 성공: " + newPostRef.getId());
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firestore", "게시글 추가 실패", e);
                        });
            }
        });

        //Read
        binding.btRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection("Boards").document("Review").collection("Posts")
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Firestore", document.getId() + " => " + document.getData());
                                }
                            } else {
                                Log.w("Firestore", "게시글 가져오기 실패", task.getException());
                            }
                        });
            }
        });

        //Update
        binding.btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference postRef = db.collection("Boards").document("Review")
                        .collection("Posts").document("Bu1pzXm18N7J9QvlpByi");

                postRef.update("place", "updated place1", "content", "updated content1")
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "게시글 수정 성공");
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firestore", "게시글 수정 실패", e);
                        });

            }
        });

        //Delete

        binding.btDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference postRef = db.collection("Boards").document("Review")
                        .collection("Posts").document("Bu1pzXm18N7J9QvlpByi");

                postRef.delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "게시글 삭제 성공");
                        })
                        .addOnFailureListener(e -> {
                            Log.w("Firestore", "게시글 삭제 실패", e);
                        });
            }
        });

    }
}