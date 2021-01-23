package com.inpt.notifications;

public class Data {
    private String userId;
    private String userName;
    private String type;
    private String postId;
    private String pdpUrl;

    public Data(String userId, String userName, String type, String postId, String pdpUrl) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.postId = postId;
        this.pdpUrl = pdpUrl;
    }

    public Data() {
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

    public String getPdpUrl() {
        return pdpUrl;
    }

    public void setPdpUrl(String pdpUrl) {
        this.pdpUrl = pdpUrl;
    }
}
