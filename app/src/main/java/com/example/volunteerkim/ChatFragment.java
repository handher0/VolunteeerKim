package com.example.volunteerkim;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private String user1id = "1234"; // 현재 로그인한 사용자 ID

    private RecyclerView recyclerViewChatList;
    private ChatListAdapter chatListAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_activity_main, container, false); // XML 연결
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonAddFriends = view.findViewById(R.id.buttonAddFriends);
        if (buttonAddFriends != null) {
            buttonAddFriends.setOnClickListener(v -> {
                ChatAddFriendFragment addFriendFragment = new ChatAddFriendFragment();
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, addFriendFragment) // fragment_container는 ChatFragment가 있는 컨테이너 ID
                        .addToBackStack(null) // 뒤로 가기 지원
                        .commit();
            });
        }

        // RecyclerView 설정
        recyclerViewChatList = view.findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatListAdapter = new ChatListAdapter(new ArrayList<>(), user1id); // Adapter 생성
        recyclerViewChatList.setAdapter(chatListAdapter);

        chatListAdapter.setOnItemClickListener(chatRoomId -> {
            openChatRoom(chatRoomId);
        });

        loadChatRooms(); // Firebase에서 채팅방 데이터 로드
    }

    /**
     * 채팅방을 열고 Chat RoomFragment로 이동
     */
    private void openChatRoom(String user2id) {
        String chatRoomId = ChatHelper.generateChatRoomId(user1id, user2id);
        Log.d(TAG, "Opening ChatRoomFragment with ID: " + chatRoomId + " for user: " + user1id);
        ChatRoomFragment chatRoomFragment = ChatRoomFragment.newInstance(chatRoomId, user1id);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatRoomFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Firebase에서 채팅방 목록을 가져오고 RecyclerView를 업데이트
     */
    private void loadChatRooms() {
        DatabaseReference userChatRef = FirebaseDatabase.getInstance().getReference("users").child(user1id).child("chats");
        Log.d(TAG, "Loading chat rooms for user1id: " + user1id);

        userChatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chatPartners = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                        String chatRoomId = chatSnapshot.getKey(); // 채팅방 ID 가져오기
                        if (chatRoomId != null) {
                            String[] ids = chatRoomId.split("_");
                            String user2id = ids[0].equals(user1id) ? ids[1] : ids[0]; // 상대방 ID 추출
                            chatPartners.add(user2id);
                        }
                    }
                    Log.d(TAG, "Loaded chat partners: " + chatPartners);
                } else {
                    Log.e(TAG, "No chat rooms exist for user1id: " + user1id);
                }
                chatListAdapter.updateData(chatPartners); // RecyclerView 업데이트
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load chat rooms", error.toException());
            }
        });
    }
}
