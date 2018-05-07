package com.rt.callrec;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ListAudioActivity extends AppCompatActivity
       {
    private static final String TAG = "ListSongActivity";
    private List<Audio> listAudio;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;


    private ProgressDialog progressDialog;



    private AudioAdapter audioAdapter;


    private LinearLayout ln_seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_audio);

        recyclerView = findViewById(R.id.song_list);
        progressDialog = new ProgressDialog(this);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("reccall");
        mDatabaseReference.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();


        if (listAudio == null) {
            listAudio = new ArrayList<>();
            audioAdapter = new AudioAdapter(listAudio, this);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(audioAdapter);
        }

    }




    @Override
    protected void onStart() {
        super.onStart();
        GetList();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }




    private void GetList() {
        progressDialog.setTitle("List Recording");
        progressDialog.setMessage("Please wait, is loading..");
        progressDialog.show();
        listAudio.clear();
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Audio audio = dataSnapshot.getValue(Audio.class);
                //users.setUser_online(dataSnapshot.child("online").getValue().toString());
                //Log.d("onChildAdded", "s: " + s);
                //Log.d("onChildAdded", "dataSnapshot: " + new Gson().toJson(audio));
                //Log.d("onChildAdded", "dataSnapshot: " + dataSnapshot.getKey());
                listAudio.add(0, audio);
                audioAdapter.ChangeList(listAudio);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                },600);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (progressDialog!=null)
                    progressDialog.dismiss();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
               if (progressDialog!=null)
                progressDialog.dismiss();
            }
        });

    }


}
