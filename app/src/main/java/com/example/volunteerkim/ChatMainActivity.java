package com.example.volunteerkim;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatMainActivity extends AppCompatActivity {
    private EditText editTextUser1, editTextUser2;
    private Button buttonCreateChatRoom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity_main); // 올바른 XML 파일 연결

        // XML에서 위젯 참조
        editTextUser1 = findViewById(R.id.editTextUser1);
        editTextUser2 = findViewById(R.id.editTextUser2);
        buttonCreateChatRoom = findViewById(R.id.buttonCreateChatRoom);

        // 버튼 클릭 이벤트
        buttonCreateChatRoom.setOnClickListener(v -> {
            String user1 = editTextUser1.getText().toString().trim();
            String user2 = editTextUser2.getText().toString().trim();

            if (user1.isEmpty() || user2.isEmpty()) {
                Toast.makeText(this, "Both User1 and User2 must be filled!", Toast.LENGTH_SHORT).show();
                return;
            }

            createChatRoom(user1, user2);
        });
    }

    private void createChatRoom(String user1, String user2) {
        // Generate chat room ID
        String chatRoomId = ChatHelper.generateChatRoomId(user1, user2);

        // Update Firebase
        DatabaseReference userChatRef = FirebaseDatabase.getInstance().getReference("users");
        userChatRef.child(user1).child("chats").child(chatRoomId).setValue(true);
        userChatRef.child(user2).child("chats").child(chatRoomId).setValue(true);

        // Start ChatRoomActivity
        Intent intent = new Intent(this, ChatRoomActivity.class);
        intent.putExtra("chatRoomId", chatRoomId);
        intent.putExtra("currentUserId", user1); // Pass User1 as the current user
        startActivity(intent);
    }
}
