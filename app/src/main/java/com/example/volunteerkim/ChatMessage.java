package com.example.volunteerkim;

public class ChatMessage {
    private String messageId;
    private String text;
    private String senderId;
    private long timestamp;

    // Constructor
    public ChatMessage(String messageId, String text, String senderId, long timestamp) {
        this.messageId = messageId;
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Default constructor (Firebase 사용 시 필요)
    public ChatMessage() {}

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public String getText() {
        return text;
    }

    public String getSenderId() {
        return senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
