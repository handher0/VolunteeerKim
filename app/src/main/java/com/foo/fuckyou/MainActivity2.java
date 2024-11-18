package com.foo.fuckyou;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity2 extends AppCompatActivity {
    private RecyclerView recyclerViewChatList;
    private Button buttonCreateChatRoom;
    private ArrayList<String> chatRoomList;
    private ChatListAdapter chatListAdapter;

    private DatabaseReference userChatsRef;
    private String currentUserId = "test_user_id"; // 테스트용 사용자 ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(this));

        buttonCreateChatRoom = findViewById(R.id.buttonCreateChatRoom);
        chatRoomList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatRoomList);
        recyclerViewChatList.setAdapter(chatListAdapter);

        userChatsRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId).child("chats");

        loadChatRooms();

        buttonCreateChatRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateChatRoomDialog();
            }
        });

        recyclerViewChatList.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerViewChatList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String chatRoomId = chatRoomList.get(position);
                Intent intent = new Intent(MainActivity2.this, ChatRoomActivity111.class);
                intent.putExtra("chatRoomId", chatRoomId);
                startActivity(intent);
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity2.this, "Failed to load chat rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCreateChatRoomDialog() {
        EditText editTextFriendId = new EditText(this);
        editTextFriendId.setHint("Enter Friend's ID");

        new AlertDialog.Builder(this)
                .setTitle("Create Chat Room")
                .setView(editTextFriendId)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String friendId = editTextFriendId.getText().toString().trim();
                        if (!friendId.isEmpty()) {
                            createChatRoom(friendId);
                        } else {
                            Toast.makeText(MainActivity2.this, "Friend's ID cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createChatRoom(String friendId) {
        String chatRoomId = ChatHelper.generateChatRoomId(currentUserId, friendId);
        userChatsRef.child(chatRoomId).setValue(true);
        FirebaseDatabase.getInstance().getReference("users").child(friendId).child("chats").child(chatRoomId).setValue(true);

        Intent intent = new Intent(this, ChatRoomActivity111.class);
        intent.putExtra("chatRoomId", chatRoomId);
        startActivity(intent);
    }
}
