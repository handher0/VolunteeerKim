package com.example.volunteerkim;

import com.google.firebase.Timestamp;

import java.util.List;

public class ReviewPost {
    String postId;
    String place;
    String address;
    String author;
    String category;
    String content;
    private String volunteerDate;
    String startTime;
    String endTime;
    List<String> imageUrls;
    boolean hasImages = false;
    Timestamp timestamp;
    float rating;

    public ReviewPost() {}

    // 모든 필드를 포함한 생성자
    public ReviewPost(String postId, String place, String address, String author, String category, String content, String volunteerDate,
                      String startTime, String endTime, List<String> imageUrls, boolean hasImages, Timestamp timestamp, float rating) {
        this.postId = postId;
        this.place = place;
        this.address = address;
        this.author = author;
        this.category = category;
        this.content = content;
        this.volunteerDate = volunteerDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageUrls = imageUrls;
        this.hasImages = hasImages;
        this.timestamp = timestamp;
        this.rating = rating;
    }

    // Getter 메서드들

    public String getPostId() {return postId;}

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

    public String getVolunteerDate() {return volunteerDate;}


    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public List<String> getImageUrls() {return imageUrls;}

    public boolean getHasImages() {
        return hasImages;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public float getRating() {
        return rating;
    }

    // Setter 메서드들

    public void setPostId(String postId) {this.postId = postId;}

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

    public void setVolunteerDate(String volunteerDate) {this.volunteerDate = volunteerDate;}

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setImageUrls(List<String> imageUrls) {this.imageUrls = imageUrls;}

    public void setHasImages(boolean hasImages) {
        this.hasImages = hasImages;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}

