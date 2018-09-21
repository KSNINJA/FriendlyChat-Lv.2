package com.example.kartiksinghal.friendlychat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ChildEventListener mChildEventListener;
    public static final int RC_SIGN_IN = 1;
    private chat_lobby_adapter mchat_lobby_adapter;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference myDatabaseReference;

    ListView chat_lobby_listView ;
    RelativeLayout chat_lobby_view;
    public String mUsername = "";
    String chat_image ;
    String chat_name;
    String LastMessage;
    SharedPreferences.Editor chat_pref_editor;
    SharedPreferences.Editor messages_pref_editor;
    SharedPreferences chat_details_pref ;
    SharedPreferences messages_pref;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_lobby);

        mFirebaseAuth = FirebaseAuth.getInstance();
        chat_lobby_listView = (ListView) findViewById(R.id.chat_lobby_listView);
        chat_lobby_view = (RelativeLayout) findViewById(R.id.chat_lobby_view);
        final List<Chatdetails> chat_details = new ArrayList<>();
        mchat_lobby_adapter = new chat_lobby_adapter(this, R.layout.chat_preview_item, chat_details);
        chat_lobby_listView.setAdapter(mchat_lobby_adapter);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myDatabaseReference = mFirebaseDatabase.getReference().child("database").child("users");
        mDatabaseReference = mFirebaseDatabase.getReference().child("database");
        chat_details_pref = getSharedPreferences("chat_details_pref",Context.MODE_PRIVATE);
        messages_pref = getSharedPreferences("messages_pref",Context.MODE_PRIVATE);
        chat_pref_editor = chat_details_pref.edit();
        messages_pref_editor = messages_pref.edit();



       mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user_auth = firebaseAuth.getCurrentUser();
                if (user_auth != null) {
                    //signed in
                        mUsername = user_auth.getEmail();
                        attachChatReadListener();
                        attachReadListener();

                       /* if(isNetworkAvailable()) {
                            attachReadListener();
                            chat_pref_editor.clear();
                        }
                        else {

                            Map<String, ?> keys = chat_details_pref.getAll();

                            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                                Gson gson = new Gson();
                                Chatdetails chatdetails = gson.fromJson(entry.getValue().toString(), Chatdetails.class);
                                mchat_lobby_adapter.add(chatdetails);
                            }
                        }*/

                } else {
                    //signed out
                    mchat_lobby_adapter.clear();
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);




        chat_lobby_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object details = chat_lobby_listView.getItemAtPosition(position);
                Chatdetails chatdetails = (Chatdetails) details ;
                String Name_of_chat =chatdetails.chat_name;
                Intent intent = new Intent( MainActivity.this ,Messaging.class );
                intent.putExtra("chatName",Name_of_chat);
                intent.putExtra("chat_photo_link" , ((Chatdetails) details).chat_photo_link);
                intent.putExtra("mUsername" , mUsername);
                intent.putExtra("key" , chatdetails.key);
                startActivity(intent);
            }
        });







    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.New) {
            newChat();
        }
        if (id == R.id.sign_out) {
            AuthUI.getInstance().signOut(this);
        }

        return super.onOptionsItemSelected(item);
    }



    public void newChat() {
        Intent intent = new Intent(this, new_chat.class);
        intent.putExtra("mUsername" , mUsername);
        startActivity(intent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                messages_pref_editor.remove(chatdetails.key);
                messages_pref_editor.putString(chatdetails.key , chatdetails.messages.toString());
                messages_pref_editor.commit();
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

    private void attachReadListener()
    {
        Map<String, ?> keys = chat_details_pref.getAll();
        for(Map.Entry<String, ?> entry : keys.entrySet()) {
            Gson gson = new Gson();
            Chatdetails chatdetails = gson.fromJson(entry.getValue().toString(), Chatdetails.class);
            mchat_lobby_adapter.add(chatdetails);
        }

        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Gson gson = new Gson();
                String chat = chat_details_pref.getString(key , "");
                Chatdetails chatdetails = gson.fromJson(chat, Chatdetails.class);
                mchat_lobby_adapter.add(chatdetails);
            }
        };
        chat_details_pref.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

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
                chat_pref_editor.commit();
                attachMessagesReadListener(chatdetails);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void attachChatReadListener(){

        // if(mChildEventListener == null && mUsername!=null) {
     //   chat_pref_editor.clear();
        ChildEventListener mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull  DataSnapshot dataSnapshot, @Nullable String s) {
                String chat_key= dataSnapshot.getValue().toString();
                chat_key = chat_key.substring(1,chat_key.length()-1);
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
        encodedUsername = mUsername.replace('.', '?');
        encodedUsername = encodedUsername.substring(0 , encodedUsername.length()-4);
        mDatabaseReference.child("users").child(encodedUsername).addChildEventListener(mChildEventListener);
        // }

    }

}


