package com.foo.fuckyou;

public class Message {
    private String text;
    private String senderId;
    private long timestamp;

    public Message(String text, String senderId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
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
