package com.inpt.notifications;

public class Data {
    private String userId;
    private String userName;
    private String to;
    private String toUsername;
    private String toPdp;
    private String type;
    private String postUrl;
    private String pdpUrl;
    private String message;



    private String postId;

    public Data(String userId, String userName, String type, String postUrl, String pdpUrl,String postId,String toUsername,String toPdp,String to) {
        this.userId = userId;
        this.userName = userName;
        this.type = type;
        this.postUrl = postUrl;
        this.pdpUrl = pdpUrl;
        this.postId=postId;
        this.toUsername=toUsername;
        this.toPdp=toPdp;
        this.to=to;
    }
//  for messages
    public Data(String from, String userName, String pdpUrl,String message,String type,String to) {
        this.userId=from;
        this.userName = userName;
        this.pdpUrl = pdpUrl;
        this.type=type;
        this.message=message;
        this.to=to;
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

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getToPdp() {
        return toPdp;
    }

    public void setToPdp(String toPdp) {
        this.toPdp = toPdp;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
