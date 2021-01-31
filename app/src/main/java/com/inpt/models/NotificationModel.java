package com.inpt.models;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String from;
    private String fromName;
    private String fromPdp;
    private String to;
    private String toPdp;
    private String toUsername;
    private String type;
    private String postId;
    private String postUrl;
    private Timestamp date;
    private String message;

    public String getImageNotified() {
        return imageNotified;
    }

    public void setImageNotified(String imageNotified) {
        this.imageNotified = imageNotified;
    }

    private String imageNotified;

    public NotificationModel() {

    }

    public NotificationModel(String fromName, String fromPdp,String message,String type,String to,String from) {
        this.fromName = fromName;
        this.fromPdp = fromPdp;
        this.message= message;
        this.from=from;
        this.to=to;
        this.type=type;
    }

    public NotificationModel(String from, String fromName, String fromPdp, String type, String postId,String imageNotified, Timestamp date) {
        this.from = from;
        this.fromName = fromName;
        this.fromPdp = fromPdp;
        this.type = type;
        this.postId = postId;
        this.imageNotified=imageNotified;
        this.date = date;
    }

    public NotificationModel(String from, String fromName, String fromPdp,Timestamp date, String type) {
        this.from = from;
        this.fromName = fromName;
        this.fromPdp = fromPdp;
        this.type = type;
        this.date = date;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getFromPdp() {
        return fromPdp;
    }

    public void setFromPdp(String fromPdp) {
        this.fromPdp = fromPdp;
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

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getToPdp() {
        return toPdp;
    }

    public void setToPdp(String toPdp) {
        this.toPdp = toPdp;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
