package com.inpt.models;

import com.google.firebase.Timestamp;

public class LastMessageModel {
    private String pdpUrl;
    private String userId;
    private String userName;
    private String message;
    private long time;

    public LastMessageModel(String pdpUrl, String userId, String userName, String message, long time) {
        this.pdpUrl = pdpUrl;
        this.userId = userId;
        this.userName = userName;
        this.message = message;
        this.time = time;
    }

    public LastMessageModel() {
    }

    public String getPdpUrl() {
        return pdpUrl;
    }

    public void setPdpUrl(String pdpUrl) {
        this.pdpUrl = pdpUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
