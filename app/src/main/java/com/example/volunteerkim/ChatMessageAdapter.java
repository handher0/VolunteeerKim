package com.example.volunteerkim;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> messages; // List로 선언
    private String currentUserId; // Non-static field

    public ChatMessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId; // 초기화
    }

    @Override
    public int getItemViewType(int position) {
        // 메시지의 뷰 타입 결정 (기본 메시지)
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof MessageViewHolder) {
            // 메시지 ViewHolder 처리
            MessageViewHolder messageHolder = (MessageViewHolder) holder;

            // 초기화: 모든 말풍선 숨김
            messageHolder.leftMessageContainer.setVisibility(View.GONE);
            messageHolder.rightMessageContainer.setVisibility(View.GONE);

            // 현재 사용자와 메시지 송신자를 비교하여 말풍선 위치 설정
            if (message.getSenderId().equals(currentUserId)) {
                // 현재 사용자의 메시지 -> 오른쪽
                messageHolder.rightMessageContainer.setVisibility(View.VISIBLE);
                messageHolder.textViewRightMessage.setText(message.getText());
                String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
                messageHolder.textViewRightTime.setText(timeText);
            } else {
                // 상대방의 메시지 -> 왼쪽
                messageHolder.leftMessageContainer.setVisibility(View.VISIBLE);
                messageHolder.textViewLeftMessage.setText(message.getText());
                String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
                messageHolder.textViewLeftTime.setText(timeText);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder 클래스
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        View leftMessageContainer, rightMessageContainer;
        TextView textViewLeftMessage, textViewLeftTime;
        TextView textViewRightMessage, textViewRightTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            leftMessageContainer = itemView.findViewById(R.id.leftMessageContainer);
            rightMessageContainer = itemView.findViewById(R.id.rightMessageContainer);
            textViewLeftMessage = itemView.findViewById(R.id.textViewLeftMessage);
            textViewLeftTime = itemView.findViewById(R.id.textViewLeftTime);
            textViewRightMessage = itemView.findViewById(R.id.textViewRightMessage);
            textViewRightTime = itemView.findViewById(R.id.textViewRightTime);
        }
    }
}
