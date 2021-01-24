package com.inpt.notifications;

public class Data {
    private String userId;
    private String userName;
    private String type;
    private String postUrl;
    private String pdpUrl;



    private String postId;

    public Data(String userId, String userName, String type, String postUrl, String pdpUrl,String postId) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.postUrl = postUrl;
        this.pdpUrl = pdpUrl;
        this.postId=postId;
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
        return postUrl;
    }

    public void setPostId(String postId) {
        this.postUrl = postId;
    }

    public String getPdpUrl() {
        return pdpUrl;
    }

    public void setPdpUrl(String pdpUrl) {
        this.pdpUrl = pdpUrl;
    }public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }
}
