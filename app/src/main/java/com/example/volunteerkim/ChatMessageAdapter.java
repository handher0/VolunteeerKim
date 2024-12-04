package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {
    private List<ChatMessage> messages;
    private String currentUserId;

    // Constructor
    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // 초기화: 모든 말풍선 숨김
        holder.leftMessageContainer.setVisibility(View.GONE);
        holder.rightMessageContainer.setVisibility(View.GONE);

        // 현재 사용자와 메시지 송신자를 비교하여 말풍선 위치 설정
        if (message.getSenderId().equals(currentUserId)) {
            // 현재 사용자의 메시지 -> 오른쪽
            holder.rightMessageContainer.setVisibility(View.VISIBLE);
            holder.textViewRightMessage.setText(message.getText());

            String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(message.getTimestamp()));
            holder.textViewRightTime.setText(timeText);
        } else {
            // 상대방의 메시지 -> 왼쪽
            holder.leftMessageContainer.setVisibility(View.VISIBLE);
            holder.textViewLeftMessage.setText(message.getText());

            String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(message.getTimestamp()));
            holder.textViewLeftTime.setText(timeText);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // 메시지 업데이트
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    // ViewHolder 클래스
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View leftMessageContainer, rightMessageContainer;
        TextView textViewLeftMessage, textViewLeftTime;
        TextView textViewRightMessage, textViewRightTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            // 왼쪽 말풍선
            leftMessageContainer = itemView.findViewById(R.id.leftMessageContainer);
            textViewLeftMessage = itemView.findViewById(R.id.textViewLeftMessage);
            textViewLeftTime = itemView.findViewById(R.id.textViewLeftTime);

            // 오른쪽 말풍선
            rightMessageContainer = itemView.findViewById(R.id.rightMessageContainer);
            textViewRightMessage = itemView.findViewById(R.id.textViewRightMessage);
            textViewRightTime = itemView.findViewById(R.id.textViewRightTime);
        }
    }
}
