package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private List<String> chatRoomList;
    private OnItemClickListener listener;
    private String currentUserId; // 현재 사용자 ID 저장

    // 생성자: 채팅방 리스트와 현재 사용자 ID를 받음
    public ChatListAdapter(List<String> chatRoomList, String currentUserId) {
        this.chatRoomList = chatRoomList;
        this.currentUserId = currentUserId;
    }

    // 클릭 리스너 인터페이스 정의
    public interface OnItemClickListener {
        void onItemClick(String chatRoomId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String chatRoomName = chatRoomList.get(position); // 상대방 ID
        holder.textViewChatRoomId.setText(chatRoomName);

        // 클릭 이벤트 처리
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(chatRoomName); // 클릭 시 리스너 호출
            }
        });
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

    // RecyclerView 데이터 갱신 메서드
    public void updateData(List<String> newChatRooms) {
        this.chatRoomList.clear();
        this.chatRoomList.addAll(newChatRooms);
        notifyDataSetChanged();
    }
}
