package com.foo.fuckyou;

import android.os.Bundle;
import android.util.Log;
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

public class ChatRoomActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private ArrayList<Message> messages;
    private EditText editTextMessage;
    private Button buttonSend;

    private DatabaseReference chatRoomRef;
    private String chatRoomId;
    private String currentUserId = "test_user_id"; // 테스트용 사용자 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        chatRoomId = getIntent().getStringExtra("chatRoomId");
        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).child("messages");

        Log.d("DatabaseReference", "Path to chat room messages: " + chatRoomRef.toString());

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(messages, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        buttonSend.setOnClickListener(v -> sendMessage());
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
            }
        });
    }
}
