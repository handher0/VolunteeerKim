package com.example.volunteerkim;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
    private ChatMessageAdapter messageAdapter;
    private ArrayList<ChatMessage> messages;
    private EditText editTextMessage;
    private Button buttonSend;

    private DatabaseReference chatRoomRef;
    private String chatRoomId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_room);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        messages = new ArrayList<>();

        currentUserId = getIntent().getStringExtra("currentUserId");
        chatRoomId = getIntent().getStringExtra("chatRoomId");

        if (currentUserId == null || chatRoomId == null) {
            Toast.makeText(this, "Error: Missing user or chat room information.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatRoomRef = FirebaseDatabase.getInstance().getReference("chatRooms").child(chatRoomId).child("messages");

        messageAdapter = new ChatMessageAdapter(messages, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);

        buttonSend.setOnClickListener(v -> sendMessage());
        loadMessages();
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String messageId = chatRoomRef.push().getKey();
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("sender", currentUserId);
            messageData.put("message", messageText);
            messageData.put("timestamp", System.currentTimeMillis());

            if (messageId != null) {
                chatRoomRef.child(messageId).setValue(messageData)
                        .addOnSuccessListener(aVoid -> editTextMessage.setText(""))
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void loadMessages() {
        chatRoomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    String messageId = messageSnapshot.getKey();
                    String text = messageSnapshot.child("message").getValue(String.class);
                    String senderId = messageSnapshot.child("sender").getValue(String.class);
                    Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                    if (messageId != null && text != null && senderId != null && timestamp != null) {
                        messages.add(new ChatMessage(messageId, text, senderId, timestamp));
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messages.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatRoomActivity", "Failed to load messages.", error.toException());
            }
        });
    }
}
