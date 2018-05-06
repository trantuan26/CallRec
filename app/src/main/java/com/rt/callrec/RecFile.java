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

    public RecFile(File pathname, String name) {
        super(pathname.getAbsolutePath(), name);
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

    public static ArrayList<Audio> getListFileName(Context context) {
        ArrayList<Audio> t = new ArrayList<>();
      List<String> a = Arrays.asList(new File(context.getFilesDir().getAbsolutePath()).list());
      for (int i = 0; i < a.size(); i++){
          Audio au = new Audio();
          au.setFileName(a.get(i));
          au.setUri(context.getFilesDir().getAbsolutePath()+a.get(i));
          t.add(au);
      }

        return t;
    }
}