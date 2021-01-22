package com.inpt.models;

public class LikesModel {
    private String userId;
    private String userName;
    private String pdpUrl;

    public LikesModel() {

    }

    public LikesModel(String userId, String userName, String pdpUrl) {
        this.userId = userId;
        this.userName = userName;
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

    public String getPdpUrl() {
        return pdpUrl;
    }

    public void setPdpUrl(String pdpUrl) {
        this.pdpUrl = pdpUrl;
    }
}
