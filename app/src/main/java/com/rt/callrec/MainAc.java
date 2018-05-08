package com.rt.callrec;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainAc extends AppCompatActivity {
    private static final String
            ACTION_IN = "android.intent.action.PHONE_STATE",
            ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL",
            BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private int RECORD_AUDIO_REQUEST_CODE = 12345;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FragmentManager fragmentManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.muit_List:
                    mReplayFragmentHome();
                    return true;
                case R.id.muit_Infor:
                    mmReplayFragmentAppointment();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_l);


        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fragmentManager = getSupportFragmentManager();

        {
            List_Rec_Frm list_rec_frm = new List_Rec_Frm();
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.flContent, list_rec_frm);
            transaction.commit();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(MainAc.this, RecService.class));
    }

    private void mainLogout() {
        Intent startPageIntent = new Intent(MainAc.this, LoginActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }



    public void mReplayFragmentHome() {

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        List_Rec_Frm fragmentHome = new List_Rec_Frm();

        if (fragmentManager.findFragmentByTag("fragmenthome") != null) {
            //if the fragment exists, show it.
            transaction.show(fragmentManager.findFragmentByTag("fragmenthome")).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            transaction.add(R.id.flContent, fragmentHome, "fragmenthome")
                    .addToBackStack("fragmenthome").commit();
        }
        if (fragmentManager.findFragmentByTag("fragmentcategory") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("fragmentcategory")).commit();
        }

    }


    public void mmReplayFragmentAppointment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        AccountsFragment appointmentFragment = new AccountsFragment();
        if (fragmentManager.findFragmentByTag("fragmentcategory") != null) {
            //if the fragment exists, show it.
            transaction.show(fragmentManager.findFragmentByTag("fragmentcategory")).commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            transaction.add(R.id.flContent, appointmentFragment, "fragmentcategory")
                    .addToBackStack("fragmentcategory").commit();
        }
        if (fragmentManager.findFragmentByTag("fragmenthome") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("fragmenthome")).commit();
        }
    }


}
