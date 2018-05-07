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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
    List_Rec_Frm list_rec_frm;
    private FirebaseAuth mAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.muit_List:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flContent, list_rec_frm)
                            .commit();
                    return true;
                case R.id.muit_Infor:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.flContent, new AccountsFragment())
                            .commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_l);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        }

        mAuth = FirebaseAuth.getInstance();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        list_rec_frm = new List_Rec_Frm();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flContent, list_rec_frm)
                .commit();

//        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_IN);
//        filter.addAction(ACTION_OUT);
//        filter.addAction(BOOT_COMPLETED);
//        this.registerReceiver(new RecReceiver(), filter);
        startService(new Intent(MainAc.this, RecService.class));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {
        String[] permissions = new String[]{
                android.Manifest.permission.RECORD_AUDIO
                , android.Manifest.permission.READ_EXTERNAL_STORAGE
                , android.Manifest.permission.READ_PHONE_STATE
                , android.Manifest.permission.PROCESS_OUTGOING_CALLS
                , android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                , Manifest.permission.RECEIVE_BOOT_COMPLETED

        };
        List<String> listPermissionsNeeded = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    RECORD_AUDIO_REQUEST_CODE);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            int count = 0;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    count++;
                }
            }
            if (grantResults.length == count) {
                //Toast.makeText(this, "Record Audio permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }


}
