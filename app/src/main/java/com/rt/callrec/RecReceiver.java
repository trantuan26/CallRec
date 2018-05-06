package com.rt.callrec;

import android.content.Context;
import android.os.Environment;
import android.text.format.DateFormat;

import java.io.File;
import java.util.Date;

/**
 * Created by mac24h on 5/1/18.
 */

public class RecReceiver extends PhoneCallReceiver {
    private RecFile recFile;
    private Recording recording = null;

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

        File root = Environment.getExternalStorageDirectory();
        File path = new File(root.getAbsolutePath() + "/VoiceRecorderSimplifiedCoding/Audios");
        if (!path.exists()) {
            path.mkdirs();
        }

        String fileName = number + "_"+ DateFormat.format(context.getString(R.string.date_time_format), new Date()) +
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
}
