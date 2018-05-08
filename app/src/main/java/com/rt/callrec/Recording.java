package com.rt.callrec;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by QNIT on 11/28/2016.
 */

public class Recording extends MediaRecorder {
    private boolean isStarted = false;
    private File file;

    public Recording(File file) {
        this.file = file;
    }

    public void startRecording() {
        this.setOutputFormat(OutputFormat.AAC_ADTS);
        this.setAudioEncoder(AudioEncoder.AAC);
//        this.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        this.setAudioEncodingBitRate(64000);

//        if(!file.exists()){
//            try {
//                file.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        this.setOutputFile(file.getAbsolutePath());

        try {
            this.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.start();
        isStarted = true;
    }

    public void stopRecording() {
        if (isStarted) {
            this.stop();
            isStarted = false;
        }
    }

    public void startRecCommunication(Context context) {
//        int deviceCallVol;
//        AudioManager audioManager;


//        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//get the current volume set
//        deviceCallVol = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
//set volume to maximum
//        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);

        this.setAudioSource(AudioSource.VOICE_COMMUNICATION);

//        this.setAudioChannels(2);
        this.startRecording();
    }

    public void startRecFromMic() {
        this.setAudioSource(AudioSource.MIC);
        this.startRecording();
    }
}
