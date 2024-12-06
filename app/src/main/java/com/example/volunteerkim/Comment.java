package com.example.volunteerkim;

import com.google.firebase.Timestamp;
import java.util.List;

public class Comment {
    private String commentId;
    private String content;
    private String author;
    private Timestamp timestamp;
    private List<Reply> replies;

    // 기본 생성자
    public Comment() {}

    // Getter 메서드
    public String getCommentId() {
        return commentId;
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

    public List<Reply> getReplies() {
        return replies;
    }

    // Setter 메서드
    public void setCommentId(String commentId) {
        this.commentId = commentId;
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

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }

}