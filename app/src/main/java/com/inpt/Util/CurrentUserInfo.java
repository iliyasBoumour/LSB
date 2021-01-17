package com.inpt.Util;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

public class CurrentUserInfo extends Application {
    private String userId;
    private String userName;
    private String pdpUrl;
    private static CurrentUserInfo instance;


    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }

    public CurrentUserInfo() {

    }
    public static CurrentUserInfo getInstance() {
        if(instance == null) {
            instance = new CurrentUserInfo();
        }
        return instance;
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
