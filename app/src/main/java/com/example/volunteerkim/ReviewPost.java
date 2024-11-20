package com.example.volunteerkim;

import com.google.firebase.Timestamp;

public class ReviewPost {
    String place;
    String author;
    String category;
    String content;
    int startTime;
    int endTime;
    String photo;
    Timestamp timestamp;
    float rating;

    public ReviewPost() {}

    // 모든 필드를 포함한 생성자
    public ReviewPost(String place, String author, String category, String content,
                      int startTime, int endTime, Timestamp timestamp, float rating) {
        this.place = place;
        this.author = author;
        this.category = category;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    // Getter 메서드들
    public String getPlace() {
        return place;
    }

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public float getRating() {
        return rating;
    }

    // Setter 메서드들
    public void setPlace(String place) {
        this.place = place;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

