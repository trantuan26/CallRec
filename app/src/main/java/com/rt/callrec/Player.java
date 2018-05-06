package com.rt.callrec;
import android.media.MediaPlayer;
import android.os.Handler;
import java.io.IOException;

/**
 * Created by QNIT on 12/5/2016.
 */

public abstract class Player extends MediaPlayer implements Runnable, MediaPlayer.OnCompletionListener {
    private Handler handler;
    private boolean isRunning;

    public Player(String sourcePath) {
        isRunning = true;
        try {
            setDataSource(sourcePath);
            prepare();
            setOnCompletionListener(this);
            new Thread(this).start();
            handler = new Handler();
            onPrepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (isRunning) {
            onPlaying();
            handler.postDelayed(this, 200);
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            isRunning = false;
            super.stop();
        }
    }

    public abstract void onPrepare();

    public abstract void onPlaying();

    public abstract void togglePlayPause();

}
