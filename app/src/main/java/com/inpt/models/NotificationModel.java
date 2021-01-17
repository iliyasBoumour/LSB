package com.inpt.models;

import com.google.firebase.Timestamp;

public class NotificationModel {
    private String pdp;
    private String notification;
    private Timestamp date;
    private String image;

    public NotificationModel(String pdp, String notification, Timestamp date, String image) {
        this.pdp = pdp;
        this.notification = notification;
        this.date = date;
        this.image = image;
    }
    public NotificationModel(String pdp, String notification, Timestamp date) {
        this.pdp = pdp;
        this.notification = notification;
        this.date = date;
        this.image = "";
    }
    public NotificationModel(String notification, Timestamp date) {
        this.notification = notification;
        this.date = date;
        this.image = "";
    }

    public String getPdp() {
        return pdp;
    }

    public void setPdp(String pdp) {
        this.pdp = pdp;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
