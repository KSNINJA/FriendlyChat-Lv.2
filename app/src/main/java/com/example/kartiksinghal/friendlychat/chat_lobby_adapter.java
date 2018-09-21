package com.example.kartiksinghal.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.bumptech.glide.Glide;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class chat_lobby_adapter extends ArrayAdapter<Chatdetails> {
    public chat_lobby_adapter(Context context, int resource, List<Chatdetails> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.chat_preview_item, parent, false);
        }


        TextView chat_name_view = (TextView) convertView.findViewById(R.id.chat_name);
        TextView chat_recipients_view = (TextView) convertView.findViewById(R.id.chat_recipients);
        ImageView chat_image = (ImageView) convertView.findViewById(R.id.chat_photo);
        Chatdetails details = getItem(position);

            chat_name_view.setText(details.chat_name);
            String LastMessage = details.LastMessage;
            chat_recipients_view.setText(LastMessage);


        chat_image.setVisibility(View.VISIBLE);
        Glide.with(chat_image.getContext())
                .load(details.chat_photo_link)
                .into(chat_image);






        return convertView;
    }

}
