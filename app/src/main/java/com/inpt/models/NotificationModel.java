package com.inpt.models;

import java.util.Date;

public class NotificationModel {
    private String pdp;
    private String notification;
    private Date date;
    private String image;

    public NotificationModel(String pdp, String notification, Date date, String image) {
        this.pdp = pdp;
        this.notification = notification;
        this.date = date;
        this.image = image;
    }
    public NotificationModel(String pdp, String notification, Date date) {
        this.pdp = pdp;
        this.notification = notification;
        this.date = date;
        this.image = "";
    }
    public NotificationModel(String notification, Date date) {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
