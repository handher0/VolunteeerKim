package com.example.volunteerkim;

import com.google.firebase.Timestamp;
import java.util.Date;

import java.util.List;

public class OtherPost {
    private String postId;
    private String title;
    private String content;
    private String author;
    private Timestamp timestamp;
    private Date recruitmentStart;
    private Date recruitmentEnd;
    private List<String> imageUrls;
    private boolean hasImages;

    // 기본 생성자
    public OtherPost() {}

    // Getter 메서드들
    public String getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
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

    public Date getRecruitmentStart() {
        return recruitmentStart;
    }

    public Date getRecruitmentEnd() {
        return recruitmentEnd;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public boolean isHasImages() {
        return hasImages;
    }

    // Setter 메서드들
    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setRecruitmentStart(Date recruitmentStart) {
        this.recruitmentStart = recruitmentStart;
    }

    public void setRecruitmentEnd(Date recruitmentEnd) {
        this.recruitmentEnd = recruitmentEnd;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setHasImages(boolean hasImages) {
        this.hasImages = hasImages;
    }
}