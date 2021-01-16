package com.inpt.models;

import com.google.firebase.Timestamp;

public class Post {
    private String postId;
    private String imageUrl;
    private String caption;
    private String userId;
    private int nbLike;
    private Timestamp timeAdded;

    public Post() {

    }

    public Post(String postId, String imageUrl, String caption, String userId, int nbLike, Timestamp timeAdded) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.caption = caption;
        this.userId = userId;
        this.nbLike = nbLike;
        this.timeAdded = timeAdded;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getNbLike() {
        return nbLike;
    }

    public void setNbLike(int nbLike) {
        this.nbLike = nbLike;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }
}


