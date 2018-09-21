package com.example.kartiksinghal.friendlychat;


import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public  class Chatdetails {
    String chat_name;
    String LastMessage ;
    String chat_photo_link ;
    String key;
    ArrayList<String> messages = new ArrayList<>();
    Chatdetails(String last_message ,String chat_name_ ,String chat_image, String received_key )
    {
        this.LastMessage = last_message ;
        this.chat_name = chat_name_;
        this.chat_photo_link = chat_image;
        key = received_key ;
    }

    public  void addMessage(String received_message)
    {
        messages.add(received_message);
    }

}
