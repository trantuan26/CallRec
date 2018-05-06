package com.rt.callrec;

import android.content.Context;
import android.graphics.Bitmap;

/**
 * Created by QNIT on 12/27/2016.
 */

public class RecItem extends RecFile{
    protected RecFile recFile;
    String timeCreate;
    private Bitmap contactPhoto;

    public RecItem(Context context, RecFile recFile) {
        this.recFile = recFile;
        timeCreate = recFile.getTimeDuration(context, recFile.lastModified());
    }

    public RecItem() {

    }

    public Bitmap getContactPhoto() {
        return contactPhoto;
    }

    public RecFile getRecFile() {
        return recFile;
    }

    public void setRecFile(Context context, RecFile recFile) {
        this.recFile = recFile;
        timeCreate = recFile.getTimeDuration(context, recFile.lastModified());
    }

    public void setContactPhoto(Bitmap contactPhoto){
        this.contactPhoto = contactPhoto;
    }

    public String getTimeCreate() {
        return timeCreate;
    }
}
