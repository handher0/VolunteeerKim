package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.foo.fuckyou.R;

import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private ArrayList<String> chatRoomList;

    public ChatListAdapter(ArrayList<String> chatRoomList) {
        this.chatRoomList = chatRoomList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String chatRoomId = chatRoomList.get(position);
        holder.textViewChatRoomId.setText(chatRoomId);
    }

    @Override
    public int getItemCount() {
        return chatRoomList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewChatRoomId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewChatRoomId = itemView.findViewById(R.id.textViewChatRoomId);
        }
    }
}
