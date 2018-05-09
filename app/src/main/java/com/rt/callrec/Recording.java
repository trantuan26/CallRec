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
        try {
            this.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            this.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            this.setAudioSamplingRate(8000);
            this.setAudioEncodingBitRate(12200);
            this.setOutputFile(file.getAbsolutePath());
            this.prepare();
            this.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        this.setAudioSource(AudioSource.MIC);

//        this.setAudioChannels(2);
        this.startRecording();
    }

    public void startRecFromMic() {
        this.setAudioSource(AudioSource.MIC);
        this.startRecording();
    }
}
