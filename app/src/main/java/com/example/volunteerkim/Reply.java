package com.example.volunteerkim;

import com.google.firebase.Timestamp;

public class Reply {
    private String replyId;
    private String content;
    private String author;
    private Timestamp timestamp;

    // 기본 생성자
    public Reply() {}

    // Getter 메서드
    public String getReplyId() {
        return replyId;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Setter 메서드
    public void setReplyId(String replyId) {
        this.replyId = replyId;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
