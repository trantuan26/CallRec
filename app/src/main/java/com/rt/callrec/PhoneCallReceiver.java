package com.rt.callrec;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by mac24h on 4/30/18.
 */

public abstract class PhoneCallReceiver extends BroadcastReceiver {
    private static final String
            ACTION_IN = "android.intent.action.PHONE_STATE",
            ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL",
            BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    //The receiver will be recreated whenever android feels like it.
    // We need a static variable to remember data between instantiations

    private int lastState = TelephonyManager.CALL_STATE_IDLE;
    private boolean isIncoming;
    private String savedNumber;  //because the passed incoming is only valid in ringing

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "OnReceive", Toast.LENGTH_SHORT).show();
        //We listen to two intents. The new outgoing call only tells us of an outgoing call. We use it to get the number.

        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String mPhoneNumber = tMgr.getLine1Number();
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
