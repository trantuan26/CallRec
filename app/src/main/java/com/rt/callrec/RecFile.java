package com.rt.callrec;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class RecFile extends File {
    String name;
    boolean isPlaying = false;

    public RecFile(File pathname, String name) {
        super(pathname.getAbsolutePath(), name);
        this.name = name;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static List<String> getListFileName(Context context) {
        return Arrays.asList(new File(context.getFilesDir().getAbsolutePath()).list());
    }
}