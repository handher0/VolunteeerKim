package com.example.volunteerkim;

import android.os.Bundle;
import android.text.TextUtils;
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

public class ChatFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String param1;
    private String param2;

    private EditText editTextUser1, editTextUser2;
    private Button buttonCreateChatRoom;

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_activity_main, container, false); // XML 연결
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // XML에서 위젯 참조
        editTextUser1 = view.findViewById(R.id.editTextUser1);
        editTextUser2 = view.findViewById(R.id.editTextUser2);
        buttonCreateChatRoom = view.findViewById(R.id.buttonCreateChatRoom);

        // 버튼 클릭 이벤트
        buttonCreateChatRoom.setOnClickListener(v -> {
            String user1 = editTextUser1.getText().toString().trim();
            String user2 = editTextUser2.getText().toString().trim();

            if (TextUtils.isEmpty(user1) || TextUtils.isEmpty(user2)) {
                Toast.makeText(getContext(), "Both User1 and User2 must be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            createChatRoom(user1, user2);
        });
    }

    private void createChatRoom(String user1, String user2) {
        // 채팅방 ID 생성
        String chatRoomId = ChatHelper.generateChatRoomId(user1, user2);

        // Firebase 업데이트
        DatabaseReference userChatRef = FirebaseDatabase.getInstance().getReference("users");
        userChatRef.child(user1).child("chats").child(chatRoomId).setValue(true);
        userChatRef.child(user2).child("chats").child(chatRoomId).setValue(true);

        // ChatRoomFragment로 이동
        ChatRoomFragment chatRoomFragment = ChatRoomFragment.newInstance(chatRoomId, user1);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatRoomFragment) // MainActivity에 정의된 container ID
                .addToBackStack(null)
                .commit();
    }
}
