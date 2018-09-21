package com.example.kartiksinghal.friendlychat;

import java.sql.Date;
import java.sql.Time;

public class FriendlyMessage {
    private String text;
    private String name;
    private String photoUrl;
    public String time_of_upload;

    public FriendlyMessage() {
    }

    public FriendlyMessage(String text_received, String name, String photoUrl,String time) {
        this.text = text_received;
        this.name = name;
        this.photoUrl = photoUrl;
        this.time_of_upload = time;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTimeOfUpload(){return time_of_upload;}
}
