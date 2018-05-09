package com.rt.callrec;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_OUT);
        filter.addAction(ACTION_IN);
        this.registerReceiver(new CallReceiver(), filter);
//        return super.onStartCommand(intent, flags, startId);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = this.getSharedPreferences("RUA", MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public abstract class PhonecallReceiver extends BroadcastReceiver {
        //The receiver will be recreated whenever android feels like it.
        // We need a static variable to remember data between instantiations

        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private boolean isIncoming;
        private String savedNumber;  //because the passed incoming is only valid in ringing

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "OnReceive", Toast.LENGTH_SHORT).show();
            //We listen to two intents. The new outgoing call only tells us of an outgoing call. We use it to get the number.

//            TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
//            @SuppressLint("MissingPermission")
//            String mPhoneNumber = tMgr.getLine1Number();

            if (intent.getAction().equals(ACTION_OUT)) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }

                onCallStateChanged(context, state, number);
            }
        }

        //Derived classes should override these to respond to specific events of interest
        protected abstract void onIncomingCallReceived(Context ctx, String number);

        protected abstract void onIncomingCallAnswered(Context ctx, String number);

        protected abstract void onIncomingCallEnded(Context ctx);

        protected abstract void onOutgoingCallStarted(Context ctx, String number);

        protected abstract void onOutgoingCallEnded(Context ctx);

        protected abstract void onMissedCall(Context ctx, String number);

        //Deals with actual events

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        public void onCallStateChanged(Context context, int state, String number) {
            if (lastState == state) {
                //No change, debounce extras
                return;
            }
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    savedNumber = number;
                    onIncomingCallReceived(context, number);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        onOutgoingCallStarted(context, savedNumber);
                    } else {
                        isIncoming = true;
                        onIncomingCallAnswered(context, savedNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber);
                    } else if (isIncoming) {
                        onIncomingCallEnded(context);
                    } else {
                        onOutgoingCallEnded(context);
                    }
                    break;
            }
            lastState = state;
        }
    }

    public class CallReceiver extends PhonecallReceiver {
        private File file;

        @Override
        protected void onIncomingCallReceived(Context ctx, String number) {

        }

        @Override
        protected void onIncomingCallAnswered(Context ctx, String number) {
            startRecording(ctx, "IN", number);
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number) {
            startRecording(ctx, "OUT", number);
        }

        @Override
        protected void onIncomingCallEnded(Context ctx) {
            endRecording(ctx);
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx) {
            endRecording(ctx);
        }

        @Override
        protected void onMissedCall(Context ctx, String number) {

        }

        private void startRecording(Context context, String callAction, String number) {
            String fileName = number + DateFormat.format(getString(R.string.date_time_format), new Date()) +
                    "_" + callAction +".mp3";

            File root = new File(context.getFilesDir().getAbsolutePath());

            file = null;
            try {
                file = File.createTempFile(fileName,".mp3",root);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recording = new Recording(file);
            recording.startRecCommunication(context);
        }

        private void endRecording(Context context) {
            if (recording != null) {
                recording.stopRecording();
                recording = null;

                List<String> aye = Arrays.asList(new File(context.getFilesDir().getAbsolutePath()).list());
                for (int i = 0; i < aye.size(); i++) {
                    if (aye.get(i).split("mp3").length > 0) {
                        UploadFile(context, aye.get(i));
                    }
                }
            }
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
    }
}
