package com.rt.callrec;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rt.callrec.Constants.PATH;


/**
 * Created by QNIT on 11/27/2016.
 */

public class RecService extends Service {
    private static final String
            ACTION_IN = "android.intent.action.PHONE_STATE",
            ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private Recording recording = null;
    private FirebaseAuth mAuth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public final static String EXTRA_PHONE_CALL = "GIATRI";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String callAction = intent.getStringExtra("callAction");
        String number = intent.getStringExtra("number");
        startRecording(getApplicationContext(), callAction, number);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = this.getSharedPreferences("RUA", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }



    MediaRecorder mediaRecorder;
    boolean isRecording = false;

    private void startRecording(Context context, String callAction, String number) {
        if (!isRecording) {
            isRecording = true;

            File file = null;
            try {
                String fileName = number+ DateFormat.format(getString(R.string.date_time_format), new Date()) +
                        "_" + callAction;
                File dir = new File(context.getFilesDir().getAbsolutePath());
                mediaRecorder = new MediaRecorder();
                file = File.createTempFile(fileName, ".mp3", dir);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
                mediaRecorder.setAudioSamplingRate(8000);
                mediaRecorder.setAudioEncodingBitRate(12200);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.setOutputFile(file.getAbsolutePath());
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(context,"start record",Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                if (file != null) file.delete();
                isRecording = false;
            }
        }
    }

    private void stopRecording(Context context) {
        if (isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
                Toast.makeText(context,"stop record",Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            List<String> aye = Arrays.asList(new File(context.getFilesDir().getAbsolutePath()).list());
//            for (int i = 0; i < aye.size(); i++) {
//                if (aye.get(i).split("mp3").length > 0) {
//                    UploadFile(context, aye.get(i));
//                }
//            }
        }
    }

    @Override
    public void onDestroy() {
        stopRecording(getApplicationContext());
        super.onDestroy();
    }

    private void UploadFile(Context context, String filename) {
        //cap nhat anh vao store
        mAuth = FirebaseAuth.getInstance();
        String firebaseUserId = "";

        if (mAuth != null) {
            FirebaseUser User = mAuth.getCurrentUser();
            if (User != null) {
                firebaseUserId = User.getUid().toString();
            } else {
                firebaseUserId = sharedPreferences.getString("uerID", "");
            }
        }

        final String filenam = filename;
        final File file = new File(context.getFilesDir().getAbsolutePath(), filenam);
        final Uri resultUri = Uri.fromFile(file);

        StorageReference mStorageRefImage = FirebaseStorage.getInstance().getReference().child(filenam);
        final String finalFirebaseUserId = firebaseUserId;
        mStorageRefImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("reccall").push();
                    Map messageBody = new HashMap();
                    messageBody.put("fileName", filenam);
                    messageBody.put("mUri", downloadUrl);
                    messageBody.put("userID", finalFirebaseUserId);
                    messageBody.put("adminID", finalFirebaseUserId);


                    databaseReference.updateChildren(messageBody, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Log.d("TAG", "onComplete: databaseError");
                            }
                        }
                    });

                    file.delete();

                } else {
//                        Toast.makeText(getContext(), "update picture faile", Toast.LENGTH_LONG).show();
                }

            }
        });
    }




    public static void startService(Context context,String callAction, String number) {
        Intent intent = new Intent(context, RecService.class);
        intent.putExtra("callAction", callAction);
        intent.putExtra("number", number);
        context.startService(intent);
    }


    public static void stopService(Context context) {
        Intent intent = new Intent(context, RecService.class);
        context.stopService(intent);
    }
}
