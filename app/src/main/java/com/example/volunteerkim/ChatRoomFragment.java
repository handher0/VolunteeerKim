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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomFragment extends Fragment {
    private static final String ARG_CHAT_ROOM_ID = "chatRoomId";
    private static final String ARG_CURRENT_USER_ID = "currentUserId";

    private String chatRoomId;
    private String currentUserId;

    private RecyclerView recyclerViewMessages;
    private ChatMessageAdapter messageAdapter;
    private List<ChatMessage> messages;

    private EditText editTextMessage;
    private Button buttonSend;

    private DatabaseReference chatRoomRef;

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    public static ChatRoomFragment newInstance(String chatRoomId, String currentUserId) {
        ChatRoomFragment fragment = new ChatRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CHAT_ROOM_ID, chatRoomId);
        args.putString(ARG_CURRENT_USER_ID, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            chatRoomId = getArguments().getString(ARG_CHAT_ROOM_ID);
            currentUserId = getArguments().getString(ARG_CURRENT_USER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));

        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        messages = new ArrayList<>();
        messageAdapter = new ChatMessageAdapter(messages, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);

        // 초기화 확인 및 예외 처리
        if (chatRoomId == null || currentUserId == null) {
            Toast.makeText(getContext(), "Error: Missing user or chat room information.", Toast.LENGTH_SHORT).show();
            Log.e("ChatRoomFragment", "Missing chatRoomId or currentUserId");
            return;
        }

        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).child("messages");

        buttonSend.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(getContext(), "Message cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String messageId = chatRoomRef.push().getKey();
        if (messageId != null) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("messageId", messageId);
            messageData.put("senderId", currentUserId);
            messageData.put("text", messageText);
            messageData.put("timestamp", System.currentTimeMillis());

            chatRoomRef.child(messageId).setValue(messageData)
                    .addOnSuccessListener(aVoid -> {
                        editTextMessage.setText("");
                        Log.d("ChatRoomFragment", "Message sent successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ChatRoomFragment", "Failed to send message.", e);
                        Toast.makeText(getContext(), "Failed to send message.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadMessages() {
        chatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                Log.d("ChatRoomFragment", "Loading messages for chatRoomId: " + chatRoomId);

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    try {
                        String messageId = messageSnapshot.getKey();
                        String text = messageSnapshot.child("text").getValue(String.class);
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);
                        Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                        if (messageId != null && text != null && senderId != null && timestamp != null) {
                            messages.add(new ChatMessage(messageId, text, senderId, timestamp));
                            Log.d("ChatRoomFragment", "Parsed message: " + text + ", senderId: " + senderId);
                        } else {
                            Log.w("ChatRoomFragment", "Incomplete message data at " + messageSnapshot.getKey());
                        }
                    } catch (Exception e) {
                        Log.e("ChatRoomFragment", "Error parsing message data", e);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(Math.max(messages.size() - 1, 0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatRoomFragment", "Failed to load messages.", error.toException());
            }
        });
    }
}
