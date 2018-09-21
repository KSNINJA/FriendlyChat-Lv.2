package com.example.kartiksinghal.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class new_chat extends AppCompatActivity {

    String name_of_the_chat;
    String recipient_one;
    String recipient_two;
    String recipient_three;
    EditText name_view ;
    EditText name_of_recipient_one;
    EditText name_of_recipient_two;
    EditText name_of_recipient_three;
    Button new_chat_btn;
    ImageView new_chat_image;
    private static  final int RC_PHOTO_PICKER =2;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    String link ;
    Uri downloadUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_message);
        name_view = (EditText) findViewById(R.id.chat_name);
        name_of_recipient_one = (EditText) findViewById(R.id.recipient_1);
        name_of_recipient_two = (EditText) findViewById(R.id.recipient_2);
        name_of_recipient_three= (EditText) findViewById(R.id.recipient_3);
        new_chat_btn = (Button) findViewById(R.id.new_chat_form_btn);
        Bundle extras = getIntent().getExtras();
        String mUsername = extras.getString("mUsername");
        name_of_recipient_one.setText(mUsername);
        name_of_recipient_one.setFocusable(false);
        new_chat_image = (ImageView) findViewById(R.id.new_chat_img_view);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("database");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("chat_photos");
    }

    public void new_chat_form_filled(View view)
    {
        name_of_the_chat = name_view.getText().toString();
        recipient_one = name_of_recipient_one.getText().toString();
        recipient_two = name_of_recipient_two.getText().toString();
        recipient_three = name_of_recipient_three.getText().toString();
        recipient_one=recipient_one.replace('.' , '/');
        recipient_two=recipient_two.replace('.' , '/');
        recipient_three=recipient_three.replace('.' , '/');
        ArrayList<String> recipients = new ArrayList<>();
        recipients.add(recipient_one);
        recipients.add(recipient_two);
        recipients.add(recipient_three);
      //  mDatabaseReference.child(name_of_the_chat).child("recipients").setValue(recipients);


        FriendlyMessage friendlyMessage = new FriendlyMessage("Welcome to FriendlyChat","App team","" ,"");


        String key = mDatabaseReference.child("chats").push().getKey();
        mDatabaseReference.child("chats").child(key).child("chat_image").setValue(link);
        mDatabaseReference.child("chats").child(key).child("chat_name").setValue(name_of_the_chat);
        mDatabaseReference.child("chats").child(key).child("key").setValue(key);

        mDatabaseReference.child("chats").child(key).child("messages").push().setValue(friendlyMessage);


            for (String recipient_name:recipients
                 ) {
                if(!recipient_name.equals(null)) {
                    mDatabaseReference.child("users").child(recipient_name).push().setValue(key);
                }
            }

            finish();

    }
    public void select_image(View view)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_PHOTO_PICKER && resultCode==RESULT_OK)
        {
            final Uri selectedImageUri = data.getData();
            new_chat_btn.setEnabled(false);
            final StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                             downloadUrl = uri;
                            link = downloadUrl.toString();
                            Glide.with(new_chat_image.getContext())
                                    .load(downloadUrl)
                                    .into(new_chat_image);
                            new_chat_btn.setEnabled(true);


                        }
                    });

                }
            });
        }
    }
}
