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
        implements MediaController.MediaPlayerControl, AudioAdapter.AudioListener {
    private static final String TAG = "ListSongActivity";
    private List<Audio> listAudio;
    private RecyclerView recyclerView;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean mUserIsSeeking = false;
    private MusicController controller;
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    private TextView mTextDuration;
    private TextView mTextSeekto;
    private SeekBar mSeekbarAudio;

    private ProgressDialog progressDialog;

    private Utilities utils;

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

        setController();
        initializeUI();
        initializeSeekbar();

        if (listAudio == null) {
            listAudio = new ArrayList<>();
            audioAdapter = new AudioAdapter(listAudio, this, this);
            recyclerView.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(audioAdapter);
        }

    }

    private void setController() {
        //set the controller up
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.song_list));
        controller.setEnabled(true);

    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass list
            musicSrv.setList(listAudio);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        GetList();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            this.bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private void resetTimemSeekbarAudio() {
        mSeekbarAudio.setProgress(0);
        mSeekbarAudio.setMax(100);
        // Updating progress bar
        updateProgressBar();
    }


    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: " + musicBound);
        super.onStop();

        if (musicBound) {
            // Hủy giàng buộc kết nối với dịch vụ.
            if (isPlaying()) {
                pause();
            }
            musicSrv = null;
            this.unbindService(musicConnection);
            musicBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + musicBound);
        if (musicBound) {
            // Hủy giàng buộc kết nối với dịch vụ.
            musicSrv = null;
            this.unbindService(musicConnection);
            musicBound = false;
        }

        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void start() {
        musicSrv.go();
    }

    @Override
    public void pause() {
        musicSrv.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound) {
            return musicSrv.getDur();
        } else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng()) {
            return musicSrv.getPosn();
        } else return 0;
    }


    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);

    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    //play next
    private void playNext() {
        musicSrv.playNext();
        controller.show(0);
    }

    //play previous
    private void playPrev() {
        musicSrv.playPrev();
        controller.show(0);
    }

    private void initializeUI() {
        mTextDuration = (TextView) findViewById(R.id.tv_duration);
        mTextSeekto = (TextView) findViewById(R.id.tv_seekto);
        ln_seekbar = findViewById(R.id.ln_seekbar);

        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        utils = new Utilities();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onStop: " + musicBound);
        super.onBackPressed();
        finish();
    }


    private void initializeSeekbar() {
        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                        new Handler().removeCallbacks(mUpdateTimeTask);
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }


                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        musicSrv.seek(userSelectedPosition);
                        new Handler().removeCallbacks(mUpdateTimeTask);
                        int totalDuration = getDuration();
                        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

                        // forward or backward to certain seconds
                        seekTo(currentPosition);

                        // update timer progress again
                        updateProgressBar();
                    }
                });
    }

    public void updateProgressBar() {
        new Handler().postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (isPlaying()) {
                ln_seekbar.setVisibility(View.VISIBLE);
                long totalDuration = getDuration();
                long currentDuration = getCurrentPosition();

                // Displaying Total Duration time
                mTextDuration.setText("" + utils.milliSecondsToTimer(totalDuration));
                // Displaying time completed playing
                mTextSeekto.setText("" + utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
                //Log.d("Progress", ""+progress);
                mSeekbarAudio.setProgress(progress);

                // Running this thread after 100 milliseconds
                new Handler().postDelayed(this, 100);
            } else {
                ln_seekbar.setVisibility(View.GONE);
            }
        }
    };


    private void GetList() {
        progressDialog.setTitle("List Recording");
        progressDialog.setMessage("Please wait, is loading..");
        progressDialog.show();
        mDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Audio audio = dataSnapshot.getValue(Audio.class);
                //users.setUser_online(dataSnapshot.child("online").getValue().toString());
                //Log.d("onChildAdded", "s: " + s);
                //Log.d("onChildAdded", "dataSnapshot: " + new Gson().toJson(audio));
                //Log.d("onChildAdded", "dataSnapshot: " + dataSnapshot.getKey());
                listAudio.add(0, audio);
                audioAdapter.notifyItemInserted(0);
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

    @Override
    public void onClickAudio(Audio audio, int position) {
        if (listAudio != null)
            if (musicSrv != null) {
//                if (isPlaying()) {
//                    pause();
//                } else
                    {
                    musicSrv.setSong(position);
                    musicSrv.playSong();
                    resetTimemSeekbarAudio();
                }
            }

    }


    @Override
    public void onClickLongAudio(Audio audio, int position) {

    }
}
