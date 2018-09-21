package com.example.kartiksinghal.friendlychat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.net.URL;
import java.util.List;

public class MessageAdapter extends ArrayAdapter {
    public MessageAdapter(Context context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_message, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        LinearLayout messageItem = (LinearLayout) convertView.findViewById(R.id.message_item);
        TextView time_view = (TextView) convertView.findViewById(R.id.time_of_upload_view);

        FriendlyMessage message = (FriendlyMessage) getItem(position);

        boolean isPhoto =!message.getPhotoUrl().equals("");
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            messageTextView.setText(message.getPhotoUrl());
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        String mUsername = Messaging.mUsername;

        if(message.getName().equals(mUsername))
        {
            messageItem.setBackgroundColor(Color.parseColor("#FFF8C5"));
            authorTextView.setTextColor(Color.parseColor("#32CD32"));
        }
        else {
            messageItem.setBackgroundColor(Color.WHITE);
            authorTextView.setTextColor(Color.parseColor("#FFA500"));
        }

        authorTextView.setText('-' + message.getName());
        time_view.setText(message.getTimeOfUpload());

        return convertView;
    }
}
