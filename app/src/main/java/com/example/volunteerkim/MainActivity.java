package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // 로그를 위한 import
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.foo.fuckyou.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity"; // 로그 태그

    private RecyclerView recyclerViewChatList;
    private Button buttonCreateChatRoom;
    private ArrayList<String> chatRoomList;
    private ChatListAdapter chatListAdapter;

    // 테스트용 사용자 ID
    private String currentUserId = "test_user_1";
    private String friendId = "test_user_2";

    private DatabaseReference userChatsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_main);

        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(this));

        buttonCreateChatRoom = findViewById(R.id.buttonCreateChatRoom);
        chatRoomList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatRoomList);
        recyclerViewChatList.setAdapter(chatListAdapter);

        // Firebase 참조 초기화
        userChatsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("chats");
        Log.d(TAG, "Firebase Database Reference: " + userChatsRef.toString());

        // 기존 채팅방 불러오기
        loadChatRooms();

        // 채팅방 생성 버튼 클릭 이벤트
        buttonCreateChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChatRoom(friendId);  // friendId는 고정된 테스트용 값입니다.
            }
        });
    }

    private void loadChatRooms() {
        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    String chatRoomId = chatSnapshot.getKey();
                    chatRoomList.add(chatRoomId);
                }
                chatListAdapter.notifyDataSetChanged();
                Log.d(TAG, "Chat rooms loaded: " + chatRoomList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load chat rooms", error.toException());
            }
        });
    }

    private void createChatRoom(String friendId) {
        String chatRoomId = ChatHelper.generateChatRoomId(currentUserId, friendId);
        userChatsRef.child(chatRoomId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Chat room created for current user: " + chatRoomId);
                    } else {
                        Log.e(TAG, "Failed to create chat room for current user", task.getException());
                    }
                });

        FirebaseDatabase.getInstance().getReference("users").child(friendId).child("chats").child(chatRoomId).setValue(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Chat room created for friend: " + friendId);
                    } else {
                        Log.e(TAG, "Failed to create chat room for friend", task.getException());
                    }
                });

        Intent intent = new Intent(this, ChatRoomActivity.class); // ChatRoomActivity로 수정
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }
}
