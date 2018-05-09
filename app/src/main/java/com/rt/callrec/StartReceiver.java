package com.rt.callrec;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by QNIT on 12/1/2016.
 */

public class StartReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent i = new Intent(context, RecService.class);
            context.startService(i);
            Toast.makeText(context, "Started service", Toast.LENGTH_SHORT).show();
        }

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
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


    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                savedNumber = number;
                RecService.startService(context,"IN",savedNumber);
                String log = "CALL_STATE_RINGING " + savedNumber ;
                Toast.makeText(context, log, Toast.LENGTH_SHORT).show();
                break;

            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                }
                Intent intent = new Intent(context, RecService.class);
                intent.putExtra("callAction", "OUT");
                intent.putExtra("number", number);
                context.startService(intent);
                String log4 = "CALL_STATE_OFFHOOK " + savedNumber ;
                Toast.makeText(context, log4, Toast.LENGTH_SHORT).show();
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    String log3 = "lastState " + savedNumber ;
                    Toast.makeText(context, log3, Toast.LENGTH_SHORT).show();
                } else if (isIncoming) {
                    String log2 = "Incoming " + savedNumber ;
                    Toast.makeText(context, log2, Toast.LENGTH_SHORT).show();


                } else {
                    String log1 = "outgoing " + savedNumber ;
                    Toast.makeText(context, log1, Toast.LENGTH_SHORT).show();
                }
                Intent intent1 = new Intent(context, RecService.class);
                context.stopService(intent1);
                break;
        }
        lastState = state;
    }
}
