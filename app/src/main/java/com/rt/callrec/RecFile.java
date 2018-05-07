package com.rt.callrec;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.rt.callrec.Constants.PATH;

class RecFile extends File {
    String name;

    public RecFile(File pathname, String name) {
        super(pathname.getAbsolutePath(), name);
        try {
            super.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.name = name;
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
        Log.d("L_Path", context.getFilesDir().getAbsolutePath() + PATH);
        return Arrays.asList(new File(context.getFilesDir().getAbsolutePath() + PATH).list());
    }
}