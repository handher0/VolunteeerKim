package com.example.volunteerkim;

import com.google.firebase.Timestamp;

public class ReviewPost {
    String place;
    String address;
    String author;
    String category;
    String content;
    String startTime;
    String endTime;
    String photo;
    Timestamp timestamp;
    float rating;

    public ReviewPost() {}

    // 모든 필드를 포함한 생성자
    public ReviewPost(String place, String address, String author, String category, String content,
                      String startTime, String endTime, Timestamp timestamp, float rating) {
        this.place = place;
        this.address = address;
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

    public String getAddress(){return address;}

    public String getAuthor() {
        return author;
    }

    public String getCategory() {
        return category;
    }

    public String getContent() {
        return content;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
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

    public void setAddress(String address) { this.address = address;}

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

