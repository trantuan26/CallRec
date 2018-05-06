package com.rt.callrec;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.List;

import static com.rt.callrec.Constants.PATH;


/**
 * Created by QNIT on 11/27/2016.
 */

public class RecService extends Service {
    private static final String
            ACTION_IN = "android.intent.action.PHONE_STATE",
            ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL",
            BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private Recording recording = null;

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
                TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                @SuppressLint("MissingPermission")
                String line1Number = tMgr.getLine1Number();
                Log.d("L_line1Number", line1Number);
                savedNumber = line1Number;

//                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
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
        private RecFile recFile;

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

            File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + PATH);
            if (!path.exists()) {
                path.mkdirs();
            }

            String fileName = number + "_"+ DateFormat.format(getString(R.string.date_time_format), new Date()) +
                    "_" + callAction + "_" + "." + "mp3";
            recFile = new RecFile(context, path + "/" + RecFile.getDateCreate(fileName, null) + "/" + fileName);
            recFile.setFormat("mp3");
            recording = new Recording(recFile);
            recording.startRecCommunication(context);
        }

        private void endRecording(Context context) {
            if (recording != null) {
                recording.stopRecording();
                Explorer.insertRecFile(context, recFile);
                recording = null;
            }
        }

        private boolean isNumberInList(String number, List<String> list) {
            if (list != null)
                for (int i = 0; i < list.size(); i++) {
                    if (number.equals(list.get(i))) return true;
                }
            return false;
        }
    }
}
