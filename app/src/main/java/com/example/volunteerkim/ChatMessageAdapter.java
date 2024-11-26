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
import java.util.Locale;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {
    private ArrayList<ChatMessage> messages;
    private String currentUserId;

    public ChatMessageAdapter(ArrayList<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (message.getSenderId().equals(currentUserId)) {
            holder.textViewMessage.setBackgroundResource(R.drawable.bg_message_sent);
            holder.textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        } else {
            holder.textViewMessage.setBackgroundResource(R.drawable.bg_message_received);
            holder.textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        }

        holder.textViewMessage.setText(message.getText());
        String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(message.getTimestamp()));
        holder.textViewTime.setText(timeText);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMessage, textViewTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }
}
