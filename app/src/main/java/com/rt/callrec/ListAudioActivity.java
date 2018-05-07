package com.rt.callrec;

import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


public class ListAudioActivity extends AppCompatActivity {
    private static final String TAG = "ListSongActivity";
    private List<Audio> listAudio;
    private RecyclerView recyclerView;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;


    private ProgressDialog progressDialog;


    private AudioAdapter audioAdapter;


    private Toolbar mToolbar;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_audio);

        recyclerView = findViewById(R.id.song_list);
        progressDialog = new ProgressDialog(this);
        ActionBar();
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
        FirebaseUser user = mAuth.getCurrentUser();
        String onlineUserID="";
        if (user != null){
            onlineUserID = user.getUid();
        }

        final String finalOnlineUserID = onlineUserID;
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Audio audio = dataSnapshot.getValue(Audio.class);
                //users.setUser_online(dataSnapshot.child("online").getValue().toString());
                //Log.d("onChildAdded", "s: " + s);
                //Log.d("onChildAdded", "dataSnapshot: " + new Gson().toJson(audio));
                //Log.d("onChildAdded", "dataSnapshot: " + dataSnapshot.getKey());

                if (finalOnlineUserID.equals(audio.getAdminID())){
                    listAudio.add(0, audio);
                    audioAdapter.ChangeList(listAudio);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                }, 600);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (progressDialog != null)
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
                if (progressDialog != null)
                    progressDialog.dismiss();
            }
        });
    }

    private void ActionBar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // toolbar fancy stuff
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("List Recording Audio");

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_list, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
     searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                audioAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                audioAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }
}
