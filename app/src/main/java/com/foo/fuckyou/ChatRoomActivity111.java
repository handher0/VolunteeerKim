package com.foo.fuckyou;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatRoomActivity111 extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private EditText editTextMessage;
    private Button buttonSend;

    private DatabaseReference chatRoomRef;
    private String currentUserId = "test_user_1"; // 테스트용 사용자 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        String chatRoomId = getIntent().getStringExtra("chatRoomId");

        if (chatRoomId == null) {
            finish();
            return;
        }

        // Firebase Database 경로 설정
        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).child("messages");

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // 메시지 전송 버튼 클릭 이벤트
        buttonSend.setOnClickListener(v -> sendMessage());

        // Firebase에서 메시지 실시간 수신
        loadMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString();
        if (!messageText.isEmpty()) {
            String messageId = chatRoomRef.push().getKey();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", currentUserId);
            messageData.put("message", messageText);
            messageData.put("timestamp", System.currentTimeMillis());

            if (messageId != null) {
                chatRoomRef.child(messageId).setValue(messageData);
            }

            editTextMessage.setText("");
        }
    }

    private void loadMessages() {
        chatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String sender = messageSnapshot.child("sender").getValue(String.class);
                    String messageText = messageSnapshot.child("message").getValue(String.class);
                    long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                    Message message = new Message(messageText, sender, timestamp);
                    messages.add(message);
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // 오류 처리
            }
        });
    }
}
