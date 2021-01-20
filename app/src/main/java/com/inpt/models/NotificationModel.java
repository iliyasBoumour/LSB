package com.inpt.models;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String from;
    private String to;
    private String type;
    private String postId;
    private Timestamp date;

    public NotificationModel() {

    }

    public NotificationModel(String from, String to, String type, String postId, Timestamp date) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.postId = postId;
        this.date = date;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }
}
