package com.example.volunteerkim;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChatAddFriendFragment extends Fragment {

    private EditText editTextFriendName;
    private Button buttonAddFriend;

    public ChatAddFriendFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_add_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextFriendName = view.findViewById(R.id.editTextFriendName);
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend);

        // 현재 로그인한 사용자 ID (임시)
        String currentUserId = "1234"; // Replace with actual user ID fetching logic

        buttonAddFriend.setOnClickListener(v -> {
            String friendName = editTextFriendName.getText().toString().trim();
            if (TextUtils.isEmpty(friendName)) {
                Toast.makeText(getContext(), "Please enter a valid friend name", Toast.LENGTH_SHORT).show();
                return;
            }

            // 채팅방 생성 호출
            createChatRoom(currentUserId, friendName);

            // 이전 화면으로 돌아가기
            getParentFragmentManager().popBackStack();
        });
    }

    private void createChatRoom(String user1id, String user2id) {
        String chatRoomId = ChatHelper.generateChatRoomId(user1id, user2id);
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // 사용자 채팅방 경로 설정
        database.child("users").child(user1id).child("chats").child(chatRoomId).setValue(user2id)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room added for user1id: " + user1id))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to add chat room for user1id: " + user1id, e));

        database.child("users").child(user2id).child("chats").child(chatRoomId).setValue(user1id)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room added for user2id: " + user2id))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to add chat room for user2id: " + user2id, e));

        // 채팅방 경로에 기본 데이터 추가
        HashMap<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("createdAt", System.currentTimeMillis()); // 생성 시간 추가
        chatRoomData.put("lastMessage", ""); // 기본 메시지 값 추가

// 배열 대신 List로 변환
        List<String> members = new ArrayList<>();
        members.add(user1id);
        members.add(user2id);
        chatRoomData.put("members", members); // 멤버 정보 추가

        database.child("chatRooms").child(chatRoomId).setValue(chatRoomData)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room created with ID: " + chatRoomId))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to create chat room with ID: " + chatRoomId, e));

    }
}
