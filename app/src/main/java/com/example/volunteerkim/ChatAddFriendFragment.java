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
    private String user1id; // 현재 로그인한 사용자 닉네임

    public ChatAddFriendFragment() {
        // Required empty public constructor
    }

    public static ChatAddFriendFragment newInstance(String user1id) {
        ChatAddFriendFragment fragment = new ChatAddFriendFragment();
        Bundle args = new Bundle();
        args.putString("user1id", user1id);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_add_friend, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user1id = getArguments().getString("user1id"); // MainActivity에서 전달된 user1id 가져오기
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextFriendName = view.findViewById(R.id.editTextFriendName);
        buttonAddFriend = view.findViewById(R.id.buttonAddFriend);

        buttonAddFriend.setOnClickListener(v -> {
            String friendNickname = editTextFriendName.getText().toString().trim();
            Log.d("ChatAddFriendFragment", "Entered friend nickname: " + friendNickname);

            if (TextUtils.isEmpty(friendNickname)) {
                Toast.makeText(getContext(), "Please enter a valid friend nickname", Toast.LENGTH_SHORT).show();
                return;
            }

            // 채팅방 생성 호출
            createChatRoom(user1id, friendNickname);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_left,   // 새 화면 들어올 때
                            R.anim.slide_out_right, // 현재 화면 나갈 때
                            R.anim.slide_in_right,  // 뒤로가기 시 들어올 때
                            R.anim.slide_out_left   // 뒤로가기 시 나갈 때
                    )
                    .remove(this) // 현재 프래그먼트 제거
                    .commit();
        });

    }

    /**
     * 채팅방 생성 메서드
     */
    private void createChatRoom(String user1Nickname, String user2Nickname) {
        Log.d("ChatHelper", "Generating chat room ID with user1: " + user1Nickname + ", user2: " + user2Nickname);
        String chatRoomId = ChatHelper.generateChatRoomId(user1Nickname, user2Nickname);
        Log.d("ChatHelper", "Generated chat room ID: " + chatRoomId);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        // 사용자 채팅방 경로 설정
        database.child("users").child(user1Nickname).child("chats").child(chatRoomId).setValue(user2Nickname)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room added for user1Nickname: " + user1Nickname))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to add chat room for user1Nickname: " + user1Nickname, e));

        database.child("users").child(user2Nickname).child("chats").child(chatRoomId).setValue(user1Nickname)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room added for user2Nickname: " + user2Nickname))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to add chat room for user2Nickname: " + user2Nickname, e));

        // 채팅방 경로에 기본 데이터 추가
        HashMap<String, Object> chatRoomData = new HashMap<>();
        chatRoomData.put("createdAt", System.currentTimeMillis()); // 생성 시간 추가
        chatRoomData.put("lastMessage", ""); // 기본 메시지 값 추가

        // 멤버 정보 추가
        List<String> members = new ArrayList<>();
        members.add(user1Nickname);
        members.add(user2Nickname);
        chatRoomData.put("members", members);

        database.child("chatRooms").child(chatRoomId).setValue(chatRoomData)
                .addOnSuccessListener(aVoid -> Log.d("ChatFragment", "Chat room created with ID: " + chatRoomId))
                .addOnFailureListener(e -> Log.e("ChatFragment", "Failed to create chat room with ID: " + chatRoomId, e));

        // 사용자에게 알림
        Toast.makeText(getContext(), "Chat room created successfully!", Toast.LENGTH_SHORT).show();

        // 이전 화면으로 돌아가기
        getParentFragmentManager().popBackStack();
    }
}
