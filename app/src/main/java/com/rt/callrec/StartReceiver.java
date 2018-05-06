package com.rt.callrec;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by QNIT on 12/1/2016.
 */

public class StartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Intent i = new Intent(context, RecService.class);
            context.startService(i);
            Toast.makeText(context, "Started service", Toast.LENGTH_SHORT).show();
        }
    }
}
