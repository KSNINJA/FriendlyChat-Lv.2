package com.example.kartiksinghal.friendlychat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.data.LocalUriFetcher;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.Array;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class Messaging extends AppCompatActivity {
    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER  = 2;
    public static final int RC_BACKGROUND_PICKER = 3;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;
    private ImageView chat_title_image ;
    private TextView chat_name_title;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mChatDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;
    public static String mUsername;
    SharedPreferences messages_pref;
    SharedPreferences.Editor message_pref_editor;
    SharedPreferences.Editor chat_pref_editor;
    SharedPreferences chat_details_pref ;
    String key;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        String chat_name = extras.getString("chatName");
        String chat_photo_link = extras.getString("chat_photo_link");
        mUsername = extras.getString("mUsername");
         key = extras.getString("key");
        messages_pref = getSharedPreferences("messages_pref",Context.MODE_PRIVATE);
        message_pref_editor = messages_pref.edit();
        chat_details_pref = getSharedPreferences("chat_details_pref" , Context.MODE_PRIVATE);
        chat_pref_editor = chat_details_pref.edit();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        mChatDatabaseReference = mDatabaseReference.child("database").child("chats").child(key).child("messages");

        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        mProgressBar = findViewById(R.id.progressBar);
        mMessageListView = findViewById(R.id.messageListView);

        mPhotoPickerButton = findViewById(R.id.photoPickerButton);
        mMessageEditText = findViewById(R.id.messageEditText);
        mSendButton = findViewById(R.id.sendButton);

        mMessageListView.setSmoothScrollbarEnabled(true);
        // Initialize message ListView and its adapter
        final List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);
        setTitle(chat_name );
        isExecuted=false;

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date time = Calendar.getInstance().getTime();
                DateFormat formatdate = new SimpleDateFormat("HH:mm");
                String formattedDate = formatdate.format(time);
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername,"", formattedDate);
                mChatDatabaseReference.push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");
            }
        });



          /* for(Map.Entry<String,?> entry : keys.entrySet()){
                Gson gson = new Gson();
                FriendlyMessage friendlyMessage = gson.fromJson(entry.getValue().toString() , FriendlyMessage.class);
                MessageAdapter.add(chatdetails);
            //}
        }*/

              String messages_string = messages_pref.getString(key, "");
              messages_string = messages_string.substring(1 , messages_string.length()-1);
              String messages[];
              messages = messages_string.split(",");
              for (String eachMessage : messages
                      ) {
                  eachMessage = eachMessage.replace( "/comma", ",");
                  Gson gson = new Gson();
                  FriendlyMessage friendlyMessage = gson.fromJson(eachMessage, FriendlyMessage.class);
                  mMessageAdapter.add(friendlyMessage);

              }

        attachReadListener();


    }
    @Override
    public void onActivityResult(int requestCode ,int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode == RC_PHOTO_PICKER && resultCode ==RESULT_OK )
        {
            final Uri selectedImageUri = data.getData();
            final StorageReference photoRef = mStorageReference.child(selectedImageUri.getLastPathSegment());
            photoRef.putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            Uri downloadUrl = uri;
                            Date time = Calendar.getInstance().getTime();
                            DateFormat formatdate = new SimpleDateFormat("HH:mm");
                            String formattedDate = formatdate.format(time);
                            FriendlyMessage friendlyMessage = new FriendlyMessage("", mUsername , downloadUrl.toString() , formattedDate);
                            mChatDatabaseReference.push().setValue(friendlyMessage);

                        }
                    });

                }
            });
        }

    }

    boolean isExecuted ;
    public void attachReadListener(){
     /*  String messages_string = messages_pref.getString(key , "");
       messages_string = messages_string.substring(1,messages_string.length()-1);
       String messages [];
       messages  = messages_string.split(",");
        for (String message: messages
             ) {
            Gson gson = new Gson();

            message = message.replace("/comma" , ",");
            FriendlyMessage friendlyMessage = gson.fromJson(message, FriendlyMessage.class);
            mMessageAdapter.add(friendlyMessage);
        }
*/

        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Gson gson = new Gson();
                String chat =messages_pref.getString(key , "");
                FriendlyMessage friendlyMessage = gson.fromJson(chat, FriendlyMessage.class);
                mMessageAdapter.add(friendlyMessage);
            }
        };
        messages_pref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
            /*mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    if(!isExecuted)
                    {
                        mMessageAdapter.clear();
                        isExecuted = true;
                    }
                    mMessageAdapter.add(friendlyMessage);

                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            mChatDatabaseReference.addChildEventListener(mChildEventListener);

*/
        }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachChatReadListener();
    }
    public void attachChatReadListener(){

        // if(mChildEventListener == null && mUsername!=null) {
        chat_pref_editor.clear();
        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull  DataSnapshot dataSnapshot, @Nullable String s) {
                String chat_key= dataSnapshot.getValue().toString();
                addChild(chat_key);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        String encodedUsername;

        encodedUsername = mUsername.replace('.', '/');
        mDatabaseReference.child(encodedUsername).addChildEventListener(mChildEventListener);
        // }

    }
    String chat_image;
    String chat_name;
    String LastMessage;
    public void addChild(final String chat_key)
    {
        mDatabaseReference.child("chats").child(chat_key).orderByKey().equalTo("chat_image").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chat_image = dataSnapshot.child("chat_image").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mDatabaseReference.child("chats").child(chat_key).orderByKey().equalTo("chat_name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chat_name = dataSnapshot.child("chat_name").getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mDatabaseReference.child("chats").child(chat_key).child("messages").orderByValue().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LastMessage = null;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    LastMessage = ds.child("text").getValue().toString() ;
                }
                if(LastMessage.equals(""))
                {
                    LastMessage = "Photo";
                }
                Chatdetails chatdetails = new Chatdetails(LastMessage , chat_name , chat_image , chat_key);
                Gson gson = new Gson();
                String json = gson.toJson(chatdetails);
                chat_pref_editor.putString(chat_key , json);
                chat_pref_editor.apply();
                attachMessagesReadListener(chatdetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void attachMessagesReadListener(final Chatdetails chatdetails){
        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                Gson gson = new Gson();
                String message = gson.toJson(friendlyMessage);
                message = message.replace(",","/comma");
                chatdetails.messages.add(message);
                message_pref_editor.remove(chatdetails.key);
                message_pref_editor.putString(chatdetails.key , chatdetails.messages.toString());
                message_pref_editor.apply();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.child("chats").child(chatdetails.key).child("messages").addChildEventListener(mChildEventListener);


    }


}

